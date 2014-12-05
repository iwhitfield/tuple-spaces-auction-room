package com.zackehh.javaspaces.ui.listeners;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.util.Constants;
import com.zackehh.javaspaces.util.SpaceUtils;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Listener for the LotCard views which allow a seller to
 * accept a bid for their item. Uses Transactions to ensure
 * that the bid is properly accepted in the space. This
 * does not touch the UI, which will be handled by the use
 * of notifier in the RemoteEventListener implementations.
 */
public class AcceptBidListener extends MouseAdapter {

    /**
     * The label showing the current price of the lot, which
     * by extension represents the latest bid amount.
     */
    private JLabel currentPrice;

    /**
     * The lot which would be accepting the latest bid.
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
     * Initialize a new listener from a given lot. Passes in
     * the currentPrice label which contains the latest price as
     * seen by the user. This is used to display the text inside
     * the modal dialog. Also initializes a JavaSpace and a
     * TransactionManager.
     *
     * @param lot               the lot item to accept a bid for
     * @param currentPrice      the currentPrice label
     */
    public AcceptBidListener(IWsLot lot, JLabel currentPrice){
        this.currentPrice = currentPrice;
        this.lot = lot;
        this.manager = SpaceUtils.getManager();
        this.space = SpaceUtils.getSpace();
    }

    /**
     * Creates a confirmation modal to ensure the user wishes
     * to accept the latest bid, displaying the value for the
     * user's convenience. Uses a transaction to ensure that the
     * bid is accepted, to avoid updating any internal objects
     * which would leave the application in a bad state. This does
     * not touch the UI, which is controlled by the registered
     * RemoteEventListeners.
     *
     * @param event             the mouse event
     */
    @Override
    public void mouseClicked(MouseEvent event){
        JPanel modal = new JPanel();

        modal.add(new JLabel("Are you sure you want to accept the bid of " + currentPrice.getText() + "?"));

        int result = JOptionPane.showConfirmDialog(null, modal,
                "Accept Bid?", JOptionPane.OK_CANCEL_OPTION);

        if(result == JOptionPane.OK_OPTION){
            Transaction transaction = null;
            try {
                Transaction.Created trc = TransactionFactory.create(manager, 3000);
                transaction = trc.transaction;

                IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null, false);
                IWsLot updatedLot = (IWsLot) space.take(template, transaction, Constants.SPACE_TIMEOUT);

                updatedLot.ended = true;

                space.write(updatedLot, transaction, Constants.LOT_LEASE_TIMEOUT);

                transaction.commit();

                lot = updatedLot;
            } catch(Exception e) {
                System.err.println("Error when accepting bid: " + e);
                e.printStackTrace();
                try {
                    if(transaction != null){
                        transaction.abort();
                    }
                } catch(Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

}
