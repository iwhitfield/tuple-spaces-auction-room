package com.zackehh.javaspaces.auction;

import com.zackehh.javaspaces.util.InterfaceUtils;
import net.jini.core.entry.*;

/**
 * The main Lot object to be dealt with when listing the items
 * available in the auction. This object implements Entry to allow
 * storage in a given JavaSpace and provides constructors to provide
 * easy retrieval from the space. Ties to a user id so we know who
 * is eligible to accept a given bid.
 */
public class IWsLot implements Entry {

    /**
     * The unique id of this lot item.
     */
    public Integer id;

    /**
     * The current price of this item.
     */
    public Double price;

    /**
     * The list of bid ids (in order) which have been
     * associated with this item.
     */
    public String history;

    /**
     * The name of this item.
     */
    public String name;

    /**
     * The description of this item.
     */
    public String description;

    /**
     * The user id of the seller of this item.
     */
    public String userId;

    /**
     * Default constructor, used to match anything in the space.
     */
    public IWsLot(){ }

    /**
     * Templating constructor, used to ensure all fields can be set
     * for use when matching specific fields across the space.
     *
     * @param id            the id of this item
     * @param userId        the user id who created this item
     * @param history       a list of historical bids
     * @param name          the name of the item
     * @param price         the current price of the item
     * @param description   a short description of the item
     */
    public IWsLot(Integer id, String userId, String history, String name, Double price, String description){
        this.id = id;
        this.userId = userId;
        this.history = history;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    /**
     * Getter for the id of the lot.
     *
     * @return Integer      the numeric id
     */
    public Integer getId(){
        return id;
    }

    /**
     * Getter for the user id of the lot.
     *
     * @return String       the user id
     */
    public String getUserId(){
        return userId;
    }

    /**
     * Getter for a history list of the lot. If there
     * is no history, returns a default value.
     *
     * @return String       the history of bids
     */
    public String getHistory() { return history == null ? "," : history; }

    /**
     * Getter for the name of the item in the lot.
     *
     * @return String       the item name
     */
    public String getItemName(){
        return name;
    }

    /**
     * Getter for the description of the item in the lot.
     *
     * @return String       the item description
     */
    public String getItemDescription() { return description; }

    /**
     * Getter for the current price of the lot.
     *
     * @return Double       the lot price
     */
    public Double getCurrentPrice(){
        return price;
    }

    /**
     * Helper to return the last bid associated with the lot.
     * This uses the bid history to return the numeric id of
     * the latest bid.
     *
     * @return Integer      the latest bid id
     */
    public Integer getLatestBid() {
        String[] ids = getHistory().split(",");
        if(ids.length == 1){
            return null;
        }
        return Integer.parseInt(ids[ids.length - 1]);
    }

    /**
     * Helper to convert the lot to a row of data, which is
     * then used to populate the table of lots in the main
     * views.
     *
     * @return Object[]     the lot as a data row
     */
    public Object[] asObjectArray(){
        return new Object[]{
            id,
            name,
            userId,
            InterfaceUtils.getDoubleAsCurrency(price)
        };
    }
}
