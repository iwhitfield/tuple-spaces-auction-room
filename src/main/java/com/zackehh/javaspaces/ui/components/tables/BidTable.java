package com.zackehh.javaspaces.ui.components.tables;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.util.Vector;

/**
 * Created by iwhitfield on 01/12/14.
 */
public class BidTable extends JTable {

    public BidTable(){
        // Configure some of JTable's parameters
        setShowHorizontalLines(true);
        setRowSelectionAllowed(true);
        setDefaultRenderer(String.class, new DefaultTableCellRenderer() {{
            setHorizontalAlignment(JLabel.CENTER);
        }});

        JTableHeader tableHeader = getTableHeader();

        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);

        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void setModel(Vector<Vector<String>> bidHistory, Vector<String> columns){
        setModel(new DefaultTableModel(bidHistory, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }

            @Override
            public Class getColumnClass(int column) {
                return String.class;
            }
        });
    }
}
