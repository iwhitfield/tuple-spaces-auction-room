package com.zackehh.javaspaces.ui.cards;

import com.zackehh.javaspaces.auction.IWsBid;
import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.constants.Constants;
import com.zackehh.javaspaces.ui.components.tables.BidTable;
import com.zackehh.javaspaces.ui.listeners.AcceptBidListener;
import com.zackehh.javaspaces.ui.listeners.GenericNotificationListener;
import com.zackehh.javaspaces.ui.listeners.PlaceBidListener;
import com.zackehh.javaspaces.util.InterfaceUtils;
import com.zackehh.javaspaces.util.SpaceUtils;
import com.zackehh.javaspaces.util.UserUtils;
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
    private final BidTable bidTable;

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
    private final JLabel acceptBid;

    /**
     * The label displaying the current price of the lot.
     */
    private final JLabel currentPrice;

    /**
     * The label allowing the user to place a bid.
     */
    private final JLabel placeBid;

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

        this.lot = lotForCard;

        this.space = SpaceUtils.getSpace();

        setLayout(new BorderLayout());

        try {
            // register this as a remote object
            // and get a reference to the 'stub'
            NewBidListener bidListener = new NewBidListener();
            LotChangeListener lotListener = new LotChangeListener();

            // generate the templates
            IWsBid bidTemplate = new IWsBid(null, null, lot.getId(), null, null);
            IWsLot lotTemplate = new IWsLot(lot.getId(), null, null, null, null, null, null);

            // add the listener
            space.notify(bidTemplate, null, bidListener.getListener(), Lease.FOREVER, null);
            space.notify(lotTemplate, null, lotListener.getListener(), Lease.FOREVER, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        currentPrice = new JLabel();

        JLabel back = new JLabel("Back");
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                cards.remove(LotCard.this);
            }
        });

        panel.add(back, BorderLayout.WEST);

        placeBid = new JLabel("Place Bid");
        acceptBid = new JLabel("Accept Latest Bid");

        if(UserUtils.getCurrentUser().matches(lot.getUserId()) && !lot.hasEnded()){
            acceptBid.addMouseListener(new AcceptBidListener(lot, currentPrice));
            if(lot.getLatestBid() == null){
                acceptBid.setVisible(false);
            }
            panel.add(acceptBid, BorderLayout.EAST);

            // TODO: Remove item
        } else {
            placeBid.addMouseListener(new PlaceBidListener(lot));
            panel.add(placeBid, BorderLayout.EAST);
        }

        add(panel, BorderLayout.NORTH);

        String[] labels = {
            "ID",
            "User ID",
            "Item Name",
            "Item Description"
        };

        int numPairs = labels.length;

        JPanel p = new JPanel(new GridLayout(numPairs + 1, 2));
        p.setBorder(BorderFactory.createEmptyBorder(-8, 0, 10, 0));

        try {
            for (String label : labels) {
                JLabel l = new JLabel(label + ": ", SwingConstants.RIGHT);
                p.add(l);
                Class<?> c = lot.getClass();

                Method method = c.getMethod(InterfaceUtils.toCamelCase("get " + label, " "));

                String valueOfField = method.invoke(lot) + "";

                JLabel textLabel = new JLabel(valueOfField);
                l.setLabelFor(textLabel);
                p.add(textLabel);
            }
        } catch (Exception e) {
            // will never happen
        }

        bidHistory = InterfaceUtils.getVectorBidMatrix(lot);

        if(lot.hasEnded()){
            JLabel currentPriceLabel = new JLabel("Won by " + bidHistory.get(bidHistory.size() - 1).get(0) +
                    " for " + InterfaceUtils.getDoubleAsCurrency(Double.parseDouble(lot.getCurrentPrice().toString())),
                    SwingConstants.CENTER);
            p.add(currentPriceLabel);
        } else {
            JLabel currentPriceLabel = new JLabel("Current Price: ", SwingConstants.RIGHT);
            currentPrice.setText(InterfaceUtils.getDoubleAsCurrency(Double.parseDouble(lot.getCurrentPrice().toString())));

            currentPriceLabel.setLabelFor(currentPrice);

            p.add(currentPriceLabel);
            p.add(currentPrice);
        }

        add(p);

        bidTable = new BidTable();
        bidTable.setModel(bidHistory, new Vector<String>(){{
            add("Buyer ID");
            add("Bid Amount");
        }});

        // Add the table to a scrolling pane
        JScrollPane itemListPanel = new JScrollPane(bidTable);

        add(itemListPanel, BorderLayout.SOUTH);
    }

    /**
     * Provides a way to update the UI based on new bids added
     * to the lot. This will add a new bid to the table, as well
     * as update the current price. This will also enable the
     * seller to now accept the latest bid if it is the first
     * bid added to the lot.
     */
    private class NewBidListener extends GenericNotificationListener {

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
                IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null);
                final IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);

                IWsBid bidTemplate = new IWsBid(latestLot.getLatestBid(), null, null, null, null);
                final IWsBid latestBid = (IWsBid) space.read(bidTemplate, null, Constants.SPACE_TIMEOUT);

                Vector<String> insertion = new Vector<String>(){{
                    add(latestBid.isAnonymous(latestLot) ? "Anonymous User" : latestBid.getUserId());
                    add(InterfaceUtils.getDoubleAsCurrency(latestBid.getPrice()));
                }};

                if(!acceptBid.isVisible() && !latestLot.hasEnded()){
                    acceptBid.setVisible(true);
                }

                bidHistory.add(0, insertion);
                bidTable.revalidate();
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
    private class LotChangeListener extends GenericNotificationListener {

        /**
         * Fetches the latest version of the lot from the space
         * when notified and ensures that the auction has not
         * been closed by the seller.
         *
         * @param ev        the remote event
         */
        @Override
        public void notify(RemoteEvent ev) {
            try {
                IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null);
                final IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);
                if(latestLot.hasEnded()){
                    acceptBid.setVisible(false);
                    placeBid.setVisible(false);
                    // TODO: Better handling here
                    currentPrice.setText("ENDED");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
