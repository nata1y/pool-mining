package controller;

import model.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionListener;
import java.util.Observer;
import java.awt.event.ActionEvent;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Controlls play button.
 */
public class ButtonPanel extends JPanel implements ActionListener, Observer {
    private Simulation sim;
    private ScheduledFuture<?> updateLoop;
    private TableController t;
    private JLabel showTime;
    private JLabel solo;
    private final ScheduledThreadPoolExecutor scheduler;
    private JButton play;

    public ButtonPanel(Simulation sim){
        this.sim = sim;
        this.t = new TableController(sim);
        this.sim.addObserver(this);
        this.showTime = new JLabel();
        this.scheduler = new ScheduledThreadPoolExecutor(1);

        JPanel grid = new JPanel();
        JPanel controller = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));

        showTime = new JLabel();
        showTime.setVerticalAlignment(JLabel.NORTH);
        showTime.setText("Time: " + this.sim.getTime());
        controller.add(showTime);

        play = new JButton("Play");
        play.setActionCommand("play");
        play.addActionListener(this);

        solo = new JLabel();
        solo.setVerticalAlignment(JLabel.NORTH);
        solo.setText("Amount solo miners: " + this.sim.getAmountSoloMiners());
        controller.add(solo);

        controller.add(play);

        grid.add(t, BorderLayout.CENTER);
        grid.add(controller, BorderLayout.CENTER);

        this.add(grid);
        play();
    }

    /**
     * If button is klicked evokes corresponding action.
     */
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "play":
                play();
                break;
            case "stop":
                stop();
                break;
            default:
                throw new RuntimeException("Unknown action command");
        }
    }

    /**
     * Allow simulation to proceed.
     */
    private void play() {
        if (isPlaying())
            return;

        updateLoop = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sim.timeStep();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        // Update the play/pause button
        play.setActionCommand("stop");
        play.setText("Stop");
    }

    /**
     * Pause the simulation.
     */
    private void stop() {
        if (!isPlaying())
            return;

        updateLoop.cancel(false);

        // Update the play/pause button
        play.setActionCommand("play");
        play.setText("Play");
    }

    private boolean isPlaying() {
        return updateLoop != null && !updateLoop.isCancelled();
    }

    /**
     * Completelly stops and clears current simulation. 
     */
    public void stopSimulation(){
        scheduler.shutdownNow();
    }

    /**
     * Update displayed time step and amount of solo miners.
     */
    @Override
    public void update(Observable source, Object arg) {
        t.getPtm().fireTableDataChanged();
        showTime.setText("Time step: " + this.sim.getTime());
        solo.setText("Amount solo miners: " + this.sim.getAmountSoloMiners());
    }
}
