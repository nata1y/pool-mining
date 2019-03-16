package model;

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

    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
