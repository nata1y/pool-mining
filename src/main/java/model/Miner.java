package model;

import javafx.util.Pair;

public abstract class Miner {

	private Simulation sim;
	private Task task;
	private final int id;
	private double fPoW;
	private double pPoW;
	private double ownRevDen;
	private double revenueInOwnPool = 0;

	private final double miningPower = 1;
	private final double probabiltyMineBlock = 0.25;
	
	public Miner (Simulation sim, int id) {
		this.sim = sim;
		this.id = id;
		this.pPoW = Math.random() * 10;
		this.fPoW = 0.0;
	}

	abstract void work();
	abstract Pair<Double, Double> publish();
	abstract void calculateOwnRevDen();

	/** function for calculating poisson distrubution
	 taken from
	 https://stackoverflow.com/questions/1241555/algorithm-to-generate-poisson-and-binomial-random-numbers
	 lambda = amount of trails * probability of event A
	 **/
	public static int poissonDistribution(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		do {
			k++;
			p *= Math.random();
		} while (p > L);
		return k - 1;
	}

	public double getRevenueInOwnPool() {
		return revenueInOwnPool;
	}

	public void setRevenueInOwnPool(double revenueInOwnPool) {
		this.revenueInOwnPool += revenueInOwnPool;
	}

	public double getfPoW() {
		return fPoW;
	}

	public void setfPoW(double fPoW) {
		this.fPoW = fPoW;
	}

	public double getpPoW() {
		return pPoW;
	}

	public void setpPoW(double pPoW) {
		this.pPoW = pPoW;
	}

	public int getId() {
		return id;
	}

	public boolean isWorking() {
		return (this.task != null && this.task.getTime() > 0);
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public void generatePoW(){
		this.fPoW = poissonDistribution(probabiltyMineBlock * this.task.getTime());
		this.pPoW = poissonDistribution(miningPower * this.task.getTime());
	}

	public void setOwnRevDen(double rd) {
		this.ownRevDen = rd;
	}

	public double getOwnRevDen() {
		return ownRevDen;
	}

	public void setSim(Simulation sim) {
		this.sim = sim;
	}

	public Simulation getSim() {
		return sim;
	}
}