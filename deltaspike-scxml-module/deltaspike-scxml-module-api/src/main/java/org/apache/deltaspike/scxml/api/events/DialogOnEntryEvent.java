/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api.events;

import org.apache.commons.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogOnEntryEvent {

    TransitionTarget target;

    public DialogOnEntryEvent(TransitionTarget target) {
        this.target = target;
    }

    public TransitionTarget getTarget() {
        return target;
    }
}
