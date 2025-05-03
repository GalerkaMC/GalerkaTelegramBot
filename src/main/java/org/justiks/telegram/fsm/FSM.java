package org.justiks.telegram.fsm;


import java.util.HashMap;

/**
 * Finite state machine
 * Contains all states of users
 * Singleton
 */
public class FSM {

    /**
     * instance
     */
    private final static FSM instance = new FSM();

    /**
     * States of users
     */
    private HashMap<Long, UserState> states = new HashMap<>();

    /**
     * instance getter
     *
     * @return FSM instance
     */
    public static FSM getInstance() {
        return instance;
    }

    /**
     * in singleton constructor is private
     */
    private FSM() {
    }

    /**
     * get state from states hashmap
     *
     * @param userId telegram user id
     * @return UserState
     */
    public UserState getState(Long userId) {
        return states.get(userId);
    }

    /**
     * add or edit UserState to states hashmap
     *
     * @param userId telegram user id
     * @param state  UserState
     */
    public void addState(Long userId, UserState state) {
        states.put(userId, state);
    }

    /**
     * remove state from states hashmap
     */
    public void clearState(Long userId) {
        states.remove(userId);
    }
}
