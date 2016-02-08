/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.model.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class AsyncTrigger implements Runnable {


    private final SCXMLExecutor executor;

    private final List<TriggerEvent> events;

    private final Log log = LogFactory.getLog(AsyncTrigger.class);

    public AsyncTrigger(final SCXMLExecutor executor, final TriggerEvent event) {
        this.executor = executor;
        this.events = new ArrayList();
        this.events.add(event);
    }

    public AsyncTrigger(final SCXMLExecutor executor) {
        this.executor = executor;
        this.events = new ArrayList();
    }
    
    public boolean isEmpty() {
        return events.isEmpty();
    }

    public boolean contains(TriggerEvent e) {
        return events.contains(e);
    }

    public boolean add(TriggerEvent e) {
        return events.add(e);
    }

    public TriggerEvent get(int index) {
        return events.get(index);
    }

    public TriggerEvent set(int index, TriggerEvent element) {
        return events.set(index, element);
    }

    public void add(int index, TriggerEvent element) {
        events.add(index, element);
    }

    public TriggerEvent remove(int index) {
        return events.remove(index);
    }

    public void start() {
        if (!events.isEmpty()) {
            run();
        }
    }

    @Override
    public void run() {
        try {
            synchronized (executor) {
                TriggerEvent[] evts = events.toArray(new TriggerEvent[events.size()]);
                executor.triggerEvents(evts);
            }
        } catch (ModelException me) {
            log.error(me.getMessage(), me);
        }
    }
}
