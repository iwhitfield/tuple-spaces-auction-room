package com.zackehh.javaspaces.printer;

import net.jini.core.entry.*;

public class IWsQueueItem implements Entry {

    // Variables
    public Integer jobNumber;
    public String filename;
    
    // No arg contructor
    public IWsQueueItem (){

    }
    
    // Arg constructor
    public IWsQueueItem (int job, String fn){
        jobNumber = job;
        filename = fn;
    }
}
