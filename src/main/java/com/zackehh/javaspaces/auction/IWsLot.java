package com.zackehh.javaspaces.auction;

import com.zackehh.javaspaces.util.Constants;
import com.zackehh.javaspaces.util.InterfaceUtils;
import com.zackehh.javaspaces.util.SpaceUtils;
import net.jini.core.entry.*;

public class IWsLot implements Entry {

    public Integer id;
    public Double currentPrice;
    public String bidList;
    public String itemName;
    public String itemDescription;
    public String userId;

    public IWsLot(){
        // No-op
    }

    public IWsLot(Integer id, String userId, String bidList, String itemName, Double currentPrice, String itemDescription){
        this.id = id;
        this.userId = userId;
        this.bidList = bidList;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.itemDescription = itemDescription;
    }

    public Integer getId(){
        return id;
    }

    public String getUserId(){
        return userId;
    }

    public String getBidList() { return bidList == null ? "," : bidList; }

    public String getItemName(){
        return itemName;
    }

    public Double getCurrentPrice(){
        return currentPrice;
    }

    public String getItemDescription() { return itemDescription; }

    public Integer getLatestBid() {
        String[] ids = getBidList().split(",");
        if(ids.length == 1){
            return null;
        }
        return Integer.parseInt(ids[ids.length - 1]);
    }

    public Object[] asObjectArray(){
        return new Object[]{
            id,
            itemName,
            userId,
            InterfaceUtils.getDoubleAsCurrency(currentPrice)
        };
    }
}
