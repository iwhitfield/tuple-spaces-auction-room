package com.zackehh.javaspaces.ui.cards;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.auction.IWsSecretary;
import com.zackehh.javaspaces.constants.Constants;
import com.zackehh.javaspaces.ui.components.JResultText;
import com.zackehh.javaspaces.ui.components.tables.LotTable;
import com.zackehh.javaspaces.ui.listeners.GenericNotificationListener;
import com.zackehh.javaspaces.util.InterfaceUtils;
import com.zackehh.javaspaces.util.SpaceUtils;
import com.zackehh.javaspaces.util.UserUtils;
import net.jini.core.event.RemoteEvent;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * The main body of the application's UI. This card displays
 * the list of lots in the auction currently, along with their
 * information. Clicking a row allows the user to go to a card
 * specifically based on the chosen lot to allow bid's etc.
 * This card also provides a way for a user to add a new lot.
 */
public class AuctionCard extends JPanel {

    /**
     * The common JavaSpace instance, stored privately.
     */
    private final JavaSpace space;

    /**
     * The common TransactionManager instance, stored privately.
     */
    private final TransactionManager manager;

    /**
     * An ArrayList to keep track of the lots gathered from the
     * space.
     */
    private final ArrayList<IWsLot> lots;

    /**
     * The table to keep track of any new lots entered.
     */
    private final LotTable lotTable;

    /**
     * Initialize a new AuctionCard with a list of initial lots
     * to display. Provides access to the list of lots and the cards
     * parent to enable addition/removal of cards representing the
     * bid card.
     *
     * @param lots              the list of lot items
     * @param cards             the cards layout
     */
    public AuctionCard(final ArrayList<IWsLot> lots, final JPanel cards){
        super(new BorderLayout());

        this.lots = lots;

        this.manager = SpaceUtils.getManager();

        this.space = SpaceUtils.getSpace();

        JPanel fieldInputPanel = new JPanel(new GridLayout(4, 2));
        fieldInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        final JLabel itemNameLabel = new JLabel("Name of Item: ");
        final JTextField itemNameIn = new JTextField("", 12);
        final JLabel itemDescriptionLabel = new JLabel("Item description: ");
        final JTextField itemDescriptionIn = new JTextField("", 1);
        final JLabel startingPriceLabel = new JLabel("Starting Price: ");
        final JTextField startingPriceIn = new JTextField("", 6);
        final JLabel expirationLengthLabel = new JLabel("Auction Length (s): ");
        final JTextField expirationLengthIn = new JTextField("");
        final JLabel resultTextLabel = new JLabel("Result: ");
        final JResultText resultTextOut = new JResultText();

        fieldInputPanel.add(itemNameLabel);
        fieldInputPanel.add(itemNameIn);
        fieldInputPanel.add(itemDescriptionLabel);
        fieldInputPanel.add(itemDescriptionIn);
        fieldInputPanel.add(startingPriceLabel);
        fieldInputPanel.add(startingPriceIn);
//        fieldInputPanel.add(expirationLengthLabel);
//        fieldInputPanel.add(expirationLengthIn);
        fieldInputPanel.add(resultTextLabel);
        fieldInputPanel.add(resultTextOut);

        add(fieldInputPanel, BorderLayout.NORTH);

        JTextArea itemListOut = new JTextArea(30, 30);
        itemListOut.setEditable(false);

        lotTable = new LotTable(lots, cards);
        lotTable.setModel(new String[0][5], new String[] {
                "Lot ID", "Item Name", "Seller ID", "Current Price", "Status"
        });

        // Add the table to a scrolling pane
        JScrollPane itemListPanel = new JScrollPane(
                lotTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(itemListPanel, BorderLayout.CENTER);

        JPanel bidListingPanel = new JPanel();
        bidListingPanel.setLayout(new FlowLayout());

        JButton addLotButton = new JButton();
        addLotButton.setText("Add Auction Item");
        addLotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resultTextOut.setText("");

                String itemName = itemNameIn.getText();
                String itemDescription = itemDescriptionIn.getText();
                Number startingPrice = InterfaceUtils.getTextAsNumber(startingPriceIn);
                Double potentialDouble = startingPrice == null ? 0 : startingPrice.doubleValue();

                if(startingPrice == null || potentialDouble == 0){
                    resultTextOut.setText("Invalid price!");
                    return;
                }

                Transaction transaction = null;
                try {
                    Transaction.Created trc = TransactionFactory.create(manager, 3000);
                    transaction = trc.transaction;

                    IWsSecretary secretary = (IWsSecretary) space.take(new IWsSecretary(), transaction, Constants.SPACE_TIMEOUT);

                    final int lotNumber = secretary.addNewLot();
                    IWsLot newLot = new IWsLot(lotNumber, UserUtils.getCurrentUser(), null, itemName, potentialDouble, itemDescription, false);

                    space.write(newLot, transaction, Lease.FOREVER);
                    space.write(secretary, transaction, Lease.FOREVER);

                    transaction.commit();

                    resultTextOut.setText("Added Lot #" + lotNumber + "!");
                } catch(Exception e) {
                    System.err.println("Error when adding lot to the space: " + e);
                    e.printStackTrace();
                    try {
                        if(transaction != null){
                            transaction.abort();
                        }
                    } catch(Exception e2) {
                        e2.printStackTrace();
                    }
                }

            }
        });
        bidListingPanel.add(addLotButton);

        add(bidListingPanel, BorderLayout.SOUTH);

        try {
            space.notify(new IWsLot(), null, new NewLotNotifier().getListener(), Lease.FOREVER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the table model of lotTable for access outside
     * this class. This is used to populate the table initially.
     *
     * @return DefaultTableModel    the table model
     */
    public DefaultTableModel getTableModel(){
        return ((DefaultTableModel) lotTable.getModel());
    }

    /**
     * The notifier for when a new lot enters the space. This
     * contains handling for adding new lots to table, but
     * also for marking existing lots as ended, or updating
     * their current price.
     */
    private class NewLotNotifier extends GenericNotificationListener {

        /**
         * Overrides the parent notify method to allow the
         * space to notify when a new lot is added to the
         * number. This will either add the new lot to the
         * table, or will update the existing record with
         * the new information.
         *
         * @param ev        the remote event
         */
        @Override
        public void notify(RemoteEvent ev) {
            DefaultTableModel model = getTableModel();

            try {
                IWsSecretary secretary = (IWsSecretary) space.read(new IWsSecretary(), null, Constants.SPACE_TIMEOUT);
                IWsLot template = new IWsLot(secretary.getLotNumber(), null, null, null, null, null, null);
                IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);

                Object[] insertion = latestLot.asObjectArray();

                if(latestLot.hasEnded()){
                    lots.set(latestLot.getId() - 1, latestLot);
                    model.setValueAt(insertion[4], latestLot.getId() - 1, 4);
                    return;
                }

                if(latestLot.getId() > model.getRowCount()) {
                    lots.add(latestLot);
                    model.addRow(insertion);
                } else {
                    lots.set(latestLot.getId() - 1, latestLot);
                    model.setValueAt(insertion[3], latestLot.getId() - 1, 3);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
