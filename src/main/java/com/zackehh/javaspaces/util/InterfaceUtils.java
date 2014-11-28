package com.zackehh.javaspaces.util;

import com.zackehh.javaspaces.auction.IWsBid;
import com.zackehh.javaspaces.auction.IWsLot;
import net.jini.space.JavaSpace;

import javax.swing.text.JTextComponent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class InterfaceUtils {

    /**
     * Parse a text input as a Number. Should the input not be
     * a valid Number, set errorTextOut to display a parse error.
     *
     * @param  component    the component to retrieve text of
     * @return Number       a valid Number object
     */
    public static Number getTextAsNumber(JTextComponent component){
        try {
            return NumberFormat.getInstance().parse(component.getText());
        } catch(ParseException e){
            return null;
        }
    }

    /**
     * Parse a Double to a currency formatted string.
     *
     * @param  value        the Double value to convert
     * @return Double       a valid currency string
     */
    public static String getDoubleAsCurrency(Double value){
        DecimalFormat currencyEnforcer = new DecimalFormat("0.00");
        if(value == null){
            return null;
        }
        return "£" + currencyEnforcer.format(value);
    }

    public static ArrayList<IWsBid> getBidHistory(IWsLot lot){
        JavaSpace space = SpaceUtils.getSpace();

        ArrayList<IWsBid> bidHistory = new ArrayList<IWsBid>();

        try {
            IWsLot lotTemplate = new IWsLot(lot.getId(), null, null, null, null, null);
            IWsLot refreshedLot = (IWsLot) space.read(lotTemplate, null, Constants.SPACE_TIMEOUT);

            String[] bids = refreshedLot.getBidList().split(",");

            if(bids.length <= 1){
                return bidHistory;
            }

            for(int i = 1; i < bids.length; i++){
                IWsBid template = new IWsBid(Integer.parseInt(bids[i]), null, lot.getId(), null, null);
                IWsBid bidItem = ((IWsBid) space.read(template, null, Constants.SPACE_TIMEOUT));

                if(!bidItem.isPublic() && !UserUtils.getCurrentUser().matches(bidItem.getUserId())) {
                    bidItem.setUserId("Anonymous Buyer");
                }

                bidHistory.add(bidItem);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        Collections.sort(bidHistory, new Comparator<IWsBid>() {
            @Override
            public int compare(IWsBid bid1, IWsBid bid2) {
                return bid2.getMaxPrice().compareTo(bid1.getMaxPrice());
            }
        });
        return bidHistory;
    }

    public static Vector<Vector<String>> getVectorBidMatrix(IWsLot lot){
        ArrayList<IWsBid> bids = getBidHistory(lot);

        Vector<Vector<String>> values = new Vector<Vector<String>>();

        for(int iY = 0; iY < bids.size(); iY++){
            final IWsBid bid = bids.get(iY);
            values.add(iY, new Vector<String>(){{
                add(bid.getId().toString());
                add(bid.getUserId());
                add(InterfaceUtils.getDoubleAsCurrency(bid.getMaxPrice()));
            }});
        }

        return values;
    }

    public static String[][] getBidMatrix(IWsLot lot){
        ArrayList<IWsBid> bids = getBidHistory(lot);

        String[][] dataValues = new String[bids.size()][2];

        for(int iY = 0; iY < bids.size(); iY++){
            IWsBid bid = bids.get(iY);
            dataValues[iY] = new String[] {
                bid.getId().toString(),
                bid.getUserId(),
                bid.getMaxPrice().toString()
            };
        }

        return dataValues;
    }

    /**
     * Returns a String converted to CamelCase when using
     * argument split as a delimiter for the split.
     *
     * @param  str          the string to convert
     * @param  split        the split delimiter
     * @return
     */
    public static String toCamelCase(String str, String split){
        String[] parts = str.toLowerCase().split(split);
        String camelCaseString = "";
        int i = 0;
        for (String part : parts){
            if(i++ > 0) {
                camelCaseString +=
                        part.substring(0, 1).toUpperCase() +
                                part.substring(1).toLowerCase();
            } else {
                camelCaseString += part;
            }
        }
        return camelCaseString;
    }

}
