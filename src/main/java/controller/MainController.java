package controller;

import model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class MainController implements Observer {
    static JFrame window;
    private Simulation currentSimulation;
    private JPanel pane;
    private int amountAgents;
    private int amountPools;
    private int amountSoloM;
    private int amountSim;
    private int bound;
    private ButtonPanel bp;
    private JFrame frame;
    private int counter = 1;
    private int b = 1;

    private Random rand = new Random();

    public MainController(){}

    /**
     * Set up the simulation. 
     */
    public void setup() {
        if (window != null) {
            window.dispose();
            window = null;
        }

        /* Take initial amount of pool miners, pools, solo miners and simulation repetitons
           from the user input */
        final JFrame setup = new JFrame("");
        setup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent pane = (JComponent)setup.getContentPane();
        pane.setLayout(new BorderLayout());
        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel create = new JLabel ("Simulation settings", SwingConstants.LEFT);
        create.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setLayout(new GridLayout(4, 2));

        JLabel label = new JLabel("Amount of Miners in pools: ");
        JTextField field = new JTextField(Integer.toString(10));
        body.add(label);
        body.add(field);
        JTextField miners = field;

        label = new JLabel("Amount of Pools: ");
        field = new JTextField(Integer.toString(2));
        body.add(label);
        body.add(field);
        JTextField pools = field;

        label = new JLabel("Amount of Solo Miners: ");
        field = new JTextField(Integer.toString(2));
        body.add(label);
        body.add(field);
        JTextField solom = field;

        label = new JLabel("Amount of Runs: ");
        field = new JTextField(Integer.toString(1));
        body.add(label);
        body.add(field);
        JTextField runs = field;

        JButton ok = new JButton("Set");
        ok.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            startSimulations(Integer.parseInt(miners.getText()), Integer.parseInt(pools.getText()), Integer.parseInt(solom.getText()), Integer.parseInt(runs.getText()));
                            setup.dispose();
                        } catch (RuntimeException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                }
        );

        pane.add(create, BorderLayout.NORTH);
        pane.add(body, BorderLayout.CENTER);
        pane.add(ok, BorderLayout.SOUTH);

        setup.pack();
        setup.setLocationRelativeTo(null);
        setup.setSize(300, 200);
        setup.setVisible(true);
    }

    /**
     * Create simulation with given parameters.
     */
    public void startSimulations(int amountAgents, int amountPools, int amountSoloM, int amountSim) {
        this.amountAgents = amountAgents;
        this.amountPools = amountPools;
        this.amountSim = amountSim - 1;

        currentSimulation = new Simulation(amountAgents, amountPools, amountSoloM);
        currentSimulation.addObserver(this);
        window = createAndShowGUI();
    }

    /**
     * Create simulation frame and start the simulation.
     */
    private JFrame createAndShowGUI() {
        frame = new JFrame("Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        pane = new JPanel(new GridLayout(1, 2));

        bp = new ButtonPanel(currentSimulation);
        currentSimulation.addObserver(bp);
        pane.add(bp);

        frame.add(pane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return frame;
    }

    /**
     * Stop the simulation ad selete simulation frame.
     */
    private void deletePrevGUI() {
        frame.dispose();
        window.dispose();
        bp.stopSimulation();

    }

    /**
     * Get simulation parameters and update the whole game.
     */
    public void update(Observable source, Object arg) {
        // If the simulation has converged.
        if(currentSimulation.isConverged()){
            deletePrevGUI();
            //Start next simulation with new parameters.
            if(amountSim > 0) {
                System.out.println("Convergence Time: " + currentSimulation.getTime());
                if(counter > 1){
                    counter --;
                    startSimulations(amountAgents, amountPools, amountSoloM, amountSim);
                } else {
                    counter = 1;
                    bound ++;
                    //System.out.println("________ new dist: " + bound);
                    startSimulations(amountAgents, amountPools, 0, amountSim);
                }
                
            }
        }
    }
}
