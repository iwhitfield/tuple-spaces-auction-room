package com.zackehh;

import com.zackehh.auction.IWsLot;
import com.zackehh.auction.secretary.IWsBidSecretary;
import com.zackehh.auction.secretary.IWsLotSecretary;
import com.zackehh.ui.cards.AuctionCard;
import com.zackehh.util.Constants;
import com.zackehh.util.SpaceUtils;
import com.zackehh.util.UserUtils;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * The main entry into the program, which simply creates the
 * base of the UI and sets up any required fields in the
 * SpaceUtils and UserUtils classes. Also handles loading of
 * the initial objects into the main AuctionCard view, via a
 * background thread.
 */
public class AuctionRoom extends JFrame {

    /**
     * A list of lot items which are being tracked in the space.
     */
    private final ArrayList<IWsLot> lots = new ArrayList<IWsLot>();

    /**
     * The common JavaSpace instance, stored privately.
     */
    private final JavaSpace space;

    /**
     * The main AuctionCard, which is the overlay for the main
     * window of the application.
     */
    private AuctionCard auctionCard;

    /**
     * Main entry to the AuctionRoom. This prompts for a
     * user's name as a prerequisite to using the application.
     * Should this not be provided, the program will exit.
     * This is where it is defined as to which hostname we
     * will search for a JavaSpace in.
     *
     * @param args      the main program arguments
     */
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

    /**
     * Initializes a JavaSpace and ensures there is a
     * Secretary in the space, as these preconditions
     * are required to allow the user to continue. Should
     * these conditions be met, the UI will be created.
     * Existing lots are loaded in a background Thread and
     * pushed to the list of lots displayed inside the
     * AuctionCard.
     */
    public AuctionRoom() {
        space = SpaceUtils.getSpace();
        if (space == null){
            System.err.println("Failed to find the JavaSpace");
            System.exit(1);
        }

        try {
            Object o = space.read(new IWsLotSecretary(), null, 1000);
            if(o == null){
                space.write(new IWsLotSecretary(0), null, Lease.FOREVER);
            }
            o = space.read(new IWsBidSecretary(), null, 1000);
            if(o == null){
                space.write(new IWsBidSecretary(0), null, Lease.FOREVER);
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

        InitialLoadingRunnable initialLoadingRunnable = new InitialLoadingRunnable();

        new Thread(initialLoadingRunnable).start();
    }

    /**
     * Main body of UI creation. Creates main frame and
     * attaches all needed listeners. Initializes the main
     * CardLayout which will hold any cards created during
     * execution of the program.
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

    /**
     * Initial loader for the AuctionRoom. Loads a list of unfinished
     * and current Lots. This is to ensure that new users cannot access
     * lots that may have already been scheduled to be removed from the
     * space.
     */
    private class InitialLoadingRunnable implements Runnable {

        /**
         * Reads all items that are available in the space and lists
         * them in the table. Also stores inside the main list class.
         * Does not include ended or lots marked for removal, to avoid
         * potential race conditions with lots being removed as a user
         * tries to view them.
         */
        @Override
        public void run() {
            DefaultTableModel model = auctionCard.getTableModel();
            try {
                IWsLotSecretary secretary = (IWsLotSecretary) space.read(new IWsLotSecretary(), null, Constants.SPACE_TIMEOUT);
                int i = 0;
                while(i <= secretary.getItemNumber()) {
                    IWsLot template = new IWsLot(i++ + 1, null, null, null, null, null, false, false);
                    if(space.readIfExists(template, null, 1000) != null) {
                        IWsLot latestLot = (IWsLot) space.read(template, null, Constants.SPACE_TIMEOUT);
                        if (latestLot != null) {
                            lots.add(latestLot);
                            model.addRow(latestLot.asObjectArray());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
