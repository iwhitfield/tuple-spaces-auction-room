package com.zackehh.javaspaces.ui.components.tables;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.constants.Constants;
import com.zackehh.javaspaces.ui.cards.LotCard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * A simple class to represent a list of bids associated with
 * a given lot. Uses BaseTable.java to gain a quick default
 * styling. Adds a listener on a row to allow the creation of
 * cards with the given lot. This allows the developer a different entry
 * to JTable#setModel which allows simply passing Arrays which
 * will then be associated with the table. Convenience class.
 */
public class LotTable extends BaseTable {

    /**
     * Initializes the JTable with some custom styles and
     * modifies the headers to disallow reordering and
     * resizing. This is done via BidTable. Also adds a listener
     * to the data rows to enable card creation.
     *
     * @param lots          a list of lots
     * @param cards         the cards parent
     */
    public LotTable(final ArrayList<IWsLot> lots, final JPanel cards){
        super();

        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                int row = rowAtPoint(event.getPoint());
                if (event.getClickCount() == 2) {
                    cards.add(new LotCard(cards, lots.get(row)), Constants.BID_CARD);
                    CardLayout cl = (CardLayout) cards.getLayout();
                    cl.show(cards, Constants.BID_CARD);
                }
            }
        });
    }

    /**
     * An alternative entry into JTable#setModel which provides
     * shorthand to set a DefaultTableModel to the JTable. This
     * accepts Arrays matrices to quickly initialize data inside
     * the table.
     *
     * @param data          the data to display in rows
     * @param columns       the column names
     */
    public void setModel(Object[][] data, Object[] columns){
        setModel(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class getColumnClass(int column) {
                return String.class;
            }
        });
    }
}
