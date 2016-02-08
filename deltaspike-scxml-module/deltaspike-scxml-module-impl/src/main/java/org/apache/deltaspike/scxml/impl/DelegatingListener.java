/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.io.Serializable;
import javax.enterprise.inject.spi.BeanManager;
import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.deltaspike.scxml.api.events.DialogOnEntryEvent;
import org.apache.deltaspike.scxml.api.events.DialogOnExitEvent;
import org.apache.deltaspike.scxml.api.events.DialogOnTransitionEvent;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DelegatingListener implements SCXMLListener, Serializable {

    public DelegatingListener() {
    }

    @Override
    public void onEntry(TransitionTarget tt) {
        BeanManager bm = new BeanManagerLocator().getBeanManager();
        bm.fireEvent(new DialogOnEntryEvent(tt));
    }

    @Override
    public void onTransition(TransitionTarget from, TransitionTarget to, Transition t) {
        BeanManager bm = new BeanManagerLocator().getBeanManager();
        bm.fireEvent(new DialogOnTransitionEvent(from, to, t));
    }

    @Override
    public void onExit(TransitionTarget tt) {
        BeanManager bm = new BeanManagerLocator().getBeanManager();
        bm.fireEvent(new DialogOnExitEvent(tt));
    }
    
    
    
    
}
