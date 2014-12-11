package com.zackehh.javaspaces.auction;

import java.io.Serializable;

/**
 * A basic user class to contain the id of the user passed into
 * the constructor. This isn't necessarily needed, however it
 * allows for easier expansion in the case of user passwords
 * and other user based properties.
 */
public class IWsUser implements Serializable {

    /**
     * The id of the user
     */
    public final String id;

    /**
     * Set up a user with the given properties
     *
     * @param id        the user id
     */
    public IWsUser(String id){
        this.id = id;
    }

    /**
     * Getter for the id, in case of transformations
     *
     * @return String   the user id
     */
    public String getId(){
        return id;
    }

}
