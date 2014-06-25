/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.context;

import java.lang.annotation.Annotation;
import java.util.Stack;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;
import org.apache.deltaspike.scxml.api.ParallerScoped;
import org.apache.deltaspike.scxml.api.events.DialogOnEntryEvent;
import org.apache.deltaspike.scxml.api.events.DialogOnExitEvent;
import org.apache.deltaspike.scxml.impl.DialogUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParallelContextImpl extends AbstractContext {

    private final BeanManager beanManager;
    
    public ParallelContextImpl(BeanManager beanManager) {
        super(beanManager);
        this.beanManager = beanManager;
    }

    private SCXMLExecutor getExecutor() {
        return DialogUtils.getExecutor();
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        Stack<ContextualStorage> stack = getStack();
        return stack.peek();
    }
    
    
    private Stack<ContextualStorage> getStack() {
        SCXMLExecutor executor = getExecutor();
        if (executor == null) {
            return null;
        }
        Context context = executor.getRootContext();
        Stack<ContextualStorage> instance = (Stack<ContextualStorage>) context.get("_____@@@SopeParallelStack____");
        if (instance == null) {
            instance = new Stack<ContextualStorage>();
            instance.push(new ContextualStorage(beanManager, true, true));
            context.set("_____@@@SopeParallelStack____", instance);
        }
        return instance;
    }

    @Override
    public boolean isActive() {
        Stack<ContextualStorage> stack = getStack();
        return stack != null && !stack.isEmpty();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ParallerScoped.class;
    }

    public void onEntryEvent(@Observes DialogOnEntryEvent event) {
        TransitionTarget target = event.getTarget();
        if (target instanceof Parallel) {
            SCXMLExecutor executor = getExecutor();
            if (executor == null) {
                return;
            }
            Stack<ContextualStorage> stack = getStack();
            stack.push(new ContextualStorage(beanManager, true, true));
        }
    }

    public void onExitEvent(@Observes DialogOnExitEvent event) {
        TransitionTarget target = event.getTarget();
        if (target instanceof Parallel) {
            Stack<ContextualStorage> stack = getStack();
            if (!stack.isEmpty()) {
                stack.pop();
            }
        }
    }
}
