package org.justiks.telegram;

/**
 * only static methods that interact with paper
 * For example, add whitelist methods, transfer to server after 2fa etc.
 */
public class External {

    /**
     * add player to whitelist
     * @param nickname player nickname
     * @return auth token
     */
    public static String addToWhitelist(String nickname) {
        return "";
    }

    /**
     * transfer player to smp after complete 2fa
     * @param nickname player name
     * @return true if success else false
     */
    public static boolean transferToServer(String nickname) {
        return false;
    }

    /**
     * Checks the player and password.
     * @param nickname player minecraft nickname
     * @param password player password
     * @return If password is correct - return true, else false
     */
    public static boolean playerAuth(String nickname, String password) {
        return true;
    }

}
