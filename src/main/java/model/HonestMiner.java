package model;

import java.util.ArrayList;

import javafx.util.Pair;

public class HonestMiner extends Miner{

    private int poolId;
    private double revenue = 0;

    public HonestMiner(Simulation sim, int id, int pool){
        super(sim, id);
        this.poolId = pool;
    }

    public int getPoolId(){
        return this.poolId;
    }

    public Pair<Double, Double> publish(){
        Pair<Double, Double> pair = new Pair(this.getpPoW(), this.getfPoW());
        return pair;
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
            ArrayList<Miner> newMembers = candidatePool.getMembers();

            newMembers.add(this);
            candidatePool.setMembers(newMembers);

            newMembers = ownPool.getMembers();
            this.poolId = candidatePool.getId();
            newMembers.remove(this);
            ownPool.setMembers(newMembers);
        }
    }

    public void calculateOwnRevDen(){
		this.setOwnRevDen(getSim().getPools().get(poolId).getRevenueDensity()); 
	}

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
