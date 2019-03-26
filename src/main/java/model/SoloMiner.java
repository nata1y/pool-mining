package model;

import java.util.*;

import javafx.util.Pair;
import model.Miner;

/**
 * Represents a miner who works outside any pool.
 */
public class SoloMiner extends Miner {

    public SoloMiner(Simulation sim, int id) {
        super(sim, id);
    }

    /**
     * @return own partial and full proof of work. 
     */
    public Pair<Double, Double> publish(){
        Pair<Double, Double> pair = new Pair(getpPoW(), getfPoW());
        return pair;
    }

    /**
     * Work on own task for 1 step.
     */
    public void work(){
        if (!this.isWorking()) {
            this.setTask(new Task((int)this.getpPoW() + 1));
        }
        this.generatePoW();
        this.getTask().work();
    }

    /**
     * Calculate own current revenue density.
     */
    public void calculateOwnRevDen(){
        setOwnRevDen(1 / getSim().getMiningPower());
    }

    /**
     * Joins pool if it is more profitable.
     * 
     * @param placeRoundRobin place in the array of miners (in the simulation).
     */
    public void changePool(int placeRoundRobin){
        Pool candidatePool = null;
        double bestDen = getOwnRevDen();

        // Loop through all pools and try to find own with higher revenue density. 
        for(Pool p: getSim().getPools()){
            if(p.getRevenueDensity() > bestDen){
                bestDen = p.getRevenueDensity();
                candidatePool = p;
            }
        }

        // If such pool exists, become honest miner in that pool.
        if(candidatePool != null){
            ArrayList<Miner> newMembers = candidatePool.getMembers();
            HonestMiner newhm = new HonestMiner(getSim(), getId(), candidatePool.getId());
            newMembers.add(newhm);
            candidatePool.setMembers(newMembers);

            getSim().getMiners().remove(this);
            getSim().getMiners().add(placeRoundRobin, newhm);
            getSim().setAmountSoloMiners(getSim().getAmountSoloMiners() - 1);
        }
    }
}