package model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.math.RoundingMode; 

import javafx.util.Pair;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.jblas.*;

/**
 * Pool class represent a mining pool in a simulation.
 */
public class Pool {

    /**
     * Simulation where this pool is initialized.
     */
    private Simulation sim;
    /**
     * Unique id of the pool.
     */
    private final int id;
    /**
     * Contribution fees charged from each member.
     */
    private double contributionFees;
    /**
     * Total revenue at each step.
     */
    private double revenue;
    /**
     * Revenue desity values for the current round, previous round and if no attack was happened.
     */
    private double revenueDensity;
    private double revenueDensityPrevRound;
    private double revenueDensityIfNooneAttack;
    /**
     * Rate at which pool is infiltrated by sabotagers.
     */
    private int ownInfiltrationRate;
    /**
     * Own infiltration rates for every other pool.
     */
    private int[] infiltrationRates;
    /**
     * Possible infiltration rates permutation.
     * Necessary for finding arg max of revenue desity function.
     */
    private ArrayList<int[]> infeltrationPermutations;
    /**
     * Income for the whole game, and income if noone would attack.
     */
    private double incomeWholeGameNooneattack;
    private double incomeWholeGame;
    /**
     * All mining members.
     */
    private ArrayList<Miner> members;
    /**
     * Sabotaging members.
     */
    private ArrayList<AttackingMiner> sabotagers = new ArrayList<>();

    public Pool(Simulation sim, int id, double fee, ArrayList<Miner> miners){
        this.sim = sim;
        this.id = id;
        this.revenue = 0;
        this.contributionFees = fee;
        this.ownInfiltrationRate = 0;
        this.members = miners;
        this.incomeWholeGame = 0;
        this.incomeWholeGameNooneattack = 0;

        this.infiltrationRates = new int[sim.getAmountPools()];
        for(int i = 0; i < sim.getAmountPools(); i++){
            infiltrationRates[i] = 0;
        }

        this.infeltrationPermutations = new ArrayList<>();
    }

    /**
     * Assign miner a task based on their partial proof of work.
     * 
     * @param miner which will get new task.
     */
    static void assignTask(Miner m){
        m.setTask(new Task((int)m.getpPoW() + 1));
    }

    /**
     * Assign task to all non-working miners.
     */
    public void assignTasks(){
        for(Miner m: members) {
            if (!m.isWorking()) {
                assignTask(m);
                m.generatePoW();
            }
        }
    }

    /**
     * Make miners work for one time step.
     */
    public void roundOfWork(){
        for(Miner m: members){
            m.work();
        }
    }

    /**
     * Update miners proof of work if they are done with the task.
     * Collect revenue if full proof of work satisfies.
     * Update income for the whole game in case no one would attack.
     */
    public void updatePoF(){
        for(Miner m: members){
            if (!m.isWorking()) {
                Pair<Double, Double> p = m.publish();
                if(p.getValue() > 1.0){
                    collectRevenueFromMiner(m);
                }
            }
        }

        incomeWholeGameNooneattack += this.revenue;
    }

    /**
     * Change infiltration rates to all other pools.
     */
    public void changeMiners(){
        int n;
        int[] newRate;
        // Find arg max of own revenue density function.
        newRate = calculateBestInfRate();

        // Switch own miners between pools accordingly.
        for(Pool p: sim.getPools()){
            int poolId = p.getId();
            while(newRate[poolId] > infiltrationRates[poolId]){
                infiltrationRates[poolId]++;
                p.increaseOwnInfiltrationRate();
                n = 0;
                while (n < members.size() && (members.get(n) instanceof AttackingMiner)) {
                    n++;
                }
                if (n < members.size()) {
                    Miner m = members.get(n);
                    members.remove(m);

                    AttackingMiner am = new AttackingMiner(this.sim, m.getId(), this.id);
                    am.setAttackedPoolId(poolId);
                    this.sabotagers.add(am);

                    ArrayList<Miner> newMembers = p.getMembers();
                    newMembers.add(am);
                    p.setMembers(newMembers);

                    sim.getMiners().remove(m);
                    sim.getMiners().add(am);
                }
            }

            while(newRate[poolId] < infiltrationRates[poolId]){
                infiltrationRates[poolId]--;
                p.decreaseOwnInfiltrationRate();

                AttackingMiner am = new AttackingMiner(sim, -1, -1);

                for(int i = 0; i < sabotagers.size(); i++){
                    if(sabotagers.get(i).getAttackedPoolId() == poolId){
                        am = sabotagers.get(i);
                        break;
                    }
                }

                ArrayList<Miner> newMembers = p.getMembers();
                newMembers.remove(am);
                p.setMembers(newMembers);

                HonestMiner hm = new HonestMiner(this.sim, am.getId(), this.id);
                this.members.add(hm);

                sabotagers.remove(am);

                sim.getMiners().remove(am);
                sim.getMiners().add(hm);
            }
        }
    }

