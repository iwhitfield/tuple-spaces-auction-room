package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

public class IWsLot implements Entry {

    public Integer id;
    public Double currentPrice;
    public String itemName;
    public String itemDescription;

    public IWsLot(){
        // No-op
    }

    public IWsLot(Integer id, String itemName, Double currentPrice, String itemDescription){
        this.id = id;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.itemDescription = itemDescription;
    }

    public Integer getId(){
        return id;
    }

    public String getItemName(){
        return itemName;
    }

    public Double getCurrentPrice(){
        return currentPrice;
    }

    public String getItemDescription() { return itemDescription; }

}
