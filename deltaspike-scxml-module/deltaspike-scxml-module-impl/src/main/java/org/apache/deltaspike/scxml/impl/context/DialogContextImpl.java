/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.context;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;
import org.apache.deltaspike.scxml.api.DialogScoped;
import org.apache.deltaspike.scxml.impl.DialogUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogContextImpl extends AbstractContext {

    private static final String STORAGE_KEY = "_____@@@SopeDialogContext____";
    private final BeanManager beanManager;

    public DialogContextImpl(BeanManager beanManager) {
        super(beanManager);
        this.beanManager = beanManager;
    }

    private SCXMLExecutor getExecutor() {
        return DialogUtils.getExecutor();
    }

    @Override
    protected ContextualStorage getContextualStorage(boolean createIfNotExist) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        SCXMLExecutor executor = getExecutor();
        ContextualStorage contextualStorage;
        if (executor != null) {
            Context context = executor.getRootContext();
            contextualStorage = (ContextualStorage) context.get(STORAGE_KEY);
            if (contextualStorage == null) {
                synchronized (this) {
                    if (createIfNotExist) {
                        contextualStorage = new ContextualStorage(beanManager, true, true);
                        context.set(STORAGE_KEY, contextualStorage);
                    }
                }
            }
            ec.getRequestMap().put(STORAGE_KEY, contextualStorage);
        } else {
            contextualStorage = (ContextualStorage) ec.getRequestMap().get(STORAGE_KEY);
        }
        return contextualStorage;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return DialogScoped.class;
    }

    @Override
    public boolean isActive() {
        boolean result = getExecutor() != null;
        if (!result) {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();
            result = ec.getRequestMap().containsKey(STORAGE_KEY);
        }
        return result;
    }
}
