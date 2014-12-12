package com.zackehh.auction.status;

import net.jini.core.entry.Entry;

public class IWsLotChange implements Entry {

    public Integer id;
    public Double price;

    public IWsLotChange(){ }

    public IWsLotChange(Integer id, Double price){
        this.id = id;
        this.price = price;
    }

    public Integer getId(){
        return id;
    }

    public Double getPrice(){
        return price;
    }

}
