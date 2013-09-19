/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

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

    /**
     * The state machine executor.
     */
    private final SCXMLExecutor executor;
    /**
     * The event(s) to be triggered.
     */
    private final TriggerEvent[] events;
    /**
     * The log.
     */
    private final Log log = LogFactory.getLog(AsyncTrigger.class);

    /**
     * Constructor.
     *
     * @param executor The {@link SCXMLExecutor} to trigger the event on.
     * @param event The {@link TriggerEvent}.
     */
    AsyncTrigger(final SCXMLExecutor executor, final TriggerEvent event) {
        this.executor = executor;
        this.events = new TriggerEvent[1];
        this.events[0] = event;
    }

    /**
     * Fire the trigger(s) asynchronously.
     */
    public void start() {
        //new Thread(this).start();
        run();
    }

    /**
     * Fire the event(s).
     */
    @Override
    public void run() {
        try {
            synchronized (executor) {
                executor.triggerEvents(events);
            }
        } catch (ModelException me) {
            log.error(me.getMessage(), me);
        }
    }
}
