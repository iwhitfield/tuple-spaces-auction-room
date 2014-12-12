package com.zackehh.auction.secretary;

import net.jini.core.entry.Entry;

/**
 * A small tracker to be able to add new lots and bids
 * to the space without having to iterate through the
 * existing objects to find the new id numbers. This will
 * be pulled and written to so it becomes trivial to find
 * the id numbers to associate with new classes.
 */
public class IWsSecretary implements Entry {

    /**
     * The id of the latest bid placed in the space.
     */
    public Integer itemNumber;

    /**
     * Default constructor, used to match anything in the space.
     */
    public IWsSecretary(){ }

    /**
     * Takes a value for both id trackers. This is usually set to
     * 0 as the only need to initialize a Secretary is if there is
     * none already in the space - i.e. there is no data.
     *
     * @param itemNumber     the lot to start on
     */
    public IWsSecretary(int itemNumber){
        this.itemNumber = itemNumber;
    }

    /**
     * Getter for the bidNumber property. Returns the id of the
     * latest bid to be added to the space.
     *
     * @return Integer      the bid id
     */
    public Integer getItemNumber(){
        return itemNumber;
    }

    /**
     * Helper to increment the bidNumber value and return
     * the new value. Used when a new bid will be added
     * to the space.
     *
     * @return Integer      the new bid id
     */
    public Integer addNewItem(){
        return ++itemNumber;
    }

}
