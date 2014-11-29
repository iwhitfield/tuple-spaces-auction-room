package com.zackehh.javaspaces;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.auction.IWsSecretary;
import com.zackehh.javaspaces.ui.AuctionCard;
import com.zackehh.javaspaces.constants.Constants;
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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class IWsAuctionRoom extends JFrame implements RemoteEventListener {

    private ArrayList<IWsLot> lots = new ArrayList<IWsLot>();
    private JavaSpace space;

    private AuctionCard auctionCard;

    public static void main(String[] args) {

        String userId = JOptionPane.showInputDialog(null, " Enter your student ID or username: ", null);

        if(userId == null || userId.length() == 0){
            System.err.println("No user credentials provided!");
            System.exit(1);
        }

        UserUtils.setCurrentUser(userId);

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
                space.write(new IWsSecretary(0, 0), null, Lease.FOREVER);
            }
        } catch(Exception e){
            System.err.println("Died trying to read from the space: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        Exporter myDefaultExporter =
                new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                        new BasicILFactory(), false, true);

        RemoteEventListener listener;
        try {
            // register this as a remote object
            // and get a reference to the 'stub'
            listener = (RemoteEventListener) myDefaultExporter.export(this);

            // add the listener
            space.notify(new IWsLot(), null, listener, Lease.FOREVER, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();
        pack();
        setResizable(false);
        setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                DefaultTableModel model = auctionCard.getTableModel();
                IWsLot latestLot = null;
                do {
                    try {
                        IWsLot template = new IWsLot(lots.size() + 1, null, null, null, null, null);
                        latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);
                        if (latestLot != null) {
                            lots.add(latestLot);
                            model.addRow(new Object[]{
                                    latestLot.getId(),
                                    latestLot.getItemName(),
                                    latestLot.getUserId(),
                                    InterfaceUtils.getDoubleAsCurrency(latestLot.getCurrentPrice())
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while(latestLot != null);
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

        JPanel cards = new JPanel(new CardLayout());

        auctionCard = new AuctionCard(lots, cards);

        cards.add(auctionCard, Constants.AUCTION_CARD);

        cp.add(cards);
    }

    @Override
    public void notify(RemoteEvent ev) {
        DefaultTableModel model = auctionCard.getTableModel();

        try {
            IWsSecretary secretary = (IWsSecretary) space.read(new IWsSecretary(), null, Constants.SPACE_TIMEOUT);
            IWsLot template = new IWsLot(secretary.getLotNumber(), null, null, null, null, null);
            IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);

            Object[] insertion = latestLot.asObjectArray();
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
