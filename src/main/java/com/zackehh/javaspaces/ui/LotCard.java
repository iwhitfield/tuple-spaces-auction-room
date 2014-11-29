package com.zackehh.javaspaces.ui;

import com.zackehh.javaspaces.auction.IWsBid;
import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.auction.IWsSecretary;
import com.zackehh.javaspaces.util.Constants;
import com.zackehh.javaspaces.util.InterfaceUtils;
import com.zackehh.javaspaces.util.SpaceUtils;
import com.zackehh.javaspaces.util.UserUtils;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.Vector;

public class LotCard extends JPanel implements RemoteEventListener {

    private final IWsLot lot;
    private final JTable bidTable;
    private Vector<Vector<String>> bidHistory;

    private JLabel currentPrice;

    public LotCard(final JPanel cards, final IWsLot lot) {
        super();

        this.lot = lot;

        this.bidTable = new JTable();

        setLayout(new BorderLayout());

        Exporter myDefaultExporter =
                new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                        new BasicILFactory(), false, true);

        RemoteEventListener listener;
        try {
            // register this as a remote object
            // and get a reference to the 'stub'
            listener = (RemoteEventListener) myDefaultExporter.export(this);

            // add the listener
            SpaceUtils.getSpace().notify(new IWsBid(null, null, lot.getId(), null, null), null, listener, Lease.FOREVER, null);

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

        JLabel placeBid = new JLabel("Place Bid");
        placeBid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                String bidString = JOptionPane.showInputDialog(null, " Please place your bid: ", null);
                if(bidString != null){
                    Double bid;
                    if(bidString.matches(Constants.CURRENCY_REGEX) && (bid = Double.parseDouble(bidString)) > 0 && bid > lot.getCurrentPrice()){
                        try {
                            JavaSpace space = SpaceUtils.getSpace();

                            IWsSecretary secretary = (IWsSecretary) space.take(new IWsSecretary(), null, Constants.SPACE_TIMEOUT);
                            IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null);

                            // dispose of the previous lot item
                            IWsLot updatedLot = (IWsLot) space.take(template, null, Constants.SPACE_TIMEOUT);

                            int bidNumber = secretary.addBid();

                            updatedLot.bidList += "," + bidNumber;
                            updatedLot.currentPrice = bid;

                            final IWsBid newBid = new IWsBid(bidNumber, UserUtils.getCurrentUser(), lot.getId(), bid, true);

                            space.write(updatedLot, null, Lease.FOREVER);
                            space.write(newBid, null, Lease.FOREVER);
                            space.write(secretary, null, Lease.FOREVER);
                        } catch(Exception e) {
                            System.err.println("Error when adding lot to the space: " + e);
                            e.printStackTrace();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid bid entered!");
                    }
                }
            }
        });
        panel.add(placeBid, BorderLayout.EAST);

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

                Field f = c.getDeclaredField(InterfaceUtils.toCamelCase(label, " "));
                f.setAccessible(true);

                String valueOfField = f.get(lot) + "";

                JLabel textLabel = new JLabel(valueOfField);
                l.setLabelFor(textLabel);
                p.add(textLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel currentPriceLabel = new JLabel("Current Price: ", SwingConstants.RIGHT);
        currentPrice = new JLabel(
            InterfaceUtils.getDoubleAsCurrency(Double.parseDouble(lot.getCurrentPrice().toString()))
        );

        currentPriceLabel.setLabelFor(currentPrice);

        p.add(currentPriceLabel);
        p.add(currentPrice);

        add(p);

        Vector<String> columnz = new Vector<String>(){{
            add("Buyer ID");
            add("Bid Amount");
        }};

        bidHistory = InterfaceUtils.getVectorBidMatrix(lot);

        // Configure some of JTable's parameters
        bidTable.setShowHorizontalLines(true);
        bidTable.setRowSelectionAllowed(true);
        bidTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {{
            setHorizontalAlignment(JLabel.CENTER);
        }});

        bidTable.setModel(new DefaultTableModel(bidHistory, columnz) {

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

        JTableHeader tableHeader = bidTable.getTableHeader();

        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);

        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        // Add the table to a scrolling pane
        JScrollPane itemListPanel = new JScrollPane(bidTable);

        add(itemListPanel, BorderLayout.SOUTH);
    }

    @Override
    public void notify(RemoteEvent ev) {
        try {
            IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null);
            IWsLot latestLot = (IWsLot) SpaceUtils.getSpace().read(template, null, Constants.SPACE_TIMEOUT);

            IWsBid bidTemplate = new IWsBid(latestLot.getLatestBid(), null, null, null, null);
            final IWsBid latestBid = (IWsBid) SpaceUtils.getSpace().read(bidTemplate, null, Constants.SPACE_TIMEOUT);

            Vector<String> insertion = new Vector<String>(){{
                add(latestBid.getUserId());
                add(InterfaceUtils.getDoubleAsCurrency(latestBid.getMaxPrice()));
            }};

            bidHistory.add(0, insertion);
            bidTable.revalidate();
            currentPrice.setText(InterfaceUtils.getDoubleAsCurrency(latestLot.getCurrentPrice()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
