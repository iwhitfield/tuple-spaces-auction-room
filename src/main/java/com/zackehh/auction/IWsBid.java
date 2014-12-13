package com.zackehh.auction;

import com.zackehh.util.UserUtils;
import net.jini.core.entry.Entry;

import java.util.Objects;

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
    public IWsUser user;

    /**
     * Default constructor, used to match anything in the space.
     */
    public IWsBid(){ }

    /**
     * Template constructor, used to ensure all fields can be set
     * for use when matching specific fields across the space.
     *
     * @param id            the id of this bid
     * @param user          the user associated with this bid
     * @param itemId        the item id this bid is associated with
     * @param price         the price the user has bid on the item
     * @param visible       whether this bid is anonymous or not
     */
    public IWsBid(Integer id, IWsUser user, Integer itemId, Double price, Boolean visible){
        this.id = id;
        this.user = user;
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
    public IWsUser getUser() {
        return user;
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
    public Boolean isAnonymous(IWsLot lot) {
        return  lot.getId().equals(getItemId()) &&
                !isPublic() &&
                !UserUtils.getCurrentUser().getId().matches(getUser().getId()) &&
                !UserUtils.getCurrentUser().getId().matches(lot.getUser().getId());
    }

    /**
     * Setter for the userId field. This is used
     * to allow overriding the field in the case
     * of an anonymous bid.
     *
     * @param user          the id to set to
     */
    public void setUserId(IWsUser user) {
        this.user = user;
    }

    /**
     * Override Object.equals for use in testing.
     *
     * @param o             the comparison object
     * @return true         if the objects are equal
     */
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IWsBid that = (IWsBid) o;

        return  Objects.equals(this.visible, that.visible) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.price, that.price) &&
                Objects.equals(this.itemId, that.itemId) &&
                Objects.equals(this.user, that.user);
    }

}
