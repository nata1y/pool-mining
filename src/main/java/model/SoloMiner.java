package model;

import java.util.*;

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

    public void changePool(int placeRoundRobin){
        Pool candidatePool = null;
        double bestDen = getOwnRevDen();

        for(Pool p: getSim().getPools()){
            if(p.getRevenueDensity() > bestDen){
                bestDen = p.getRevenueDensity();
                candidatePool = p;
            }
        }

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