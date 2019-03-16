package model;

import javafx.util.Pair;
import model.Miner;

class SoloMiner extends Miner {

    public SoloMiner(Simulation sim, int id) {
        super(sim, id);
    }

    public Task getNewTask(Miner m){
        Task task = new Task(0);
        return task;
    }

    public Pair<Double, Double> publish(){
        Pair<Double, Double> pair = new Pair(0, 0);
        return pair;
    }

    public void work(){}
}