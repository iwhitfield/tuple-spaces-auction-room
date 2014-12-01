package com.zackehh.javaspaces.auction;

import com.zackehh.javaspaces.util.UserUtils;
import net.jini.core.entry.*;

/**
 * The main Bid object to be dealt with when bidding on lots.
 * This object implements Entry to allow storage in a given JavaSpace
 * and provides constructors to provide easy retrieval from the space.
 * Ties to a user id and keeps track of whether the bid was a private
 * bid or not.
 */
public class IWsBid implements Entry {

    /**
     * Tracks whether the bid is anonymous.
     */
    public Boolean visible;

    /**
     * The unique id of the bid object.
     */
    public Integer id;

    /**
     * The price the user entered when bidding.
     */
    public Double price;

    /**
     * The item id the bid object should be associated with.
     */
    public Integer itemId;

    /**
     * The user who bid on the item.
     */
    public String userId;

    /**
     * Default constructor, used to match anything in the space.
     */
    public IWsBid(){ }

    /**
     * Template constructor, used to ensure all fields can be set
     * for use when matching specific fields across the space.
     *
     * @param id            the id of this bid
     * @param userId        the user id associated with this bid
     * @param itemId        the item id this bid is associated with
     * @param price         the price the user has bid on the item
     * @param visible       whether this bid is anonymous or not
     */
    public IWsBid(Integer id, String userId, Integer itemId, Double price, Boolean visible){
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.price = price;
        this.visible = visible;
    }

    /**
     * Getter for the id field.
     *
     * @return Integer      the bid id
     */
    public Integer getId(){
        return id;
    }

    /**
     * Getter for the userId field.
     *
     * @return String       the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Getter for the itemId field.
     *
     * @return Integer      the item id
     */
    public Integer getItemId(){
        return itemId;
    }

    /**
     * Getter for the maxPrice field.
     *
     * @return Double       the max price
     */
    public Double getPrice(){
        return price;
    }

    /**
     * Getter for the isPublic field.
     *
     * @return true         if it's a public bid
     */
    public Boolean isPublic(){
        return visible;
    }

    /**
     * Shorthand for checking if this bid should be
     * visible to the current user.
     *
     * @return true         if the bid is anonymous
     */
    public Boolean isAnonymous(IWsLot lot){
        return  !isPublic() &&
                !UserUtils.getCurrentUser().matches(getUserId()) &&
                !UserUtils.getCurrentUser().matches(lot.getUserId());
    }

    /**
     * Setter for the userId field. This is used
     * to allow overriding the field in the case
     * of an anonymous bid.
     *
     * @param userId        the id to set to
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
