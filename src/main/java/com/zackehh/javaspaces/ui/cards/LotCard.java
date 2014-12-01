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
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Vector;

public class LotCard extends JPanel {

    private final JavaSpace space;
    private IWsLot lot;
    private final BidTable bidTable;
    private final Vector<Vector<String>> bidHistory;
    private final JLabel acceptBid, placeBid;

    private final JLabel currentPrice;

    public LotCard(final JPanel cards, final IWsLot lotForCard) {
        super();

        this.lot = lotForCard;

        this.bidTable = new BidTable();

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

        bidTable.setModel(bidHistory, new Vector<String>(){{
            add("Buyer ID");
            add("Bid Amount");
        }});

        // Add the table to a scrolling pane
        JScrollPane itemListPanel = new JScrollPane(bidTable);

        add(itemListPanel, BorderLayout.SOUTH);
    }

    private class NewBidListener extends GenericNotificationListener implements RemoteEventListener {

        public NewBidListener() throws RemoteException {
            super();
            listener = (RemoteEventListener) remoteExporter.export(this);
        }

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

    private class LotChangeListener extends GenericNotificationListener implements RemoteEventListener {

        public LotChangeListener() throws RemoteException {
            super();
            listener = (RemoteEventListener) remoteExporter.export(this);
        }

        @Override
        public void notify(RemoteEvent ev) {
            try {
                IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null);
                final IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);

                if(latestLot.hasEnded()){
                    acceptBid.setVisible(false);
                    placeBid.setVisible(false);
                }

                currentPrice.setText("ENDED");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
