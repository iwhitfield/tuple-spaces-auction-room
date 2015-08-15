package com.zackehh.ui.cards;

import com.zackehh.auction.IWsBid;
import com.zackehh.auction.IWsLot;
import com.zackehh.auction.status.IWsItemRemover;
import com.zackehh.ui.GenericNotifier;
import com.zackehh.ui.components.BaseTable;
import com.zackehh.ui.listeners.AcceptBidListener;
import com.zackehh.ui.listeners.PlaceBidListener;
import com.zackehh.ui.listeners.RemoveLotListener;
import com.zackehh.util.Constants;
import com.zackehh.util.InterfaceUtils;
import com.zackehh.util.SpaceUtils;
import com.zackehh.util.UserUtils;
import net.jini.core.event.RemoteEvent;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * The lot page which displays a list of bids associated
 * with the chosen lot. This card allows the user to place
 * a bid on an item, as well as allowing the seller to
 * accept a bid or remove the current item from the auction.
 */
public class LotCard extends JPanel {

    /**
     * The common JavaSpace instance, stored privately.
     */
    private final JavaSpace space;

    /**
     * The table of bids which will hold the the bid
     * history of the current lot.
     */
    private final BaseTable bidTable;

    /**
     * The lot that this card is associated with. This
     * will not change throughout the lifecycle of the
     * card.
     */
    private final IWsLot lot;

    /**
     * The history as reflected by the bidTable object.
     * This is updating alongside the table to allow
     * easy access to elements and easy table revalidation.
     */
    private final Vector<Vector<String>> bidHistory;

    /**
     * The label allowing the seller to accept a bid.
     */
    private final JLabel acceptBidOrRemoveLot;

    /**
     * The label displaying the current price of the lot.
     */
    private final JLabel currentPrice;

    /**
     * The label associated with the current price.
     */
    private final JLabel currentPriceLabel;

    /**
     * The label allowing the user to place a bid.
     */
    private final JLabel placeBid;

    /**
     * The main cards parent.
     */
    private final JPanel cards;

    private final AcceptBidListener acceptBidListener;

    private final RemoveLotListener removeLotListener;

    /**
     * Initializes a new card based on the given lot, which
     * allows the user to place a bid on an item and allows
     * the seller to accept a bid or remove a lot from the
     * auction. This card only exists whilst the user remains
     * on the card, it is destroyed when the user returns to
     * the main AuctionCard.
     *
     * @param cards             the parent card layout
     * @param lotForCard        the lot this card is for
     */
    public LotCard(final JPanel cards, IWsLot lotForCard) {
        super();

        // Set required fields from params
        this.cards = cards;
        this.space = SpaceUtils.getSpace();

        // Refresh the lot, in case state has changed
        IWsLot baseLot = lotForCard;
        try {
            IWsLot templateLot = new IWsLot(lotForCard.getId());
            baseLot = (IWsLot) space.read(templateLot, null, Constants.SPACE_TIMEOUT);
        } catch(Exception e){
            e.printStackTrace(); // doesn't matter, UI will handle it
        }

        // Store the last known good version of the lot
        this.lot = baseLot;

        setLayout(new BorderLayout());

        try {
            // register this as a remote object
            // and get a reference to the 'stub'
            NewBidListener bidListener = new NewBidListener();
            LotChangeListener lotListener = new LotChangeListener();

            // Generate the templates
            IWsBid bidTemplate = new IWsBid(null, null, lot.getId(), null, null);
            IWsItemRemover removerTemplate = new IWsItemRemover(lot.getId(), null, null);

            // Ensures all listeners are set to notify
            space.notify(bidTemplate, null, bidListener.getListener(), Lease.FOREVER, null);
            space.notify(removerTemplate, null, lotListener.getListener(), Lease.FOREVER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a new main panel with a BorderLayout
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // Set a back button to return to the main UI
        JLabel back = new JLabel("Back");
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                // Remove the current card
                cards.remove(LotCard.this);
            }
        });

