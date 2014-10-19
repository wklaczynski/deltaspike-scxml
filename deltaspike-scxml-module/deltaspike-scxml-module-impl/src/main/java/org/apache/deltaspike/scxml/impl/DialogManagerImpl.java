/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.facelets.compiler.Compiler;
import org.apache.deltaspike.scxml.impl.el.DialogELEvaluator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.Status;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.env.SimpleDispatcher;
import org.apache.commons.scxml.env.SimpleErrorReporter;
import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogManager;
import org.apache.deltaspike.scxml.api.DialogStateManager;
import org.apache.deltaspike.scxml.api.events.DialogOnEntryEvent;
import org.apache.deltaspike.scxml.api.events.DialogOnExitEvent;
import org.apache.deltaspike.scxml.api.events.DialogOnFinalEvent;

/**
 *
 * @author Waldemar Kłaczyński
 */
@RequestScoped
public class DialogManagerImpl implements DialogManager {

    private Stack<SCXMLExecutor> stack;
    @Inject
    DialogPublisher publisher;
    @Inject
    private Conversation conversation;

    public void initialize() {
        if (stack == null) {
            load();
            if (stack == null) {
                stack = new Stack<SCXMLExecutor>();
            }
        }
    }

    private void load() {
        if (stack == null) {
            Object[] state;
            List<DialogStateManager> references = BeanProvider.getContextualReferences(DialogStateManager.class, true);
            for (DialogStateManager manager : references) {
                state = manager.restoreState();
                if (state == null || state.length < 1) {
                    continue;
                }
                stack = (Stack<SCXMLExecutor>) state[0];
                break;
            }
        }
    }

    @Override
    public void flush() {
        List<DialogStateManager> references = BeanProvider.getContextualReferences(DialogStateManager.class, true);
        Object[] state = null;
        if (stack != null) {
            state = new Object[]{stack};
        }
        for (DialogStateManager manager : references) {
            manager.saveState(state);
        }
    }

    @Override
    public SCXMLExecutor getExecutor() {
        initialize();
        if (getStack().isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    @Override
    public SCXMLExecutor getRootExecutor() {
        initialize();
        if (stack.isEmpty()) {
            return null;
        }
        return getStack().get(0);
    }

    @Override
    public Stack<SCXMLExecutor> getStack() {
        initialize();
        return stack;
    }

    @Override
    public void pushExecutor(SCXMLExecutor executor) {
        initialize();
        getStack().push(executor);
        flush();
    }

    @Override
    public void popExecutor() {
        initialize();
        getStack().pop();
        flush();
    }

    @Override
    public boolean isStarted() {
        initialize();
        return stack != null && !stack.isEmpty();
    }

    @Override
    public void start(String src, Map params) {
        initialize();
        try {
            if (conversation.isTransient()) {
                conversation.begin();
            }

            SCXMLExecutor parent = getExecutor();

            SCXMLExecutor executor;
            SCXML statemachine = publisher.getModel(src);

            executor = new SCXMLExecutor(new DialogELEvaluator(), new SimpleDispatcher(), new SimpleErrorReporter());
            Context rootCtx = executor.getEvaluator().newContext(null);
            if (params != null) {
                for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    rootCtx.setLocal((String) entry.getKey(), entry.getValue());
                }
            }
            if (parent != null) {
                Context parentCtx = parent.getRootContext();
                rootCtx.setLocal("scxml_has_parent", true);
            }

            executor.setRootContext(rootCtx);
            executor.setStateMachine(statemachine);
            executor.addListener(statemachine, new DelegatingListener());
            Map<String, Class<Invoker>> customInvokers = publisher.getCustomInvokers();
            for (Map.Entry<String, Class<Invoker>> entry : customInvokers.entrySet()) {
                executor.registerInvokerClass(entry.getKey(), entry.getValue());
            }
            pushExecutor(executor);

            try {
                executor.go();
            } catch (ModelException me) {
            }
            if (executor.getCurrentStatus().isFinal()) {
                stop(parent);
            }
            flush();

        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void stop() {
        stop(null);
    }

    @Override
    public void stop(SCXMLExecutor to) {
        if (!isStarted()) {
            throw new IllegalStateException("Instance SCXML has not yet been started");
        }

        SCXMLExecutor executor = stack.pop();
        SCXMLExecutor parent = null;

        while (!stack.empty()) {
            parent = stack.peek();
            if (parent == to) {
                break;
            }
            executor = stack.pop();
            parent = null;
        }

        if (parent == null) {

            if (!conversation.isTransient()) {
                conversation.end();
            }
            BeanManager bm = new BeanManagerLocator().getBeanManager();
            bm.fireEvent(new DialogOnFinalEvent());
        } else {

            AsyncTrigger trigger = new AsyncTrigger(parent);

            Status pstatus = parent.getCurrentStatus();
            for (Iterator j = pstatus.getStates().iterator(); j.hasNext();) {
                State pstate = (State) j.next();
                String eventPrefix = pstate.getId() + ".invoke.";

                boolean stop = false;
                Status status = executor.getCurrentStatus();
                for (Iterator i = status.getStates().iterator(); i.hasNext();) {
                    State state = (State) i.next();
                    if (state.isFinal()) {
                        TriggerEvent te = new TriggerEvent(eventPrefix + state.getId(), TriggerEvent.SIGNAL_EVENT);
                        trigger.add(te);
                        stop = true;
                    }
                }
                if (!stop) {
                    TriggerEvent te = new TriggerEvent(eventPrefix + "close", TriggerEvent.SIGNAL_EVENT);
                    trigger.add(te);
                }
                TriggerEvent te = new TriggerEvent(eventPrefix + "done", TriggerEvent.SIGNAL_EVENT);
                trigger.add(te);
            }

            trigger.start();
        }
        flush();
    }

    public void onExitEvent(@Observes DialogOnExitEvent event) {
        SCXMLExecutor executor = getExecutor();
        if (executor.getCurrentStatus().isFinal()) {
            //stop();
        }
    }

    public void onEntryEvent(@Observes DialogOnEntryEvent event) {
        SCXMLExecutor executor = getExecutor();
        if (executor.getCurrentStatus().isFinal()) {
            //stop();
        }
    }

}
