package com.zackehh.javaspaces.ui.listeners;

import com.zackehh.javaspaces.auction.IWsBid;
import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.auction.IWsSecretary;
import com.zackehh.javaspaces.constants.Constants;
import com.zackehh.javaspaces.util.SpaceUtils;
import com.zackehh.javaspaces.util.UserUtils;
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
        // TODO: something?
        System.out.println("We'll figure this out later...");
    }

}
