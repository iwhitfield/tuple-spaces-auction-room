package com.zackehh.javaspaces.ui.components.tables;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.constants.Constants;
import com.zackehh.javaspaces.ui.cards.LotCard;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by iwhitfield on 01/12/14.
 */
public class LotTable extends JTable {

    public LotTable(final ArrayList<IWsLot> lots, final JPanel cards){
        setShowHorizontalLines(true);
        setRowSelectionAllowed(true);
        setDefaultRenderer(String.class, new DefaultTableCellRenderer() {{
            setHorizontalAlignment(JLabel.CENTER);
        }});

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                int row = rowAtPoint(event.getPoint());
                if (event.getClickCount() == 2) {
                    cards.add(new LotCard(cards, lots.get(row)), Constants.BID_CARD);
                    CardLayout cl = (CardLayout) (cards.getLayout());
                    cl.show(cards, Constants.BID_CARD);
                }
            }
        });

        JTableHeader tableHeader = getTableHeader();

        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);

        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void setModel(Object[][] data, Object[] columns){
        setModel(new DefaultTableModel(data, columns) {

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
