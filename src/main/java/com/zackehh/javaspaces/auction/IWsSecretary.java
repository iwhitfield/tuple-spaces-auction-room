package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

public class IWsSecretary implements Entry {

    // Variables
    public Integer jobNumber;

    // No arg constructor
    public IWsSecretary(){

    }

    public IWsSecretary(int jobNumber){
        this.jobNumber = jobNumber;
    }

    public Integer addNewJob(){
        return ++jobNumber;
    }

    public Integer getJobNumber(){
        return jobNumber;
    }

}
