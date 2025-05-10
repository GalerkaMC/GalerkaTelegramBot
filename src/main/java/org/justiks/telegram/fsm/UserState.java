package org.justiks.telegram.fsm;

import org.justiks.telegram.fsm.states.State;

import java.util.HashMap;

/**
 * contains all info of user state in fsm
 */
public class UserState {
    /**
     * contains data from dialog
     */
    private final HashMap<State, Object> values = new HashMap<>();

    /**
     * current user state in fsm
     */
    public State currentState;

    /**
     * state constructor
     *
     * @param firstState init state for user
     */
    public UserState(State firstState) {
        currentState = firstState;

        // create keys in values hashmap. Keys are values from currentState enum
        State temproraryState = firstState;
        while (temproraryState != null) {
            values.put(temproraryState, null);
            temproraryState = temproraryState.getNext();
        }
    }

    /**
     * set value for current state in values hashmap, and set next state
     *
     * @param value value of current state
     */
    public void setValueForCurrentState(Object value) {
        values.put(currentState, value);
        currentState = currentState.getNext();
    }

    /**
     * get value from values by state. private set public get
     * @param state key of value
     */
    public Object getValueByState(State state) {
        return values.get(state);
    }


}
