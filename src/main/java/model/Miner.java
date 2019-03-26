package model;

import javafx.util.Pair;

/**
 * Abstract class that gives layout for all possible types of miners.
 */
public abstract class Miner {

	/**
     * Simulation where this pool is initialized.
     */
	private Simulation sim;
	/**
     * Task that miner is currently working on.
     */
	private Task task;
	/**
     * Unique own id.
     */
	private final int id;
	/**
     * Partial and full proofs of work for each task.
     */
	private double fPoW;
	private double pPoW;
	/**
	 * Own revenue densities from the previous and current rounds. 
	 */
	private double ownRevDen;
	private double ownRevDenPrevRound;
	/**
     * Own revenue related to current state (concrete pool / solo mining).
     */
	private double revenueInOwnPool = 0;

	/**
     * Probabilistic values for setting Poisson distribution:
	 * -probability of finding a share.
	 * -probability of mining a block.
     */
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
	abstract void changePool(int placeRoundRobin);

	/**
	 * Function that sets poisson distribution for the game.
	 * source: https://stackoverflow.com/questions/1241555/algorithm-to-generate-poisson-and-binomial-random-numbers
	 * 
	 * @param lambda = amount of trails * probability of event A
	 * @return a number randomly drawn from a generated distribution.
	 */
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

	/**
	 * Checks whether miner is currently busy with a task.
	 * 
	 * @return whether the miner is working on a task
	 */
	public boolean isWorking() {
		return (this.task != null && this.task.getTime() > 0);
	}

	/**
	 * Draws own proof of work from Poisson distribution.
	 * Proof of work is relted to the task difficulty.
	 */
	public void generatePoW(){
		this.fPoW = poissonDistribution(probabiltyMineBlock * this.task.getTime());
		this.pPoW = poissonDistribution(miningPower * this.task.getTime());
	}

	public double getRevenueInOwnPool() {
		return revenueInOwnPool;
	}

	public void setRevenueInOwnPool(double revenueInOwnPool) {
		if(!Double.isNaN(revenueInOwnPool)){
			this.revenueInOwnPool += revenueInOwnPool;
		}
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

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public void setOwnRevDen(double rd) {
		this.ownRevDenPrevRound = ownRevDen;
		this.ownRevDen = rd;
	}

	public double getOwnRevDen() {
		return ownRevDen;
	}

	public double getOwnRevDenPrevRound() {
		return ownRevDenPrevRound;
	}

	public void setSim(Simulation sim) {
		this.sim = sim;
	}

	public Simulation getSim() {
		return sim;
	}
}