package com.zackehh.javaspaces.auction;

import com.zackehh.javaspaces.util.Constants;
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
    private JLabel resultTextLabel, itemNameLabel, startingPriceLabel, itemDescriptionLabel;
    private JPanel bidListingPanel, fieldInputPanel;
    private JScrollPane itemListPanel;
    private JTable lotTable;
    private JTextArea itemListOut;
    private JTextField itemNameIn, startingPriceIn, itemDescriptionIn, resultTextOut;
    private JPanel cards, auctionCard, bidCard;

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
        setResizable(false);
        setVisible(true);

        new Thread(new Runnable(){

            DefaultTableModel model = ((DefaultTableModel) lotTable.getModel());

            @Override
            public void run() {

                while(true) {
                    try {
                        IWsLot template = new IWsLot(lots.size() + 1, null, null, null);
                        IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);
                        if (latestLot == null) {
                            Thread.sleep(Constants.POLLING_INTERVAL);
                        } else {
                            lots.add(latestLot);
                            model.addRow(new Object[]{
                                latestLot.getId(),
                                latestLot.getItemName(),
                                latestLot.getCurrentPrice()
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(Constants.POLLING_INTERVAL);
                        } catch(Exception ex){
                            // no-op
                        }
                    }
                }

            }

        }).start();

    }

    /**
     * Main body of UI creation. Creates main frame and
     * attaches all needed listeners.
     */
    private void initComponents() {
        setTitle(Constants.APPLICATION_TITLE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        cards = new JPanel(new CardLayout());

        auctionCard = new JPanel(new BorderLayout());
        bidCard = new JPanel(new BorderLayout());

        fieldInputPanel = new JPanel(new GridLayout(4, 2));
        fieldInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        itemNameLabel = new JLabel("Name of Item: ");
        itemNameIn = new JTextField("", 12);
        itemDescriptionLabel = new JLabel("Item description: ");
        itemDescriptionIn = new JTextField("", 1);
        startingPriceLabel = new JLabel("Starting Price: ");
        startingPriceIn = new JTextField("", 6);
        resultTextLabel = new JLabel("Result: ");
        resultTextOut = new JTextField("");

        resultTextOut.setEditable(false);

        fieldInputPanel.add(itemNameLabel);
        fieldInputPanel.add(itemNameIn);
        fieldInputPanel.add(itemDescriptionLabel);
        fieldInputPanel.add(itemDescriptionIn);
        fieldInputPanel.add(startingPriceLabel);
        fieldInputPanel.add(startingPriceIn);
        fieldInputPanel.add(resultTextLabel);
        fieldInputPanel.add(resultTextOut);

        auctionCard.add(fieldInputPanel, BorderLayout.NORTH);

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

        auctionCard.add(itemListPanel, BorderLayout.CENTER);

        bidListingPanel = new JPanel();
        bidListingPanel.setLayout(new FlowLayout());

        addJobButton = new JButton();
        addJobButton.setText("Add Auction Item");
        addJobButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resultTextOut.setText("");

                String itemName = itemNameIn.getText();
                String itemDescription = itemDescriptionIn.getText();
                Double startingPrice = getTextAsCurrency(startingPriceIn);

                if(startingPrice == null){
                    return;
                }

                try {
                    IWsSecretary secretary = (IWsSecretary) space.take(new IWsSecretary(), null, Constants.SPACE_TIMEOUT);

                    System.out.println("Secretary state: " + secretary.jobNumber);

                    int jobNumber = secretary.addNewJob();
                    System.out.println(jobNumber + "");
                    IWsLot newLot = new IWsLot(jobNumber, itemName, startingPrice, itemDescription);

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

        auctionCard.add(bidListingPanel, BorderLayout.SOUTH);

        cards.add(auctionCard, Constants.AUCTION_CARD);

        cards.add(bidCard, Constants.BID_CARD);

        cp.add(cards);
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
            resultTextOut.setText("Invalid price!");
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
