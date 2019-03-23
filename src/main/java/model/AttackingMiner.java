package model;

import java.util.*;
import javafx.util.Pair;

public class AttackingMiner extends Miner{


    private int poolId;
    private int attackedPoolId;
    private double revenueInAttackedPool = 0;

    public AttackingMiner(Simulation sim, int id, int poolId){
        super(sim, id);
        this.poolId = poolId;
    }

    public Pair<Double, Double> publish(){
        Pair<Double, Double> pair = new Pair(this.getpPoW(), 0.0);
        return pair;
    }

    public void calculateOwnRevDen(){
		this.setOwnRevDen(getSim().getPools().get(poolId).getRevenueDensity()); 
	}

    public void work(){
        this.getTask().work();
    }

    public void changePool(int placeRoundRobin){
        Pool candidatePool = null;
        double bestDen = getOwnRevDen();

        for(Pool p: getSim().getPools()){
            if(p.getRevenueDensity() > bestDen || Double.isNaN(bestDen)){
                bestDen = p.getRevenueDensity();
                candidatePool = p;
            }
        }

        if(candidatePool != null){
            Pool ownPool = getSim().getPools().get(poolId);
            Pool attackedPool = getSim().getPools().get(attackedPoolId);
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
        this.revenueInAttackedPool = revenueInAttackedPool;
    }

}
