package model;

import java.util.ArrayList;

import javafx.util.Pair;

/**
 * Represents a miner who mines homestly in a pool.
 */
public class HonestMiner extends Miner{

    /**
     * Id of the pool where mining happens. 
     */
    private int poolId;
    /**
     * Own revenue in this pool.
     */
    private double revenue = 0;

    public HonestMiner(Simulation sim, int id, int pool){
        super(sim, id);
        this.poolId = pool;
    }

    /**
     * @return own partial and full proof of work. 
     */
    public Pair<Double, Double> publish(){
        Pair<Double, Double> pair = new Pair(this.getpPoW(), this.getfPoW());
        return pair;
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

        // If such pool exists, become honest miner in that pool.
        if(candidatePool != null){
            Pool ownPool = getSim().getPools().get(poolId);
            ArrayList<Miner> newMembers = candidatePool.getMembers();

            newMembers.add(this);
            candidatePool.setMembers(newMembers);

            newMembers = ownPool.getMembers();
            this.poolId = candidatePool.getId();
            newMembers.remove(this);
            ownPool.setMembers(newMembers);
        } 
        // Becomes solo miner if it is more profitable.
        else if(bestDen < 1/getSim().getMiningPower()){
            getSim().getPools().get(poolId).getMembers().remove(this);
            getSim().getMiners().remove(this);
            SoloMiner sm = new SoloMiner(getSim(), getId());
            getSim().getMiners().add(placeRoundRobin, sm);
        }
    }

    /**
     * Calculate own current revenue density.
     */
    public void calculateOwnRevDen(){
		this.setOwnRevDen(getSim().getPools().get(poolId).getRevenueDensity()); 
	}

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public int getPoolId(){
        return this.poolId;
    }
}
