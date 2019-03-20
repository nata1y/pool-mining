package model;

import java.util.*;
import java.util.Random;

import javafx.util.Pair;

/**
 * precision 100 77 23
 */


public class Simulation extends Observable {
	private int time = 0;
	private int amountMiners;
	private int amountSoloMiners;
	private int amountPools;
	private boolean isConverged;
	private ArrayList<Pool> pools;
	private ArrayList<Miner> miners;
	private double[] poolRevenues;
	private int bound;
	private Random rand = new Random();

	private final int s = 1;
	private int currentPoolRoundRobin = 0;
	private int currentMinerRoundRobin = 0;

	private final double revenueForBlock = 100;

	public Simulation(int amountMiners, int amountPools, int amountSoloM, int bound){
		this.amountMiners = amountMiners;
		this.amountPools = amountPools;
		this.amountSoloMiners = amountSoloM;
		this.isConverged = false;
		this.bound = bound;
		this.poolRevenues = new double[amountPools];
		pools = new ArrayList<>(amountPools);
		miners = new ArrayList<>(amountMiners);
		initialize();
	}

	private void initialize(){
		for(int i = 0; i < amountMiners; i++){
			//int pool = i/(amountMiners/amountPools);
			int pool = rand.nextInt(amountPools);

			//50 m 4 p 30 sim
			/*int pool = 0;
			if(i >= 2*amountMiners/10 && i < 4*amountMiners/10){
				pool = 1;
			}
			if(i >= 4*amountMiners/10 && i < 6*amountMiners/10){
				pool = 2;
			}
			if(i >= 6*amountMiners/10){
				pool = 3;
			}*/
			/*int pool = 0;
			if(i > bound){
				pool = 1;
			}*/
			HonestMiner m = new HonestMiner(this, i, pool);
			miners.add(m);
		}

		for(int i = 0; i < amountPools; i++){
			this.poolRevenues[i] = 0.0;
			ArrayList<Miner> poolMiners = new ArrayList<>();

			for(int j = 0; j < miners.size(); j++){
				Miner m = miners.get(j);
				if(m instanceof HonestMiner){
					if (((HonestMiner) m).getPoolId() == i){
						poolMiners.add(miners.get(j));
					}
				}
			}

			Pool p = new Pool(this, i, 0.05, poolMiners);
			pools.add(p);
		}

		for (Pool p: pools){
			double set = p.calculateExpectedRevenueDensityGeneral(p.getInfiltrationRates());
			p.setRevenueDensity(set);
			p.setRevenueDensityPrevRound(set);
			p.setRevenueDensityIfNooneAttack(set);
		}

		for(int i = 0 ; i < amountSoloMiners; i++){
			SoloMiner m = new SoloMiner(this, amountMiners + i);
			miners.add(m);
		}
	}

	public void timeStep(){
		time ++;

		for(Miner m: this.miners){
			if(m instanceof SoloMiner){
				((SoloMiner) m).work();
				Pair<Double, Double> pow = ((SoloMiner) m).publish();
				if(pow.getValue() > 1.0){
					((SoloMiner) m).setRevenueInOwnPool(revenueForBlock);
				}
			}

			m.calculateOwnRevDen();
		}

		for(Pool p: this.pools){
			p.assignTasks();
			p.roundOfWork();
		}

		int poolId = 0;
		for(Pool p: this.pools){
			p.updatePoF();
			p.collectRevenueFromSabotagers();

			this.poolRevenues[poolId] = p.publishRevenue();
			poolId++;
			p.sendRevenueToAll();
		}

		if(time % s == 0){
			pools.get(currentPoolRoundRobin).changeMiners();
			currentPoolRoundRobin++;
			if(currentPoolRoundRobin == pools.size()){
				currentPoolRoundRobin = 0;
				isConverged = true;
				for (Pool p: pools){
					if(p.getRevenueDensity() != p.getRevenueDensityPrevRound()){
						isConverged = false;
					}
				}
			}

			miners.get(currentMinerRoundRobin).changePool();
		}

		setChanged();
		notifyObservers();
	}

	public double[] getPoolRevenues() {
		return poolRevenues;
	}

	public int getAmountMiners() {
		return amountMiners;
	}

	public ArrayList<Miner> getMiners() {
		return miners;
	}

	public int getAmountPools() {
		return amountPools;
	}

	public ArrayList<Pool> getPools() {
		return pools;
	}

	public int getTime() {
		return time;
	}

	public boolean isConverged() {
		return isConverged;
	}

	public double getRevenueForBlock(){
		return revenueForBlock;
	}

	public int getAmountSoloMiners() {
		return amountSoloMiners;
	}

}

