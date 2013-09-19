/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.context;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.BeanManager;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.env.SimpleContext;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;
import org.apache.deltaspike.scxml.api.StateScoped;
import org.apache.deltaspike.scxml.impl.DialogUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateContextImpl extends AbstractContext {
    private final BeanManager beanManager;

    public StateContextImpl(BeanManager beanManager) {
        super(beanManager);
        this.beanManager = beanManager;
    }

    private SCXMLExecutor getExecutor() {
        return DialogUtils.getExecutor();
    }

    private Context getContext() {
        SCXMLExecutor executor = getExecutor();
        if (executor == null) {
            return null;
        }
        Iterator iterator = executor.getCurrentStatus().getStates().iterator();
        State state = ((State) iterator.next());
        Context context = getContext(state);
        return context;
    }

    public Context getContext(final TransitionTarget transitionTarget) {
        SCXMLExecutor executor = getExecutor();
        if (executor == null) {
            return null;
        }
        Map<TransitionTarget, Object> contexts = getContexts();
        Context context = (Context) contexts.get(transitionTarget);
        if (context == null) {
            TransitionTarget parent = transitionTarget.getParent();
            if (parent == null) {
                context = new SimpleContext(executor.getRootContext());
            } else {
                context = new SimpleContext(getContext(parent));
            }
            contexts.put(transitionTarget, context);
        }
        return context;
    }

    private Map<TransitionTarget, Object> getContexts() {
        SCXMLExecutor executor = getExecutor();
        if (executor == null) {
            return null;
        }
        Context context = executor.getRootContext();


        Map<TransitionTarget, Object> instance = (Map<TransitionTarget, Object>) context.get("_____@@@SopeStateMap____");
        if (instance == null) {
            instance = Collections.synchronizedMap(new HashMap<TransitionTarget, Object>());
            context.set("_____@@@SopeStateMap____", instance);
        }
        return instance;
    }

    @Override
    protected ContextualStorage getContextualStorage(boolean createIfNotExist) {
        SCXMLExecutor executor = getExecutor();
        if (executor == null) {
            throw new ContextNotActiveException("SCXMLExecutor: no dialog set for the current Thread yet!");
        }
        Context context = getContext();
        ContextualStorage contextualStorage = (ContextualStorage) context.get("_____@@@SopeTransitionContext___");
        if (contextualStorage == null) {
            synchronized (this) {
                if (createIfNotExist) {
                    contextualStorage = new ContextualStorage(beanManager, true, true);
                    context.set("_____@@@SopeTransitionContext___", contextualStorage);
                }
            }
        }
        return contextualStorage;
    }
    
    @Override
    public boolean isActive() {
        return getExecutor() != null;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return StateScoped.class;
    }

}
