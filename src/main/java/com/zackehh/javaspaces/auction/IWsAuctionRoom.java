package com.zackehh.javaspaces.auction;

import com.zackehh.javaspaces.ui.AuctionCard;
import com.zackehh.javaspaces.util.Constants;
import com.zackehh.javaspaces.util.InterfaceUtils;
import com.zackehh.javaspaces.util.SpaceUtils;
import com.zackehh.javaspaces.util.UserUtils;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class IWsAuctionRoom extends JFrame {

    private ArrayList<IWsLot> lots = new ArrayList<IWsLot>();
    private JavaSpace space;

    private JPanel cards;
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

        initComponents();
        pack();
        setResizable(false);
        setVisible(true);

        new Thread(new Runnable(){

            DefaultTableModel model = auctionCard.getTableModel();

            @Override
            public void run() {

                while(true) {
                    try {
                        IWsLot template = new IWsLot(lots.size() + 1, null, null, null, null, null);
                        IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);
                        if (latestLot == null) {
                            Thread.sleep(Constants.POLLING_INTERVAL);
                        } else {
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

        auctionCard = new AuctionCard(space, lots, cards);

        cards.add(auctionCard, Constants.AUCTION_CARD);

        cp.add(cards);
    }

}
