/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api.events;

import org.apache.commons.scxml.model.State;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogOnStopEvent {
    State state;

    public DialogOnStopEvent(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }
    
}
