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

    public void changePool(){
        Pool candidatePool = null;
        double bestDen = getOwnRevDen();

        for(Pool p: getSim().getPools()){
            if(p.getRevenueDensity() > bestDen){
                bestDen = p.getRevenueDensity();
                candidatePool = p;
            }
        }
        System.out.println("Before Own id: " + getId() + "; poolId " + poolId);
        if(candidatePool != null){
            System.out.println("Own id: " + getId() + "; poolId " + poolId + " cand pool id: " + candidatePool.getId());
            Pool ownPool = getSim().getPools().get(poolId);

            System.out.println("miners in cand pool: ");
            for(Miner m: candidatePool.getMembers()){
                System.out.print(m.getId());
                System.out.print(" ");
            }
            System.out.println();
            ArrayList<Miner> newMembers = candidatePool.getMembers();
            newMembers.add(this);
            
            candidatePool.setMembers(newMembers);
            System.out.println("miners in cand pool after addition: ");
            for(Miner m: candidatePool.getMembers()){
                System.out.print(m.getId());
                System.out.print(" ");
            }
            System.out.println();

            //newMembers.clear();
            newMembers = ownPool.getMembers();
            System.out.println("miners in own pool: ");
            for(Miner m: newMembers){
                System.out.print(m.getId());
                System.out.print(" ");
                System.out.println(this.equals(m));
            }
            System.out.println();
            this.poolId = candidatePool.getId();
            newMembers.remove(this);
            ownPool.setMembers(newMembers);
            System.out.println("miners in own pool after addition: ");
            for(Miner m: newMembers){
                System.out.print(m.getId());
                System.out.print(" ");
            }
            System.out.println();

            
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
