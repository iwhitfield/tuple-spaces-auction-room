package com.zackehh.javaspaces.auction;

import net.jini.core.entry.*;

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
    public Integer bidNumber;

    /**
     * The id of the latest lot placed in the space.
     */
    public Integer lotNumber;

    /**
     * Default constructor, used to match anything in the space.
     */
    public IWsSecretary(){ }

    /**
     * Takes a value for both id trackers. This is usually set to
     * 0 as the only need to initialize a Secretary is if there is
     * none already in the space - i.e. there is no data.
     *
     * @param bidNumber     the bid to start on
     * @param lotNumber     the lot to start on
     */
    public IWsSecretary(int bidNumber, int lotNumber){
        this.bidNumber = bidNumber;
        this.lotNumber = lotNumber;
    }

    /**
     * Getter for the bidNumber property. Returns the id of the
     * latest bid to be added to the space.
     *
     * @return Integer      the bid id
     */
    public Integer getBidNumber(){
        return bidNumber;
    }

    /**
     * Getter for the lotNumber property. Returns the id of the
     * latest lot to be added to the space.
     *
     * @return Integer      the lot id
     */
    public Integer getLotNumber(){
        return lotNumber;
    }

    /**
     * Helper to increment the bidNumber value and return
     * the new value. Used when a new bid will be added
     * to the space.
     *
     * @return Integer      the new bid id
     */
    public Integer addNewBid(){
        return ++bidNumber;
    }

    /**
     * Helper to increment the lotNumber value and return
     * the new value. Used when a new lot will be added
     * to the space.
     *
     * @return Integer      the new lot id
     */
    public Integer addNewLot(){
        return ++lotNumber;
    }

}
