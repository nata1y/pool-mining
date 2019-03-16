package model;

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

    public void work(){
        this.getTask().work();
    }

    public void changePool(){

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