    public void updateFees(){}

    /**
     * Publish own revenue (in the simulation).
     * 
     * @return revenue earned for the last step.
     */
    public double publishRevenue(){
        double perMinerRev = this.revenue/(members.size() + sabotagers.size());
        return perMinerRev;
    }

    /**
     * Collect revenue earned by sabotaging miners.
     */
    public void collectRevenueFromSabotagers(){
        for(AttackingMiner m: this.sabotagers){
            if(!Double.isNaN(m.getRevenueInAttackedPool())){
                this.revenue += m.getRevenueInAttackedPool();
            }
            m.setRevenueInAttackedPool(0);
        }
    }

    /**
     * Collect revenue from a miner who found a block.
     */
    public void collectRevenueFromMiner(Miner m){
        this.revenue += sim.getRevenueForBlock();
    }

    /**
     * Divide total revenue from the last step between all miners
     * based on their partial proof of work.
     */
    public void sendRevenueToAll(){
        double amountpow = 0;
        for(int i = 0; i < this.sabotagers.size(); i++){
            amountpow += sabotagers.get(i).getpPoW();
        }
        for(int i = 0; i < this.members.size(); i++){
            amountpow += members.get(i).getpPoW();
        }
        double eachRevenue = this.revenue/amountpow;
        for(AttackingMiner m: this.sabotagers){
            m.setRevenueInOwnPool(eachRevenue * m.getpPoW());
            incomeWholeGame += eachRevenue * m.getpPoW();
        }

        for(Miner m: this.members){
            if(m instanceof HonestMiner){
                m.setRevenueInOwnPool(eachRevenue * m.getpPoW());
                incomeWholeGame += eachRevenue * m.getpPoW();
            }

            if(m instanceof AttackingMiner){
                ((AttackingMiner) m).setRevenueInAttackedPool(eachRevenue * m.getpPoW());
            }
        }
        
        this.revenue = 0;    
    }

    /**
     * Calculate own revenue desity with the given infiltration rates.
     * 
     * @param rates infiltration rates of all other pools.
     * @return revenue density value.
     */
    public double calculateExpectedRevenueDensityGeneral(int[] rates){

        // calculate own coeficients in a system of linear equations
        int newInfRate = 0;
        double directRevenue;
        double[][] coefs = new double[sim.getAmountPools()][sim.getAmountPools()];
        double[][] constants = new double[sim.getAmountPools()][1];

        for(int i = 0; i < rates.length; i++){
            newInfRate += rates[i];
        }

        int simAttackingPower = newInfRate;

        for(Pool p: sim.getPools()){
            simAttackingPower += p.getOwnInfiltrationRate() - infiltrationRates[p.getId()];
        }

        int loyalMiners = members.size() - ownInfiltrationRate + sabotagers.size();

        directRevenue = (double)(loyalMiners - newInfRate) /
                (sim.getMiners().size() - simAttackingPower);

        constants[id][0] = directRevenue / (loyalMiners + ownInfiltrationRate);
        coefs[id][id] = 1;

        for(int i = 0; i < sim.getAmountPools(); i++){
            if(i != id){
                coefs[id][i] = -1 * (double)rates[i]/(loyalMiners + ownInfiltrationRate);
            }
        }

        // calculate coefs for all other pools
        for(Pool p: sim.getPools()){
            int poolId = p.getId();
            int infRate = 0;

            if(poolId != this.id) {
                for (int i = 0; i < rates.length; i++) {
                    infRate += p.getInfiltrationRates()[i];
                }

                int ownNewInfRate = p.getOwnInfiltrationRate() - infiltrationRates[poolId] + rates[poolId];

                loyalMiners = p.getMembers().size() - p.getOwnInfiltrationRate() + p.getSabotagers().size();

                directRevenue = (double)(loyalMiners - infRate) /
                        (sim.getMiners().size() - simAttackingPower);

                constants[poolId][0] = directRevenue / (loyalMiners + ownNewInfRate);
                coefs[poolId][poolId] = 1;

                for (int i = 0; i < sim.getAmountPools(); i++) {
                    if (i != poolId) {
                        coefs[poolId][i] = -1 * (double)p.getInfiltrationRates()[i] / (loyalMiners + ownNewInfRate);
                    }
                }
            }
        }

        // Use jblas to find result
        DoubleMatrix coef_matrix = new DoubleMatrix(coefs);
        DoubleMatrix const_matrix = new DoubleMatrix(constants);
        DoubleMatrix res = Solve.solve(coef_matrix, const_matrix);

        return res.get(id);
    }

/**

    public double calculateExpectedRevenueDensityHardCoded(int[] rates){
        Pool p;
        double m1, m2, x12, x21, R1, R2;
        double newRevDen = 0;

        if(this.id == 0){
            p = sim.getPools().get(1);
            m1 = members.size() - ownInfiltrationRate + sabotagers.size();
            m2 = p.getMembers().size() - p.getOwnInfiltrationRate() + p.getSabotagers().size();
            x12 = rates[1];
            x21 = p.getInfiltrationRates()[0];
            R1 = (m1 - x12)/(sim.getAmountMiners() - x12 - x21);
            R2 = (m2 - x21)/(sim.getAmountMiners() - x12 - x21);
            newRevDen = (m2 * R1 + x12 * (R1 + R2)) / (m1*m2 + m1*x12 + m2*x21);
        } else{
            p = sim.getPools().get(0);
            m2 = members.size() - ownInfiltrationRate + sabotagers.size();
            m1 = p.getMembers().size() - p.getOwnInfiltrationRate() + p.getSabotagers().size();
            x21 = rates[0];
            x12 = p.getInfiltrationRates()[1];
            R1 = (m1 - x12)/(sim.getAmountMiners() - x12 - x21);
            R2 = (m2 - x21)/(sim.getAmountMiners() - x12 - x21);
            newRevDen = (m1 * R2 + x21 * (R1 + R2)) / (m1*m2 + m1*x12 + m2*x21);
        }

        return newRevDen;
    }
*/

