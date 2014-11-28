package com.zackehh.javaspaces.ui;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.auction.IWsSecretary;
import com.zackehh.javaspaces.util.Constants;
import com.zackehh.javaspaces.util.InterfaceUtils;
import com.zackehh.javaspaces.util.UserUtils;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class AuctionCard extends JPanel {

    private JButton addJobButton;
    private JLabel resultTextLabel, itemNameLabel, startingPriceLabel, itemDescriptionLabel;
    private JPanel bidListingPanel, fieldInputPanel;
    private JScrollPane itemListPanel;
    private JTable lotTable;
    private JTextArea itemListOut;
    private JTextField itemNameIn, startingPriceIn, itemDescriptionIn;
    private JResultText resultTextOut;

    public AuctionCard(final JavaSpace space, final ArrayList<IWsLot> lots, final JPanel cards){
        super(new BorderLayout());

        fieldInputPanel = new JPanel(new GridLayout(4, 2));
        fieldInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        itemNameLabel = new JLabel("Name of Item: ");
        itemNameIn = new JTextField("", 12);
        itemDescriptionLabel = new JLabel("Item description: ");
        itemDescriptionIn = new JTextField("", 1);
        startingPriceLabel = new JLabel("Starting Price: ");
        startingPriceIn = new JTextField("", 6);
        resultTextLabel = new JLabel("Result: ");
        resultTextOut = new JResultText();

        fieldInputPanel.add(itemNameLabel);
        fieldInputPanel.add(itemNameIn);
        fieldInputPanel.add(itemDescriptionLabel);
        fieldInputPanel.add(itemDescriptionIn);
        fieldInputPanel.add(startingPriceLabel);
        fieldInputPanel.add(startingPriceIn);
        fieldInputPanel.add(resultTextLabel);
        fieldInputPanel.add(resultTextOut);

        add(fieldInputPanel, BorderLayout.NORTH);

        itemListOut = new JTextArea(30, 30);
        itemListOut.setEditable(false);
        itemListPanel = new JScrollPane(
                itemListOut,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        String[] columns = new String[] {
            "Lot ID", "Item Name", "Seller ID", "Current Price"
        };

        String[][] dataValues = new String[0][columns.length + 1];

        lotTable = new JTable();

        // Configure some of JTable's parameters
        lotTable.setShowHorizontalLines(true);
        lotTable.setRowSelectionAllowed(true);
        lotTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer(){{
            setHorizontalAlignment(JLabel.CENTER);
        }});

        lotTable.setModel(new DefaultTableModel(dataValues, columns) {

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

        lotTable.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent event) {
                int row = lotTable.rowAtPoint(event.getPoint());
                if(event.getClickCount() == 2){
                    System.out.println("Selected ID: " + lots.get(row).getId() +
                            ", Item Name: " + lots.get(row).getItemName());
                    cards.add(new LotCard(cards, lots.get(row)), Constants.BID_CARD);
                    CardLayout cl = (CardLayout)(cards.getLayout());
                    cl.show(cards, Constants.BID_CARD);
                }
            }
        });

        JTableHeader tableHeader = lotTable.getTableHeader();

        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);

        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        // Add the table to a scrolling pane
        itemListPanel = new JScrollPane(lotTable);

        add(itemListPanel, BorderLayout.CENTER);

        bidListingPanel = new JPanel();
        bidListingPanel.setLayout(new FlowLayout());

        addJobButton = new JButton();
        addJobButton.setText("Add Auction Item");
        addJobButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resultTextOut.setText("");

                String itemName = itemNameIn.getText();
                String itemDescription = itemDescriptionIn.getText();
                Double startingPrice = InterfaceUtils.getTextAsNumber(startingPriceIn).doubleValue();

                if(startingPrice == null || startingPrice == 0){
                    resultTextOut.setText("Invalid price!");
                    return;
                }

                try {
                    IWsSecretary secretary = (IWsSecretary) space.take(new IWsSecretary(), null, Constants.SPACE_TIMEOUT);

                    int jobNumber = secretary.addNewJob();
                    IWsLot newLot = new IWsLot(jobNumber, UserUtils.getCurrentUser(), null, itemName, startingPrice, itemDescription);

                    space.write(newLot, null, Lease.FOREVER);
                    space.write(secretary, null, Lease.FOREVER);
                    resultTextOut.setText("Added Lot #" + jobNumber + "!");
                } catch(Exception e) {
                    System.err.println("Error when adding lot to the space: " + e);
                    e.printStackTrace();
                }

            }
        });
        bidListingPanel.add(addJobButton);

        add(bidListingPanel, BorderLayout.SOUTH);
    }

    public DefaultTableModel getTableModel(){
        return ((DefaultTableModel) lotTable.getModel());
    }

    private class JResultText extends JLabel {

        @Override
        public void setText(String text){
            super.setText("  " + text);
        }

    }
}
