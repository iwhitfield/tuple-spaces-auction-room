package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

public class IWsSecretary implements Entry {

    // Variables
    public Integer bidNumber;
    public Integer jobNumber;

    // No arg constructor
    public IWsSecretary(){

    }

    public IWsSecretary(int bidNumber, int jobNumber){
        this.bidNumber = bidNumber;
        this.jobNumber = jobNumber;
    }

    public Integer addNewJob(){
        return ++jobNumber;
    }

    public Integer addBid(){
        return ++bidNumber;
    }

    public Integer getJobNumber(){
        return jobNumber;
    }

    public Integer getBidNumber(){
        return bidNumber;
    }

}
