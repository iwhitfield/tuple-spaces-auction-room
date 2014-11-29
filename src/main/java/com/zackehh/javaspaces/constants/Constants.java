package com.zackehh.javaspaces.constants;

/**
 * Various Constants used throughout the application. Used
 * just to keep things in sync, and removing the need to modify
 * values across a bunch of classes; rather just change it here.
 */
public final class Constants {

    /**
     * Default constructor which should not be called. All variables
     * are static and final.
     */
    private Constants(){
        throw new UnsupportedOperationException();
    }

    /**
     * The title of the application
     */
    public static final String APPLICATION_TITLE = "Auction Room";

    /**
     * The title of the main lot list card
     */
    public static final String AUCTION_CARD = "Auction";

    /**
     * The title of the card showing bid details for a lot
     */
    public static final String BID_CARD = "Bid";

    /**
     * A regular expression to validate a user-defined currency
     */
    public static final String CURRENCY_REGEX = "(?=.)^\\$?(([1-9]" +
            "[0-9]{0,2}(,[0-9]{3})*)|[0-9]+)?(\\.[0-9]{1,2})?$";

    /**
     * The default timeout for any JavaSpace related operations
     */
    public static final long SPACE_TIMEOUT = 3000;

}
