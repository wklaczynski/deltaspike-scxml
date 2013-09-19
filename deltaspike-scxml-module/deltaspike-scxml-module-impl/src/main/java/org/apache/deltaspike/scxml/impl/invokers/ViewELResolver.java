/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import org.apache.deltaspike.core.api.provider.BeanProvider;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ViewELResolver extends ELResolver implements Serializable {

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            ViewParamsContext ctx = BeanProvider.getContextualReference(ViewParamsContext.class);
            if (ctx.containsKey(property.toString())) {
                context.setPropertyResolved(true);
                result = ctx.get(property.toString());
            }
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Class result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            ViewParamsContext ctx = BeanProvider.getContextualReference(ViewParamsContext.class);
            if (ctx.containsKey(property.toString())) {
                context.setPropertyResolved(true);
                result = ctx.get(property.toString()).getClass();
            }
        }
        return result;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        ViewParamsContext ctx = BeanProvider.getContextualReference(ViewParamsContext.class);
        if (ctx.containsKey(property.toString())) {
            context.setPropertyResolved(true);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    private DialogScope getDialogScope(ELContext context) {
        return new DialogScope();
    }

    private static class DialogScope extends ConcurrentHashMap<String, Object> implements Serializable {

        public DialogScope() {
            super();
        }
    }
}
