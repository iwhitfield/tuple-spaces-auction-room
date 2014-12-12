package com.zackehh.auction.status;

import net.jini.core.entry.Entry;

public class IWsItemRemover implements Entry {

    public Integer id;
    public Boolean ended;
    public Boolean removed;

    public IWsItemRemover() { }

    public IWsItemRemover(Integer id, Boolean ended, Boolean removed){
        this.id = id;
        this.ended = ended;
        this.removed = removed;
    }

    public Integer getId(){
        return id;
    }

    public Boolean hasEnded(){
        return ended;
    }

    public Boolean hasBeenRemoved(){
        return removed;
    }

}
