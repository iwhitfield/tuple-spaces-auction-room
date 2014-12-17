package com.zackehh.ui.listeners;

import com.zackehh.auction.IWsBid;
import com.zackehh.auction.IWsLot;
import com.zackehh.auction.secretary.IWsBidSecretary;
import com.zackehh.auction.status.IWsLotChange;
import com.zackehh.util.Constants;
import com.zackehh.util.SpaceUtils;
import com.zackehh.util.UserUtils;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Listener for the LotCard views which allow a buyer to
 * place a bid on an item. Uses Transactions to ensure
 * that the bid is properly accepted in the space. This
 * does not touch the UI, which will be handled by the use
 * of notifier in the RemoteEventListener implementations.
 *
 */
public class PlaceBidListener extends MouseAdapter {

    /**
     * The lot the user wishes to place a bid on.
     */
    private IWsLot lot;

    /**
     * The common JavaSpace instance, stored privately.
     */
    private JavaSpace space;

    /**
     * The common TransactionManager instance, stored privately.
     */
    private TransactionManager manager;

    /**
     * Initializes with a lot item, which is used when updating
     * the space with the new associated bid identifiers.
     *
     * @param lot       the lot being bid on
     */
    public PlaceBidListener(IWsLot lot){
        this.lot = lot;
        this.manager = SpaceUtils.getManager();
        this.space = SpaceUtils.getSpace();
    }

    /**
     * Creates a modal for the user to input their bid amount and
     * select whether they wish their bid to be private or public.
     * Uses a transaction to ensure that the bid is registered in the
     * space along with any other changes needed, such as lot changes.
     * This does not touch the UI, which is controlled by the registered
     * RemoteEventListeners.
     *
     * @param event             the mouse event
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        // Create base UI components
        JPanel modal = new JPanel(new GridLayout(2, 2));
        JTextField bidEntry = new JTextField();
        JCheckBox privateCheckBox = new JCheckBox();

        // Add components to modal dialog
        modal.add(new JLabel("Bid Amount: "));
        modal.add(bidEntry);
        modal.add(new JLabel("Private Bid? "));
        modal.add(privateCheckBox);

        // Store user response to dialog
        int result = JOptionPane.showConfirmDialog(null, modal,
                "Please enter your bid details:", JOptionPane.OK_CANCEL_OPTION);

        // If user accepts dialog
        if(result == JOptionPane.OK_OPTION){

            // Get entered price value
            Double bid;
            String bidString = bidEntry.getText();

            // If entered amount if a valid currency value and is higher than the last known bid
            if(bidString.matches(Constants.CURRENCY_REGEX) && (bid = Double.parseDouble(bidString)) > 0 && bid > lot.getCurrentPrice()){
                Transaction transaction = null;
                try {
                    // Create a new Transaction
                    Transaction.Created trc = TransactionFactory.create(manager, 3000);
                    transaction = trc.transaction;

                    // Refresh the secretary and the lot form the space
                    IWsBidSecretary secretary = (IWsBidSecretary) space.take(new IWsBidSecretary(), transaction, Constants.SPACE_TIMEOUT);
                    // dispose of the previous lot item
                    IWsLot updatedLot = (IWsLot) space.take(new IWsLot(lot.getId()), transaction, Constants.SPACE_TIMEOUT);

                    // Get the next bid id value
                    int bidNumber = secretary.addNewItem();

                    // Add the new fields to the lot
                    updatedLot.getHistory().add(bidNumber);
                    updatedLot.setPrice(bid);

                    // Create a new bid with the new values
                    final IWsBid newBid = new IWsBid(bidNumber, UserUtils.getCurrentUser(), lot.getId(), bid, !privateCheckBox.isSelected());

                    // Write all values back to the space
                    space.write(new IWsLotChange(lot.getId(), bid), transaction, Constants.TEMP_OBJECT);
                    space.write(updatedLot, transaction, Constants.LOT_LEASE_TIMEOUT);
                    space.write(newBid, transaction, Constants.BID_LEASE_TIMEOUT);
                    space.write(secretary, transaction, Lease.FOREVER);

                    // Commit transaction
                    transaction.commit();

                    // Store change locally, in case of Space failure
                    lot = updatedLot;
                } catch(Exception e) {
                    e.printStackTrace();
                    try {
                        if(transaction != null){
                            transaction.abort();
                        }
                    } catch(Exception e2) {
                        e2.printStackTrace();
                    }
                }
            } else {
                // Record invalid bid
                JOptionPane.showMessageDialog(null, "Invalid bid entered!");
            }
        }
    }

}
