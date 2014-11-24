package com.zackehh.javaspaces.auction;

import com.zackehh.javaspaces.printer.IWsQueueItem;
import com.zackehh.javaspaces.util.SpaceUtils;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class IWsAuctionRoom extends JFrame {

    private ArrayList<IWsLot> lots = new ArrayList<IWsLot>();
    private JavaSpace space;

    private JButton addJobButton;
    private JLabel errorTextLabel, itemNameLabel, startingPriceLabel, itemDescriptionLabel;
    private JPanel bidListingPanel, fieldInputPanel;
    private JScrollPane itemListPanel;
    private JTable lotTable;
    private JTextArea itemListOut;
    private JTextField itemNameIn, startingPriceIn, itemDescriptionIn, errorTextOut;

    public static void main(String[] args) {
        new IWsAuctionRoom();
    }

    public IWsAuctionRoom() {
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the javaspace");
            System.exit(1);
        }

        try {
            Object o = space.read(new IWsSecretary(), null, 1000);
            if(o == null){
                space.write(new IWsSecretary(0), null, Lease.FOREVER);
            }
        } catch(Exception e){
            System.err.println("Died trying to read from the space: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        initComponents();
        pack();
        setVisible(true);

        new Thread(new Runnable(){

            DefaultTableModel model = ((DefaultTableModel) lotTable.getModel());

            @Override
            public void run() {

                while(true) {
                    try {
                        IWsLot template = new IWsLot(lots.size() + 1, null, null, null);
                        IWsLot latestLot = (IWsLot) space.read(template, null, 3000);
                        if (latestLot == null) {
                            Thread.sleep(5000);
                        } else {
                            lots.add(latestLot);
                            model.addRow(new Object[]{latestLot.getId(), latestLot.getItemName(), latestLot.getCurrentPrice()});
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(5000);
                        } catch(Exception ex){
                            // no-op
                        }
                    }
                }

            }

        }).start();

    }

    private void initComponents() {
        setTitle("Auction Room");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        fieldInputPanel = new JPanel(new GridLayout(4, 2));
        fieldInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        itemNameLabel = new JLabel("Name of Item: ");
        itemNameIn = new JTextField("", 12);
        itemDescriptionLabel = new JLabel("Item description: ");
        itemDescriptionIn = new JTextField("", 1);
        startingPriceLabel = new JLabel("Starting Price: ");
        startingPriceIn = new JTextField("", 6);
        errorTextLabel = new JLabel("Result: ");
        errorTextOut = new JTextField("");

        errorTextOut.setEditable(false);

        fieldInputPanel.add(itemNameLabel);
        fieldInputPanel.add(itemNameIn);
        fieldInputPanel.add(itemDescriptionLabel);
        fieldInputPanel.add(itemDescriptionIn);
        fieldInputPanel.add(startingPriceLabel);
        fieldInputPanel.add(startingPriceIn);
        fieldInputPanel.add(errorTextLabel);
        fieldInputPanel.add(errorTextOut);

        cp.add(fieldInputPanel, "North");

        itemListOut = new JTextArea(30, 30);
        itemListOut.setEditable(false);
        itemListPanel = new JScrollPane(
                itemListOut,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        String[] columns = new String[] {
            "Lot ID", "Item Name", "Current Price"
        };

        String[][] dataValues = new String[lots.size()][columns.length + 1];

        for(int iY = 0; iY < lots.size(); iY++){
            dataValues[iY][0] = lots.get(iY).getId().toString();
            dataValues[iY][1] = lots.get(iY).getItemName();
            dataValues[iY][2] = lots.get(iY).getCurrentPrice().toString();
        }

        lotTable = new JTable();

        // Configure some of JTable's paramters
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

        cp.add(itemListPanel, "Center");

        bidListingPanel = new JPanel();
        bidListingPanel.setLayout(new FlowLayout());

        addJobButton = new JButton();
        addJobButton.setText("Add Auction Item");
        addJobButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                errorTextOut.setText("");

                String itemName = itemNameIn.getText();
                String itemDescription = itemDescriptionIn.getText();
                Double startingPrice = getTextAsCurrency(startingPriceIn);

                if(startingPrice == null){
                    return;
                }

                try {
                    IWsSecretary secretary = (IWsSecretary) space.take(new IWsSecretary(), null, 3000);

                    System.out.println("Secretary state: " + secretary.jobNumber);

                    int jobNumber = secretary.addNewJob();
                    System.out.println(jobNumber + "");
                    IWsLot newLot = new IWsLot(jobNumber, itemName, startingPrice, itemDescription);

                    space.write(newLot, null, Lease.FOREVER);
                    space.write(secretary, null, Lease.FOREVER);
                } catch(Exception e) {
                    System.err.println("Error when adding lot to the space: " + e);
                    e.printStackTrace();
                }

            }
        });
        bidListingPanel.add(addJobButton);

        cp.add(bidListingPanel, "South");
    }

    /**
     * Parse a text input as a Number. Should the input not be
     * a valid Number, set errorTextOut to display a parse error.
     *
     * @param  component    the component to retrieve text of
     * @return Number       a valid Number object
     */
    private Number getTextAsNumber(JTextComponent component){
        try {
            return NumberFormat.getInstance().parse(component.getText());
        } catch(ParseException e){
            errorTextOut.setText("Invalid price!");
            return null;
        }
    }

    /**
     * Parse a text input as a currency (double). Should the input
     * not be a valid Number, set errorTextOut to display a parse error.
     *
     * @param  component    the component to retrieve text of
     * @return Double       a valid Double currency object
     */
    private Double getTextAsCurrency(JTextComponent component){
        DecimalFormat currencyEnforcer = new DecimalFormat("#.##");
        Number textAsNumber = getTextAsNumber(component);
        if(textAsNumber == null){
            return null;
        }
        Double textAsDouble = textAsNumber.doubleValue();
        String textAsCurrency = currencyEnforcer.format(textAsDouble);
        return Double.parseDouble(textAsCurrency);
    }

}