        // Add the back button to the main frame
        panel.add(back, BorderLayout.WEST);

        // Create any needed labels
        placeBid = new JLabel("Place Bid");
        acceptBidOrRemoveLot = new JLabel("Accept Latest Bid");
        currentPrice = new JLabel();

        // Setup the removal and accept bid listeners
        acceptBidListener = new AcceptBidListener(lot, currentPrice);
        removeLotListener = new RemoveLotListener(lot);

        // Ensure a lot has not ended (this should always be true)
        if(!lot.hasEnded()) {

            // If the user is the Seller of the lot
            if (UserUtils.getCurrentUser().equals(lot.getUser())) {

                // If there are no bids, the Seller can remove the lot
                if(lot.getLatestBid() == null){
                    // Set the new text and add a removal listener
                    acceptBidOrRemoveLot.setText("Remove Lot");
                    acceptBidOrRemoveLot.addMouseListener(removeLotListener);
                } else {
                    // Add a listener to accept the last bid
                    acceptBidOrRemoveLot.addMouseListener(acceptBidListener);
                }

                // Add the label to the main frame
                panel.add(acceptBidOrRemoveLot, BorderLayout.EAST);

            } else {
                // Allow the user to place a bid if desired
                placeBid.addMouseListener(new PlaceBidListener(lot));
                panel.add(placeBid, BorderLayout.EAST);
            }
        }

        // Add the upper panel to the UI
        add(panel, BorderLayout.NORTH);

        // The fields we're displaying information for
        String[] labels = {
            "ID",
            "User ID",
            "Item Name",
            "Item Description"
        };

        // Create a GridLayout matching the above labels length
        JPanel p = new JPanel(new GridLayout(labels.length + 1, 2));
        p.setBorder(BorderFactory.createEmptyBorder(-8, 0, 10, 0));

        try {
            // For each label, call the corresponding getter in the
            // IWsLot class. All methods are camelCase forms of the
            // above labels, so this is a simple way of dynamically
            // adding new fields to the UI without having to rewrite
            // the UI components.
            for (String label : labels) {
                // Add the new label to the left side of the panel
                JLabel l = new JLabel(label + ": ", SwingConstants.RIGHT);
                p.add(l);

                // Find the corresponding method for the label
                Class<?> c = lot.getClass();
                Method method = c.getMethod(InterfaceUtils.toCamelCase("get " + label, " "));

                // Get the string value of the returned value
                String valueOfField = method.invoke(lot) + "";

                // Add a label with the value against the field label
                JLabel textLabel = new JLabel(valueOfField);
                l.setLabelFor(textLabel);
                p.add(textLabel);
            }
        } catch (Exception e) {
            // Will never happen, because we control the fields and methods
        }

        // Grab the history of IWsBid from the Space
        bidHistory = InterfaceUtils.getVectorBidMatrix(lot);

        // Behaviour changes based on active lots
        if(lot.hasEnded()){
            // Display the winner and the price the item was won for
            currentPriceLabel = new JLabel("Won by " + bidHistory.get(0).get(0) + " -", SwingConstants.RIGHT);
            currentPrice.setText(" Price: " + InterfaceUtils.getDoubleAsCurrency(lot.getCurrentPrice()));
        } else {
            // Display the current price of the item
            currentPriceLabel = new JLabel("Current Price: ", SwingConstants.RIGHT);
            currentPrice.setText(InterfaceUtils.getDoubleAsCurrency(lot.getCurrentPrice()));
        }

        // Add the Current Price labels to the panel
        currentPriceLabel.setLabelFor(currentPrice);
        p.add(currentPriceLabel);
        p.add(currentPrice);

        // Add the panel to the frame
        add(p);

        // Create a new BaseTable with two columns
        bidTable = new BaseTable(bidHistory, new Vector<String>(){{
            add("Buyer ID");
            add("Bid Amount");
        }});

