package com.zackehh.javaspaces.ui.components.tables;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * A simple class to represent a list of bids associated with
 * a given lot. This allows the developer a different entry
 * to JTable#setModel which allows simply passing Vectors which
 * will then be associated with the table. Convenience class.
 */
public class BidTable extends BaseTable {

    /**
     * Initializes the JTable with some custom styles and
     * modifies the headers to disallow reordering and
     * resizing. This is done via the constructor of BidTable.
     */
    public BidTable(){
        super();
    }

    /**
     * An alternative entry into JTable#setModel which provides
     * shorthand to set a DefaultTableModel to the JTable. This
     * accepts Vector matrices to quickly initialize data inside
     * the table.
     *
     * @param data          the data to display in rows
     * @param columns       the column names
     */
    public void setModel(Vector<Vector<String>> data, Vector<String> columns){
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
