package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

public class IWsBid implements Entry {

    public Boolean isPublic;
    public Integer id;
    public Double maxPrice;
    public String itemName;

    public IWsBid(){
        // No-op
    }

    public IWsBid(int id, String itemName, double maxPrice, boolean isPublic){
        this.id = id;
        this.itemName = itemName;
        this.maxPrice = maxPrice;
        this.isPublic = isPublic;
    }

    public Integer getId(){
        return id;
    }

    public String getItemName(){
        return itemName;
    }

    public Double getMaxPrice(){
        return maxPrice;
    }

    public Boolean isPublic(){
        return isPublic;
    }

}
