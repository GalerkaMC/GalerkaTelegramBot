package org.justiks.telegram.whitelist_request;

import java.util.HashMap;


/**
 * whitelist requests manager. Contains all WhitelistRequests object in hashmap, and manage it
 */
public class WhitelistRequestsManager {

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