    /**
     * Calculates best infiltration rate for this pool against all other pool.
     * 
     * @return best infiltration rate based on the simulation values from the last round.
     */
    public int[] calculateBestInfRate(){
        int[] bestRate = infiltrationRates;

        // Feasible range for attacking miners from paper.
        int top = members.size() - ownInfiltrationRate + sabotagers.size();

        double maxRev = calculateExpectedRevenueDensityGeneral(infiltrationRates);
        generateInfiltrationPermutations(top, 0, new int[sim.getAmountPools()]);

        // Choose infiltration rates that yield max of revenue density function.
        for(int[] permutation: this.infeltrationPermutations){
            Double res = calculateExpectedRevenueDensityGeneral(permutation);

            // Edge case when every majority of miners converge into 1 pool and there are empty pools.
            if(Double.isNaN(maxRev) && top >= sim.getMiners().size()/sim.getAmountPools()){
                maxRev = 1.0/(sim.getMiners().size());
                bestRate = permutation;
            }

            if(res > maxRev){
                maxRev = res;
                bestRate = permutation;
            }
        }

        this.infeltrationPermutations.clear();
        this.revenueDensityPrevRound = this.revenueDensity;
        this.revenueDensity = maxRev;

        return bestRate;
    }

    /**
     * Generate possible infiltration rates.
     * 
     * @param possibleAmountMiners maximum amount of miners that can sabotage. 
     * @param permutation current permutation that is being generated.
     */
    public void generateInfiltrationPermutations(int possibleAmountMiners, int pools, int[] permutation) {
        if(pools == this.sim.getAmountPools()){
            this.infeltrationPermutations.add(permutation.clone());
        }
        else {
            for (int i = 0; i <= possibleAmountMiners; i++) {
                permutation[pools] = i;
                generateInfiltrationPermutations(possibleAmountMiners - i, pools + 1, permutation);

                if(pools == id){
                    break;
                }
            }
        }
    }

    public ArrayList<Miner> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Miner> members) {
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public ArrayList<AttackingMiner> getSabotagers() {
        return sabotagers;
    }

    public void setSabotagers(ArrayList<AttackingMiner> s) {
        this.sabotagers = s;
    }

    public double getContributionFees() {
        return contributionFees;
    }

    public void setContributionFees(double fee) {
        this.contributionFees = fee;
    }

    public int[] getInfiltrationRates() {
        return infiltrationRates;
    }

    public void setInfiltrationRates(int[] infr) {
        infiltrationRates = infr;
    }

    public double getRevenueDensity() {
        return revenueDensity;
    }

    public void setRevenueDensity(double revenueDensity) {
        this.revenueDensity = revenueDensity;
    }

    public void decreaseOwnInfiltrationRate() {
        this.ownInfiltrationRate--;
    }

    public void increaseOwnInfiltrationRate() {
        this.ownInfiltrationRate++;
    }

    public int getOwnInfiltrationRate() {
        return ownInfiltrationRate;
    }

    public void setOwnInfiltrationRate(int rate) {
        ownInfiltrationRate = rate;
    }

    public double getRevenueDensityIfNooneAttack() {
        return revenueDensityIfNooneAttack;
    }

    public void setRevenueDensityIfNooneAttack(double revenueDensityIfNooneAttack) {
        this.revenueDensityIfNooneAttack = revenueDensityIfNooneAttack;
    }

    public double getRevenueDensityPrevRound() {
        return revenueDensityPrevRound;
    }

    public void setRevenueDensityPrevRound(double revenueDensityPrevRound) {
        this.revenueDensityPrevRound = revenueDensityPrevRound;
    }

    public double getIncomeWholeGame() {
        return incomeWholeGame;
    }

    public double getIncomeWholeGameNooneattack() {
        return incomeWholeGameNooneattack;
    }
}
