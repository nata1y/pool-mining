package controller;

import model.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.event.TableModelEvent;

/**
 * Controlls displayed table of the simulation.
 */
public class TableController extends JPanel{
    private Simulation sim;
    private PoolTableModel ptm;

    public TableController(Simulation simulation) {
        this.sim = simulation;

        buildTable();
    }

    /**
     * Representation of the displayed table.
     */
    public void buildTable(){
        JTable pt;
        JPanel body;
        JLabel[] rows = new JLabel[this.sim.getAmountPools()];

        for(int i = 0; i < this.sim.getAmountPools(); i++){
            rows[i] = new JLabel(""  + this.sim.getPools().get(i).getId());
        }

        ptm = new PoolTableModel(this.sim);
        pt = new JTable(ptm);
        pt.setRowHeight(30);
        pt.setShowVerticalLines(true);
        pt.setShowHorizontalLines(false);
        if(30*this.sim.getAmountPools() <= 700){
            pt.setPreferredScrollableViewportSize(new Dimension(1250, 30*this.sim.getAmountPools()));
        } else {
            pt.setPreferredScrollableViewportSize(new Dimension(1250, 700));
        }

        body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.PAGE_AXIS));

        body.add(new JScrollPane(pt));

        this.add(body, BorderLayout.CENTER);
    }

    public PoolTableModel getPtm() {
        return ptm;
    }

    public void setPtm(PoolTableModel ptm) {
        this.ptm = ptm;
    }

    static class PoolTableModel extends AbstractTableModel implements TableModelListener {
        private String[] columns = {
                "Pool",
                "Number of mining members",
                "Revenue",
                "Number of attacking miners",
                "Fee",
                "Current revenue density",
                "Revenue if no one attack",
                "Cuurent revenue for the whole game",
                "Revenue for the whole game if noone attack"
        };

        private ArrayList<Pool> pools;
        private double[] revenues;

        public PoolTableModel(Simulation sim) {
            this.pools = new ArrayList<>(sim.getPools());
            this.revenues = sim.getPoolRevenues();
            addTableModelListener(this);
        }

        public Pool getPool(int row) {
            return pools.get(row);
        }

        @Override
        public int getRowCount() {
            return pools.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Pool pool = this.getPool(row);

            switch (column) {
                case 0:
                    return pool.getId();
                case 1:
                    return pool.getMembers().size();
                case 2:
                    return revenues[pool.getId()];
                case 3:
                    return pool.getSabotagers().size();
                case 4:
                    return pool.getContributionFees();
                case 5:
                    return pool.getRevenueDensity();
                case 6:
                    return pool.getRevenueDensityIfNooneAttack();
                case 7:
                    return pool.getIncomeWholeGame();
                case 8:
                    return pool.getIncomeWholeGameNooneattack();
                default:
                    return null;
            }
        }

        @Override
        public void tableChanged(TableModelEvent e) {

        }
    }

}
