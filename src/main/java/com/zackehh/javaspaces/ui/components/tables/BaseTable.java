package com.zackehh.javaspaces.ui.components.tables;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * A simple class to represent a list of bids associated with
 * a given lot. This allows a specific styling of table which
 * can be inherited to stay constant throughout the application.
 */
public class BaseTable extends JTable {

    /**
     * Initializes the JTable with some custom styles and
     * modifies the headers to disallow reordering and
     * resizing.
     */
    public BaseTable(){
        setShowHorizontalLines(true);
        setRowSelectionAllowed(true);
        setDefaultRenderer(String.class, new DefaultTableCellRenderer() {{
            setHorizontalAlignment(JLabel.CENTER);
        }});

        JTableHeader tableHeader = getTableHeader();

        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);

        DefaultTableCellRenderer renderer =
                (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();

        renderer.setHorizontalAlignment(SwingConstants.CENTER);
    }
}
