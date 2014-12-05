package com.zackehh.javaspaces.ui.listeners;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.util.Constants;
import com.zackehh.javaspaces.util.SpaceUtils;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

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
public class RemoveLotListener extends MouseAdapter {

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
    public RemoveLotListener(IWsLot lot){
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
        super.mouseClicked(event);
        Transaction transaction = null;
        try {
            Transaction.Created trc = TransactionFactory.create(manager, 3000);
            transaction = trc.transaction;

            IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null, null);
            IWsLot updatedLot = (IWsLot) space.take(template, transaction, Constants.SPACE_TIMEOUT);

            updatedLot.markedForRemoval = true;

            space.write(updatedLot, transaction, Constants.LOT_LEASE_TIMEOUT);

            transaction.commit();

            lot = updatedLot;
        } catch(Exception e) {
            System.err.println("Error when removing bid: " + e);
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
