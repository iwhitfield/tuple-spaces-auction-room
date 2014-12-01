package com.zackehh.javaspaces.ui.listeners;

import com.zackehh.javaspaces.auction.IWsLot;
import com.zackehh.javaspaces.constants.Constants;
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
 * Created by iwhitfield on 01/12/14.
 */
public class AcceptBidListener extends MouseAdapter {

    private JLabel currentPrice;
    private IWsLot lot;
    private JavaSpace space;
    private TransactionManager manager;

    public AcceptBidListener(IWsLot lot, JLabel currentPrice){
        this.currentPrice = currentPrice;
        this.lot = lot;
        this.manager = SpaceUtils.getManager();
        this.space = SpaceUtils.getSpace();
    }

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

                IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null);
                IWsLot updatedLot = (IWsLot) space.take(template, transaction, Constants.SPACE_TIMEOUT);

                updatedLot.ended = true;

                space.write(updatedLot, transaction, Lease.FOREVER);

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
