package com.zackehh.javaspaces.printer;

import net.jini.core.entry.*;

public class IWsQueueItem implements Entry {

    public Integer jobNumber;
    public Integer jobPriority;
    public String filename;

    public IWsQueueItem (){
        // Noop
    }

    public IWsQueueItem(int priority){
        jobPriority = priority;
    }

    public IWsQueueItem (int job, String fn){
        jobNumber = job;
        filename = fn;
        jobPriority = 1;
    }

    public IWsQueueItem(int job, String fn, int priority){
        jobNumber = job;
        filename = fn;
        jobPriority = priority;
    }
}
