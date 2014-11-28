package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

public class IWsBid implements Entry {

    public Boolean isPublic;
    public Integer id;
    public Double maxPrice;
    public Integer itemId;
    public String userId;

    public IWsBid(){
        // No-op
    }

    public IWsBid(Integer id, String userId, Integer itemId, Double maxPrice, Boolean isPublic){
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.maxPrice = maxPrice;
        this.isPublic = isPublic;
    }

    public Integer getId(){
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getItemId(){
        return itemId;
    }

    public Double getMaxPrice(){
        return maxPrice;
    }

    public Boolean isPublic(){
        return isPublic;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setIsPublic(Boolean isPublic){
        this.isPublic = isPublic;
    }

}
