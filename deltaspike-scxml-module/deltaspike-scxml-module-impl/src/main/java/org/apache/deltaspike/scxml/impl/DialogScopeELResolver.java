/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.el.*;
import javax.faces.context.FacesContext;
import javax.faces.event.PostConstructCustomScopeEvent;
import javax.faces.event.ScopeContext;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;

/**
 *
 * @author waldek
 */
public class DialogScopeELResolver extends ELResolver {

    public static final String DIALOG_SCOPE = "dialogScope";
    public static final String DIALOG_PARAM_MAP = "org.scxml.attr";

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (DIALOG_SCOPE.equals(property.toString())) {
                context.setPropertyResolved(true);
                result = getDialogScope(context);
            }
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            result = scope.get(property.toString());
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Class result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (DIALOG_SCOPE.equals(property.toString())) {
                result = DialogScope.class;
            }
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            result = String.class;
        }
        return result;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            scope.put(property.toString(), value);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        boolean result = false;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (property.toString().equals(DIALOG_SCOPE)) {
                context.setPropertyResolved(true);
                result = true;
            }
        } else if (base instanceof DialogScope) {
            result = false;
        }
        return result;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        List<FeatureDescriptor> result = Collections.<FeatureDescriptor>emptyList();
        if (null == base) {
            return Collections.<FeatureDescriptor>emptyList().iterator();
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            Map<String, Object> params = scope;
            for (Map.Entry<String, Object> param : params.entrySet()) {
                FeatureDescriptor desc = new FeatureDescriptor();
                desc.setName(param.getKey().toString());
                desc.setDisplayName("Dialog Scope Object");
                desc.setValue(ELResolver.TYPE, param.getValue().getClass());
                desc.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, true);
                result.add(desc);
            }
        }
        return result.iterator();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (null == base) {
            return null;
        } else if (base instanceof DialogScope) {
            return String.class;
        }
        return null;
    }

    private SCXMLExecutor getExecutor() {
        return DialogUtils.getExecutor();
    }

    private DialogScope getDialogScope(ELContext elContext) {
        DialogScope attrScope = null;
        SCXMLExecutor executor = getExecutor();
        if (executor != null) {
            Context context = executor.getRootContext();
            attrScope = (DialogScope) context.get(DIALOG_PARAM_MAP);
            if (attrScope == null) {
                attrScope = new DialogScope();
                context.set(DIALOG_PARAM_MAP, attrScope);
                attrScope.onCreate();
            }
        }
        return attrScope;
    }

    public class DialogScope extends ConcurrentHashMap<String, Object> implements Serializable {

        public DialogScope() {
            super();
        }

        public void onCreate() {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ScopeContext context = new ScopeContext(DIALOG_SCOPE, this);
            ctx.getApplication().publishEvent(ctx, PostConstructCustomScopeEvent.class, context);
        }
    }

}
