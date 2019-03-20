package controller;

import model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

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


    public MainController(){}

    public void setup() {
        if (window != null) {
            window.dispose();
            window = null;
        }

        final JFrame setup = new JFrame("");
        setup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent pane = (JComponent)setup.getContentPane();
        pane.setLayout(new BorderLayout());
        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel create = new JLabel ("Simulation settings", SwingConstants.LEFT);
        create.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        // amount of pools and amount of miners
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
                            startSimulations(Integer.parseInt(miners.getText()), Integer.parseInt(pools.getText()), Integer.parseInt(solom.getText()), Integer.parseInt(runs.getText()), 22);
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


    public void startSimulations(int amountAgents, int amountPools, int amountSoloM, int amountSim, int bound) {
        this.amountAgents = amountAgents;
        this.amountPools = amountPools;
        this.bound = bound;
        this.amountSim = amountSim - 1;

        currentSimulation = new Simulation(amountAgents, amountPools, amountSoloM, bound);
        currentSimulation.addObserver(this);
        window = createAndShowGUI();
    }

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

    private void deletePrevGUI() {
        frame.dispose();
        window.dispose();
        bp.stopSimulation();

    }


    public void update(Observable source, Object arg) {
        if(currentSimulation.isConverged()){
            //deletePrevGUI();
            /*
            int m1 = (bound + 1);
            int m2 = 100 - m1;
            double x1, x2;
            if(m1 <= m2/4){
                x1 = 0;
                x2 = m2/2;
            } else if(m2 <= m1/4){
                x1 = m1/2;
                x2 = 0;
            } else{
                x1 = Math.sqrt(m1 * m2) * (2 * Math.sqrt(m1) - Math.sqrt(m2)) / (Math.sqrt(m1) + Math.sqrt(m2));
                x2 = Math.sqrt(m1 * m2) * (2 * Math.sqrt(m2) - Math.sqrt(m1)) / (Math.sqrt(m1) + Math.sqrt(m2));
            }
            
            System.out.println("Convergence Time: " + currentSimulation.getTime());
            System.out.println("Starting rates (m1, m2): " + m1 + " , " + m2);
            System.out.println("Converge Rates (x1, x2): " + currentSimulation.getPools().get(1).getOwnInfiltrationRate() + " , " + currentSimulation.getPools().get(0).getOwnInfiltrationRate());
            System.out.println("Expected rates from the paper: " + Math.round(x1) + " , " + Math.round(x2));
            System.out.println();
            */
            System.out.print(currentSimulation.getTime());
            System.out.print(" ");

            if(amountSim > 0) {
                if(counter > 1){
                    counter --;
                    startSimulations(amountAgents, amountPools, amountSoloM, amountSim, bound);
                } else {
                    counter = 1;
                    System.out.println();
                    System.out.println(amountAgents + 50);
                    System.out.println();
                    startSimulations(amountAgents + 50, amountPools, amountSoloM, amountSim, bound);
                }
                
            }
        }
    }
}
