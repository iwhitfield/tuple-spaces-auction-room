package com.zackehh.javaspaces.printer;

import net.jini.core.entry.*;

public class IWsQueueStatus implements Entry {

    // Variables
    public Integer nextJob;
    
    // No arg contructor
    public IWsQueueStatus (){

    }

    public IWsQueueStatus (int n){
        // set count to n
        nextJob = n;
    }

    public void addJob(){
	    nextJob++;
    }
}
