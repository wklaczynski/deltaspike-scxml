/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import org.apache.commons.scxml.Context;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogELResolver extends ELResolver implements Serializable {

    public static final String DIALOG_VARIABLE_NAME = "dialog";
    public static final String STATE_VARIABLE_NAME = "state";

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null && ctx.has(property.toString())) {
                Object value = ctx.get(property.toString());
                if (value != null) {
                    context.setPropertyResolved(true);
                    result = value;
                }
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
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null && ctx.has(property.toString())) {
                Object value = ctx.get(property.toString());
                if (value != null) {
                    context.setPropertyResolved(true);
                    result = value.getClass();
                }
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
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null && ctx.has(property.toString())) {
                Object old = ctx.get(property.toString());
                if (old != null) {
                    context.setPropertyResolved(true);
                    ctx.set(property.toString(), value);
                }
            }
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        if (null == base) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null && ctx.has(property.toString())) {
                context.setPropertyResolved(true);
            }
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

}
