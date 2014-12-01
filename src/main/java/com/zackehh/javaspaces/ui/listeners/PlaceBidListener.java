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

public class PlaceBidListener extends MouseAdapter {

    private IWsLot lot;
    private JavaSpace space;
    private TransactionManager manager;

    public PlaceBidListener(IWsLot lot){
        this.lot = lot;
        this.manager = SpaceUtils.getManager();
        this.space = SpaceUtils.getSpace();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        JPanel modal = new JPanel(new GridLayout(2, 2));
        JTextField bidEntry = new JTextField();
        JCheckBox privateCheckBox = new JCheckBox();

        modal.add(new JLabel("Bid Amount: "));
        modal.add(bidEntry);
        modal.add(new JLabel("Private Bid? "));
        modal.add(privateCheckBox);

        int result = JOptionPane.showConfirmDialog(null, modal,
                "Please enter your bid details:", JOptionPane.OK_CANCEL_OPTION);

        if(result == JOptionPane.OK_OPTION){
            String bidString = bidEntry.getText();
            Double bid;
            if(bidString.matches(Constants.CURRENCY_REGEX) && (bid = Double.parseDouble(bidString)) > 0 && bid > lot.getCurrentPrice()){
                Transaction transaction = null;
                try {
                    Transaction.Created trc = TransactionFactory.create(manager, 3000);
                    transaction = trc.transaction;

                    IWsSecretary secretary = (IWsSecretary) space.take(new IWsSecretary(), transaction, Constants.SPACE_TIMEOUT);
                    IWsLot template = new IWsLot(lot.getId(), null, null, null, null, null, null);

                    // dispose of the previous lot item
                    IWsLot updatedLot = (IWsLot) space.take(template, transaction, Constants.SPACE_TIMEOUT);

                    int bidNumber = secretary.addNewBid();

                    updatedLot.history += "," + bidNumber;
                    updatedLot.price = bid;

                    final IWsBid newBid = new IWsBid(bidNumber, UserUtils.getCurrentUser(), lot.getId(), bid, !privateCheckBox.isSelected());

                    space.write(updatedLot, transaction, Lease.FOREVER);
                    space.write(newBid, transaction, Lease.FOREVER);
                    space.write(secretary, transaction, Lease.FOREVER);

                    transaction.commit();

                    lot = updatedLot;
                } catch(Exception e) {
                    System.err.println("Error when adding lot to the space: " + e);
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
                JOptionPane.showMessageDialog(null, "Invalid bid entered!");
            }
        }
    }

}
