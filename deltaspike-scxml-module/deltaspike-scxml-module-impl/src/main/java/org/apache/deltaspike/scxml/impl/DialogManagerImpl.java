/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.Status;
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

    private Stack<Stack<SCXMLExecutor>> stack;
    @Inject
    DialogPublisher publisher;
    @Inject
    private Conversation conversation;

    public void initialize() {
        if (stack == null) {
            stack = new Stack<Stack<SCXMLExecutor>>();
            load();
            if (stack.isEmpty()) {
                stack.push(new Stack<SCXMLExecutor>());
            }
        }
    }

    private void load() {
        if (stack.isEmpty()) {
            Object[] state;
            List<DialogStateManager> references = BeanProvider.getContextualReferences(DialogStateManager.class, true);
            for (DialogStateManager manager : references) {
                state = manager.restoreState();
                if (state == null || state.length < 1) {
                    continue;
                }
                Stack<SCXMLExecutor> current = (Stack<SCXMLExecutor>) state[0];
                stack.push(current);
                break;
            }
        }
    }

    @Override
    public void pushStack(Stack<SCXMLExecutor> pushed) {
        initialize();
        stack.push(pushed);
    }

    @Override
    public Stack<SCXMLExecutor> popStack() {
        return stack.pop();
    }

    @Override
    public void flush() {
        List<DialogStateManager> references = BeanProvider.getContextualReferences(DialogStateManager.class, true);
        Object[] state = null;
        if (stack != null && !stack.isEmpty()) {
            state = new Object[]{stack.get(0)};
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
        return (stack.peek()).peek();
    }

    @Override
    public SCXMLExecutor getRootExecutor() {
        initialize();
        if (stack.isEmpty() || (stack.peek()).isEmpty()) {
            return null;
        }
        return getStack().get(0);
    }

    @Override
    public Stack<SCXMLExecutor> getStack() {
        initialize();
        return (stack.peek());
    }

    @Override
    public void pushExecutor(SCXMLExecutor executor) {
        initialize();
        getStack().push(executor);
    }

    @Override
    public void popExecutor() {
        initialize();
        getStack().pop();
    }

    @Override
    public boolean isStarted() {
        initialize();
        return stack != null && !stack.isEmpty() && !getStack().isEmpty();
    }

    @Override
    public void start(String src) {
        initialize();
        try {
            if (conversation.isTransient()) {
                conversation.begin();
            }
            getStack().clear();
            flush();

            SCXMLExecutor executor;
            SCXML statemachine = publisher.getModel(src);
            executor = new SCXMLExecutor(new DialogELEvaluator(), new SimpleDispatcher(), new SimpleErrorReporter());
            executor.setStateMachine(statemachine);
            executor.setRootContext(executor.getEvaluator().newContext(null));
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
            Iterator iterator = executor.getCurrentStatus().getStates().iterator();
            String stateId = ((State) iterator.next()).getId();

            if (executor.getCurrentStatus().isFinal()) {
                stop();
            }
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            throw new IllegalStateException("Instance SCXML has not yet been started");
        }
        if (stack.size() == 1 && getStack().size() == 1) {
            SCXMLExecutor executor = getExecutor();
            Status status = executor.getCurrentStatus();
            popExecutor();
            if (!conversation.isTransient()) {
                conversation.end();
            }
            BeanManager bm = new BeanManagerLocator().getBeanManager();
             bm.fireEvent(new DialogOnFinalEvent());
        }
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
