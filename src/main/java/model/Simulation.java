package model;

import java.util.*;
import java.util.Random;


public class Simulation extends Observable {
	private int time = 0;
	private int amountMiners;
	private int amountPools;
	private boolean isConverged;
	private ArrayList<Pool> pools;
	private ArrayList<Miner> miners;
	private double[] poolRevenues;
	private int bound;
	private Random rand = new Random();

	private final int s = 1;
	private int currentPoolRoundRobin = 0;

	public Simulation(int amountMiners, int amountPools, int bound){
		this.amountMiners = amountMiners;
		this.amountPools = amountPools;
		this.isConverged = false;
		this.bound = bound;
		this.poolRevenues = new double[amountPools];
		pools = new ArrayList<>(amountPools);
		miners = new ArrayList<>(amountMiners);
		initialize();
	}

	private void initialize(){
		for(int i = 0; i < amountMiners; i++){
			int pool = i/(amountMiners/amountPools);
			//int pool = rand.nextInt(amountPools);
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
	}

	public void timeStep(){
		time ++;

		/*for(Miner m: this.miners){
			//instance of SOlo miner
			//generateTask();
			//m.work();
		}*/

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
		}

		/*for(Miner m: this.miners){
			//change Pool based on PoolRevenue;
		}*/

		for(Pool p: this.pools){
			p.sendRevenueToAll();
			//p.changeMiners();
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

}

