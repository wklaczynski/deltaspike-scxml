/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api.events;

import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogOnTransitionEvent {

    TransitionTarget from;
    TransitionTarget to;
    Transition transition;

    public DialogOnTransitionEvent(TransitionTarget from, TransitionTarget to, Transition transition) {
        this.from = from;
        this.to = to;
        this.transition = transition;
    }

    public TransitionTarget getFrom() {
        return from;
    }

    public TransitionTarget getTo() {
        return to;
    }

    public Transition getTransition() {
        return transition;
    }
        
        
}
