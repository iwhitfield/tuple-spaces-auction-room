package com.zackehh.javaspaces;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.auction.IWsSecretary;
import com.zackehh.javaspaces.ui.cards.AuctionCard;
import com.zackehh.javaspaces.constants.Constants;
import com.zackehh.javaspaces.util.SpaceUtils;
import com.zackehh.javaspaces.util.UserUtils;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class AuctionRoom extends JFrame {

    private final ArrayList<IWsLot> lots = new ArrayList<IWsLot>();
    private final JavaSpace space;

    private AuctionCard auctionCard;

    public static void main(String[] args) {

        String userId = JOptionPane.showInputDialog(null, " Enter your student ID or username: ", null);

        if(userId == null || userId.length() == 0){
            System.err.println("No user credentials provided!");
            System.exit(1);
        }

        SpaceUtils.setHostname(args.length == 0 ? "localhost" : args[0]);
        UserUtils.setCurrentUser(userId);

        new AuctionRoom();
    }

    public AuctionRoom() {
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
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
                        IWsLot template = new IWsLot(lots.size() + 1, null, null, null, null, null, null);
                        latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);
                        if (latestLot != null) {
                            lots.add(latestLot);
                            model.addRow(latestLot.asObjectArray());
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
}
