package model;

import javafx.util.Pair;
import model.Miner;

/**
 * look at solo miner as at pool with 1 member
 */

public class SoloMiner extends Miner {

    public SoloMiner(Simulation sim, int id) {
        super(sim, id);
    }

    public Pair<Double, Double> publish(){
        Pair<Double, Double> pair = new Pair(getpPoW(), getfPoW());
        return pair;
    }

    public void work(){
        if (!this.isWorking()) {
            this.setTask(new Task((int)this.getpPoW() + 1));
        }
        this.generatePoW();
        this.getTask().work();
    }

    public void calculateOwnRevDen(){
        double simAttackingPower = 0;
        for(Pool p: getSim().getPools()){
            simAttackingPower += p.getOwnInfiltrationRate();
        }
        setOwnRevDen(1 / (getSim().getAmountMiners() - simAttackingPower));
    }

    public void changePool(){
        
    }
}