package org.justiks.telegram.whitelist_request;

import java.util.HashMap;


/**
 * whitelist requests manager. Contains all WhitelistRequests object in hashmap, and manage it
 * singleton
 */
public class WhitelistRequestsManager {


    /**
     * singleton instance
     */
    private static final WhitelistRequestsManager instance = new WhitelistRequestsManager();

    /**
     * instance getter
     * @return instance of WhitelistRequestsManager
     */
    public static WhitelistRequestsManager getInstance() {return instance;}


    /**
     * hashmap with all WhitelistRequest objects
     */
    private final HashMap<Long, WhitelistRequest> whitelistRequests = new HashMap<>();

    public void add(WhitelistRequest whitelistRequest) {
        whitelistRequests.put(whitelistRequest.telegramUserId, whitelistRequest);
    }

    public void remove(Long telegramUserId) {
        whitelistRequests.remove(telegramUserId);
    }

    public WhitelistRequest get(Long telegramUserId) {
        return whitelistRequests.get(telegramUserId);
    }


}
