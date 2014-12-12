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
    private final JLabel acceptBid;

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

        this.cards = cards;

        this.space = SpaceUtils.getSpace();

        IWsLot baseLot = lotForCard;
        try {
            IWsLot templateLot = new IWsLot(lotForCard.getId(), null, null, null, null, null, null, null);
            baseLot = (IWsLot) space.read(templateLot, null, Constants.SPACE_TIMEOUT);
        } catch(Exception e){
            e.printStackTrace();
        }

        this.lot = baseLot;

        setLayout(new BorderLayout());

        try {
            // register this as a remote object
            // and get a reference to the 'stub'
            NewBidListener bidListener = new NewBidListener();
            LotChangeListener lotListener = new LotChangeListener();

            // generate the templates
            IWsBid bidTemplate = new IWsBid(null, null, lot.getId(), null, null);
            IWsItemRemover removerTemplate = new IWsItemRemover(lot.getId(), null, null);

            // add the listener
            space.notify(bidTemplate, null, bidListener.getListener(), Lease.FOREVER, null);
            space.notify(removerTemplate, null, lotListener.getListener(), Lease.FOREVER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

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
        currentPrice = new JLabel();

        if(!lot.hasEnded()) {
            if (UserUtils.getCurrentUser().getId().matches(lot.getUser().getId())) {
                if(lot.getLatestBid() == null){
                    acceptBid.setText("Remove Lot");
                    acceptBid.addMouseListener(new RemoveLotListener(lot));
                } else {
                    acceptBid.addMouseListener(new AcceptBidListener(lot, currentPrice));
                }
                panel.add(acceptBid, BorderLayout.EAST);
            } else {
                placeBid.addMouseListener(new PlaceBidListener(lot));
                panel.add(placeBid, BorderLayout.EAST);
            }
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
            currentPriceLabel = new JLabel("Won by " + bidHistory.get(0).get(0) + " -", SwingConstants.RIGHT);
            currentPrice.setText(" Price: " + InterfaceUtils.getDoubleAsCurrency(lot.getCurrentPrice()));
        } else {
            currentPriceLabel = new JLabel("Current Price: ", SwingConstants.RIGHT);
            currentPrice.setText(InterfaceUtils.getDoubleAsCurrency(lot.getCurrentPrice()));
        }

        currentPriceLabel.setLabelFor(currentPrice);
        p.add(currentPriceLabel);
        p.add(currentPrice);

        add(p);

        bidTable = new BaseTable(bidHistory, new Vector<String>(){{
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
                IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null, null);
                final IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);

                IWsBid bidTemplate = new IWsBid(latestLot.getLatestBid(), null, null, null, null);
                final IWsBid latestBid = (IWsBid) space.read(bidTemplate, null, Constants.SPACE_TIMEOUT);

                Vector<String> insertion = new Vector<String>(){{
                    add(latestBid.isAnonymous(latestLot) ? "Anonymous User" : latestBid.getUser().getId());
                    add(InterfaceUtils.getDoubleAsCurrency(latestBid.getPrice()));
                }};

                if(latestLot.getLatestBid() != null){
                    acceptBid.setText("Accept Latest Bid");
                    acceptBid.addMouseListener(new AcceptBidListener(lot, currentPrice));
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
                IWsItemRemover template = new IWsItemRemover(lot.getId(), null, null);
                final IWsItemRemover remover = (IWsItemRemover) space.read(template, null, Constants.SPACE_TIMEOUT);
                if(remover.ended){
                    Vector<String> winningBid = bidHistory.get(0);
                    String winningId = winningBid.get(0);
                    String winningPrice = winningBid.get(1);

                    acceptBid.setVisible(false);
                    placeBid.setVisible(false);
                    currentPriceLabel.setText("Won by " + winningId + " -");
                    currentPrice.setText(" Price: " + winningPrice);

                    return;
                }
                if(remover.removed){
                    JOptionPane.showMessageDialog(null, "This lot has been removed!");
                    cards.remove(LotCard.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
