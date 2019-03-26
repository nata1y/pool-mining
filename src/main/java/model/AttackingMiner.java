package model;

import java.util.*;
import javafx.util.Pair;

/**
 * Represents a miner who sabotages some pool.
 */
public class AttackingMiner extends Miner{

    /**
     * Id of the pool own pool. 
     */
    private int poolId;
    /**
     * Id of the pool that is sabotages and where mining happens. 
     */
    private int attackedPoolId;
    /**
     * Own revenue in this pool.
     */
    private double revenueInAttackedPool = 0;

    public AttackingMiner(Simulation sim, int id, int poolId){
        super(sim, id);
        this.poolId = poolId;
    }

    /**
     * @return own partial and full proof of work (fPoW is always 0). 
     */
    public Pair<Double, Double> publish(){
        Pair<Double, Double> pair = new Pair(this.getpPoW(), 0.0);
        return pair;
    }

    /**
     * Calculate own current revenue density.
     */
    public void calculateOwnRevDen(){
		this.setOwnRevDen(getSim().getPools().get(poolId).getRevenueDensity()); 
	}

    /**
     * Work on own task for 1 step.
     */
    public void work(){
        this.getTask().work();
    }

    /**
     * Joins other pool if it is more profitable OR decides to mine solo.
     * 
     * @param placeRoundRobin place in the array of miners (in the simulation).
     */
    public void changePool(int placeRoundRobin){
        Pool candidatePool = null;
        double bestDen = getOwnRevDen();

        // Loop through all pools and try to find own with higher revenue density.
        for(Pool p: getSim().getPools()){
            if(p.getRevenueDensity() > bestDen || Double.isNaN(bestDen)){
                bestDen = p.getRevenueDensity();
                candidatePool = p;
            }
        }

        Pool ownPool = getSim().getPools().get(poolId);
        Pool attackedPool = getSim().getPools().get(attackedPoolId);

        // If such pool exists, become honest miner in that pool.
        if(candidatePool != null){
            ArrayList<Miner> attackedPoolMembers = attackedPool.getMembers();

            attackedPoolMembers.remove(this);
            attackedPool.setOwnInfiltrationRate(attackedPool.getOwnInfiltrationRate() - 1);
            ownPool.getInfiltrationRates()[attackedPoolId] -= 1;
            attackedPool.setMembers(attackedPoolMembers);

            ArrayList<Miner> newMembers = candidatePool.getMembers();
            HonestMiner newhm = new HonestMiner(getSim(), getId(), candidatePool.getId());
            newMembers.add(newhm);
            candidatePool.setMembers(newMembers);

            ArrayList<AttackingMiner> newSabotagers = ownPool.getSabotagers();
            newSabotagers.remove(this);
            ownPool.setSabotagers(newSabotagers);

            getSim().getMiners().remove(this);
            getSim().getMiners().add(placeRoundRobin, newhm);
        } 
        // Becomes solo miner if it is more profitable.
        else if(bestDen < 1/getSim().getMiningPower()){
            ownPool.getSabotagers().remove(this);
            attackedPool.getMembers().remove(this);
            ownPool.getInfiltrationRates()[attackedPoolId] -= 1;
            getSim().getMiners().remove(this);
            SoloMiner sm = new SoloMiner(getSim(), getId());
            getSim().getMiners().add(placeRoundRobin, sm);
        }
    }

    public int getPoolId(){
        return this.poolId;
    }

    public int getAttackedPoolId() {
        return attackedPoolId;
    }

    public void setAttackedPoolId(int attackedPoolId) {
        this.attackedPoolId = attackedPoolId;
    }

    public double getRevenueInAttackedPool() {
        return revenueInAttackedPool;
    }

    public void setRevenueInAttackedPool(double revenueInAttackedPool) {
        if(Double.isNaN(revenueInAttackedPool)){
            this.revenueInAttackedPool = 0;
        } else{
            this.revenueInAttackedPool = revenueInAttackedPool;
        }
    }
}
