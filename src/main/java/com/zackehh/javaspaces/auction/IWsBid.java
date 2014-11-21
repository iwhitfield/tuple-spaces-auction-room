package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

public class IWsBid implements Entry {

    private Boolean isPublic;
    private Integer id;
    private Integer maxPrice;
    private String itemName;

    public IWsBid(){
        // No-op
    }

    public IWsBid(int id, String itemName, int maxPrice, boolean isPublic){
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

    public Integer getMaxPrice(){
        return maxPrice;
    }

    public Boolean isPublic(){
        return isPublic;
    }

}