        // Add the table to a scrolling pane
        JScrollPane itemListPanel = new JScrollPane(bidTable);

        // Add the scrolling pane to the UI
        add(itemListPanel, BorderLayout.SOUTH);
    }

    /**
     * Provides a way to update the UI based on new bids added
     * to the lot. This will add a new bid to the table, as well
     * as update the current price. This will also enable the
     * seller to now accept the latest bid if it is the first
     * bid added to the lot.
     */
    private class NewBidListener extends GenericNotifier {

        /**
         * Updates the UI with the new bid added to the lot.
         * This will keep all connected clients in sync with
         * each other without the need for manual polling.
         *
         * @param ev        the remote event
         */
        @Override
        public void notify(RemoteEvent ev) {
            try {
                // Grab the latest version of the current lot and the latest bid from the Space
                final IWsLot latestLot = (IWsLot) space.read(new IWsLot(lot.getId()), null, Constants.SPACE_TIMEOUT);
                final IWsBid latestBid = (IWsBid) space.read(new IWsBid(latestLot.getLatestBid()), null, Constants.SPACE_TIMEOUT);

                // Format the lot for the BaseTable
                Vector<String> insertion = new Vector<String>(){{
                    add(latestBid.isAnonymous(latestLot) ? "Anonymous User" : latestBid.getUser().getId());
                    add(InterfaceUtils.getDoubleAsCurrency(latestBid.getPrice()));
                }};

                // If there is a latest bid
                if(latestLot.getLatestBid() != null && UserUtils.getCurrentUser().equals(lot.getUser())){
                    // Allow the Seller to now accept the bids instead of remove them
                    acceptBidOrRemoveLot.setText("Accept Latest Bid");
                    acceptBidOrRemoveLot.addMouseListener(acceptBidListener);
                    acceptBidOrRemoveLot.removeMouseListener(removeLotListener);
                }

                // Add the bid to the top of the table
                bidHistory.add(0, insertion);

                // Redraw the table to ensure up to date
                bidTable.revalidate();

                // Set the new price to the Current Price label
                currentPrice.setText(InterfaceUtils.getDoubleAsCurrency(latestLot.getCurrentPrice()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Listens for changes to any lot items and disables
     * any actions which should be blocked after an auction
     * has ended. This is to ensure ended lots are updated
     * across all connected clients and stay in sync.
     */
    private class LotChangeListener extends GenericNotifier {

        /**
         * Fetches the latest version of the lot from the space
         * when notified and ensures that the auction has not
         * been closed by the seller. This will also ensure that
         * if a lot has been removed, it is reflected as such to
         * the user.
         *
         * @param ev        the remote event
         */
        @Override
        public void notify(RemoteEvent ev) {
            try {
                // Read the latest IWsItemRemover from the Space (there should only be the one we want)
                final IWsItemRemover remover = (IWsItemRemover) space.read(new IWsItemRemover(lot.getId()), null, Constants.SPACE_TIMEOUT);

                // If it was removed due to being won
                if(remover.hasEnded()){
                    // Grab the winning bid from the table
                    Vector<String> winningBid = bidHistory.get(0);

                    // Grab the winning user and the winning price
                    String winningId = winningBid.get(0);
                    String winningPrice = winningBid.get(1);

                    // Remove the ability to remove lot and accept/place bid
                    acceptBidOrRemoveLot.setVisible(false);
                    placeBid.setVisible(false);

                    // Set the winning labels
                    currentPriceLabel.setText("Won by " + winningId + " -");
                    currentPrice.setText(" Price: " + winningPrice);

                    // Short circuit
                    return;
                }

                // If the Seller removed the item
                if(remover.hasBeenRemoved()){
                    // Prompt that the lot was removed
                    JOptionPane.showMessageDialog(null, "This lot has been removed!");
                    // Return to the main UI
                    cards.remove(LotCard.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
