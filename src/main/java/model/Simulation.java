package model;

import java.util.*;
import java.util.Random;

import javafx.util.Pair;

/**
 * precision 100 77 23
 * 
 * convergence take MUCH MORE time because i estimate it in a shitty way
 * should be better if allow all miners to switch pools
 * 
 * no solo miners 2 pools m=100 all gather in 1 pool (which initially has larger mining power)
 * EXCEPT 50%-50% when they all make half of the miners to attack and converge to the same size
 * 
 * 3 equally sized pools - all gather in 1
 * 
 * black magic with rev dens
 * 
 * if all have option to switch pool at one step, converge to one super pool very fast
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
	private int checkConvergence = 0;
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
			int pool = i/(amountMiners/amountPools);
			//int pool = rand.nextInt(amountPools);

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

		for(Miner m: miners){
			m.calculateOwnRevDen();
		}

		if(time % s == 0){
			miners.get(currentMinerRoundRobin).changePool(currentMinerRoundRobin);
			currentMinerRoundRobin++;

			if(currentMinerRoundRobin == miners.size()){
				currentMinerRoundRobin = 0;
			}

			for(Pool p: pools){
				checkPool(p);
			}

			if(currentPoolRoundRobin >= pools.size()){
				currentPoolRoundRobin = 0;
			}
			pools.get(currentPoolRoundRobin).changeMiners();
			currentPoolRoundRobin++;

			isConverged = true;
			checkConvergence();

			if(isConverged){
				checkConvergence ++;
			} else {
				checkConvergence = 0;
			}
		}

		if(isConverged && checkConvergence >= (amountMiners + amountSoloMiners)){
			for(Pool p: pools){
				System.out.println("id " + p.getId() + " " + (p.getMembers().size() + p.getSabotagers().size() - p.getOwnInfiltrationRate()));
			}
		} else {
			isConverged = false;
		}

		setChanged();
		notifyObservers();
	}

	public void checkPool(Pool p){
		if((p.getMembers().size() - p.getOwnInfiltrationRate() + p.getSabotagers().size()) == 0){
			for(Miner m: p.getMembers()){
				HonestMiner nm = new HonestMiner(this, m.getId(), ((AttackingMiner)m).getPoolId());
				pools.get(((AttackingMiner)m).getPoolId()).getMembers().add(nm);
				pools.get(((AttackingMiner)m).getPoolId()).getSabotagers().remove(m);
				miners.add(nm);
				miners.remove(m);
			}
			for(Pool pool: pools){
				if(!pool.equals(p)){
					int [] infr = pool.getInfiltrationRates();
					infr[p.getId()] = 0;
					pool.setInfiltrationRates(infr);
				}
			}
			p.setOwnInfiltrationRate(0);
			p.setMembers(new ArrayList<Miner>());
			p.setSabotagers(new ArrayList<AttackingMiner>());
		}
	}

	public void checkConvergence(){
		for (Miner m: miners){
			if(m.getOwnRevDen() != m.getOwnRevDenPrevRound() && !Double.isNaN(m.getOwnRevDen())){
				isConverged = false;
			}
		}
		for (Pool p: pools){
			if(p.getRevenueDensity() != p.getRevenueDensityPrevRound() && !Double.isNaN(p.getRevenueDensity())){
				isConverged = false;
			}
		}
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

	public void setMiners(ArrayList<Miner> a) {
		this.miners = a;
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

	public void setAmountSoloMiners(int asm) {
		this.amountSoloMiners = asm;
	}

}

