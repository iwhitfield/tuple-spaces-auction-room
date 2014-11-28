package com.zackehh.javaspaces.util;

public class Constants {

    public static final String APPLICATION_TITLE = "Auction Room";
    public static final String AUCTION_CARD = "Auction";
    public static final String BID_CARD = "Bid";
    public static final String CURRENCY_REGEX = "(?=.)^\\$?(([1-9]" +
            "[0-9]{0,2}(,[0-9]{3})*)|[0-9]+)?(\\.[0-9]{1,2})?$";

    public static final long POLLING_INTERVAL = 5000;
    public static final long SPACE_TIMEOUT = 3000;

}
