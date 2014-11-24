package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

public class IWsLot implements Entry {

    private Integer id;
    private Double currentPrice;
    private String itemName;
    private String itemDescription;

    public IWsLot(){
        // No-op
    }

    public IWsLot(int id, String itemName, double currentPrice, String itemDescription){
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
