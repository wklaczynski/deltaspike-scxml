/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import org.apache.deltaspike.scxml.impl.AsyncTrigger;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.commons.scxml.*;
import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.invoke.InvokerException;
import org.apache.commons.scxml.model.ModelException;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogInvoker;
import org.apache.deltaspike.scxml.api.DialogManager;
import org.apache.deltaspike.scxml.impl.DialogPublisher;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogInvoker("scxml")
public class SubInvoker implements Invoker, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Parent state ID.
     */
    private String parentStateId;
    /**
     * Event prefix, all events sent to the parent executor must begin with this
     * prefix.
     */
    private String eventPrefix;
    /**
     * Invoking document's SCInstance.
     */
    private SCInstance parentSCInstance;
    /**
     * Cancellation status.
     */
    private boolean cancelled;
    //// Constants
    /**
     * Prefix for all events sent to the parent state machine.
     */
    private static final String invokePrefix = ".invoke.";
    /**
     * Suffix for invoke done event.
     */
    private static final String invokeDone = "done";
    /**
     * Suffix for invoke cancel response event.
     */
    private static final String invokeCancelResponse = "cancel.response";

    public SubInvoker() {
        super();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setParentStateId(final String parentStateId) {
        this.parentStateId = parentStateId;
        this.eventPrefix = this.parentStateId + invokePrefix;
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setSCInstance(final SCInstance scInstance) {
        this.parentSCInstance = scInstance;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invoke(final String source, final Map params) throws InvokerException {
        try {
            DialogPublisher publisher = BeanProvider.getContextualReference(DialogPublisher.class);
            DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);

            URL url = new URL(source);
            String viewId = url.getFile();
            ServletContext ctx = publisher.getServletContext();
            String realPath = ctx.getRealPath("/");
            if (viewId.contains(realPath)) {
                viewId = viewId.substring(realPath.length());
            }
            String contextPath = ctx.getContextPath();
            if (viewId.contains(contextPath)) {
                viewId = viewId.substring(viewId.indexOf(contextPath));
                viewId = viewId.substring(contextPath.length());
            }

            manager.start(viewId, params);
        } catch (MalformedURLException ex) {
            throw new InvokerException(ex);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvents(final TriggerEvent[] evts) throws InvokerException {
        if (cancelled) {
            return;
        }
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        boolean doneBefore = executor.getCurrentStatus().isFinal();
        try {
            executor.triggerEvents(evts);
        } catch (ModelException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        }
        if (!doneBefore && executor.getCurrentStatus().isFinal()) {
            manager.stop(parentSCInstance.getExecutor());
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        TriggerEvent te = new TriggerEvent(eventPrefix + invokeCancelResponse, TriggerEvent.SIGNAL_EVENT);
        new AsyncTrigger(parentSCInstance.getExecutor(), te).start();
    }
}
