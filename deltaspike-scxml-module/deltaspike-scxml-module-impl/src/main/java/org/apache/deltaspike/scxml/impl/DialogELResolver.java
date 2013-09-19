/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Set;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;

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
            if (property.toString().equals(DIALOG_VARIABLE_NAME)) {
                context.setPropertyResolved(true);
                result = getDialogScope(context);
            } else if (property.toString().equals(STATE_VARIABLE_NAME)) {
                context.setPropertyResolved(true);
                result = getStateScope(context);
            } else {
                StateScope step = getStateScope(context);
                if (step != null) {
                    Object value = step.get(property.toString());
                    if (value != null) {
                        context.setPropertyResolved(true);
                        result = value;
                    }
                }
            }
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            result = scope.get(property.toString());
        } else if (base instanceof StateScope) {
            context.setPropertyResolved(true);
            StateScope scope = (StateScope) base;
            result = scope.get(property.toString());
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
            if (property.toString().equals(DIALOG_VARIABLE_NAME)) {
                context.setPropertyResolved(true);
                result = DialogScope.class;
            } else if (property.toString().equals(STATE_VARIABLE_NAME)) {
                context.setPropertyResolved(true);
                result = StateScope.class;
            } else {
                StateScope step = getStateScope(context);
                if (step != null) {
                    Object value = step.get(property.toString());
                    if (value != null) {
                        context.setPropertyResolved(true);
                        result = value.getClass();
                    }
                }
            }
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
        } else if (base instanceof StateScope) {
            context.setPropertyResolved(true);
            StateScope scope = (StateScope) base;
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
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
            if (property.toString().equals(DIALOG_VARIABLE_NAME)) {
                throw new PropertyNotWritableException(property.toString());
            } else if (property.toString().equals(STATE_VARIABLE_NAME)) {
                throw new PropertyNotWritableException(property.toString());
            } else {
                StateScope step = getStateScope(context);
                if (step != null) {
                    Object old = step.get(property.toString());
                    if (old != null) {
                        context.setPropertyResolved(true);
                        step.set(property.toString(), value);
                    }
                }
            }
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            scope.set(property.toString(), value);
        } else if (base instanceof StateScope) {
            context.setPropertyResolved(true);
            StateScope scope = (StateScope) base;
            scope.set(property.toString(), value);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
//        List<FeatureDescriptor> result = Collections.<FeatureDescriptor>emptyList();
//        if (null == base) {
//            return Collections.<FeatureDescriptor>emptyList().iterator();
//        } else if (base instanceof DialogScope) {
//            context.setPropertyResolved(true);
//            DialogScope scope = (DialogScope) base;
//            Map<String, Object> params = scope;
//            for (Map.Entry<String, Object> param : params.entrySet()) {
//                FeatureDescriptor desc = new FeatureDescriptor();
//                desc.setName(param.getKey().toString());
//                desc.setDisplayName("Dialog Scope Object");
//                desc.setValue(ELResolver.TYPE, param.getValue().getClass());
//                desc.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, true);
//                result.add(desc);
//            }
//        } else if (base instanceof StateScope) {
//            context.setPropertyResolved(true);
//            StateScope scope = (StateScope) base;
//            Map<String, Object> params = scope;
//            for (Map.Entry<String, Object> param : params.entrySet()) {
//                FeatureDescriptor desc = new FeatureDescriptor();
//                desc.setName(param.getKey().toString());
//                desc.setDisplayName("State Scope Object");
//                desc.setValue(ELResolver.TYPE, param.getValue().getClass());
//                desc.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, true);
//                result.add(desc);
//            }
//        }
//        return result.iterator();
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    private DialogScope getDialogScope(ELContext context) {
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        Context ctx = (Context) context.getContext(Context.class);
        return new DialogScope(executor, ctx);
    }

    private StateScope getStateScope(ELContext context) {
        Context ctx = (Context) context.getContext(Context.class);
        if (ctx != null) {
            return new StateScope(ctx);
        } else {
            return null;
        }
    }

    private static class DialogScope extends AbstractMap<String, Object> implements Serializable {

        private final Context ctx;
        private final SCXMLExecutor executor;

        public DialogScope(SCXMLExecutor executor, Context ctx) {
            this.executor = executor;
            this.ctx = ctx;
        }

        public Context getCtx() {
            return ctx;
        }

        public SCXMLExecutor getExecutor() {
            return executor;
        }

        public Object get(String name) {
            return ctx.get(name);
        }

        public void set(String name, Object value) {
            Context root = executor.getRootContext();
            root.setLocal(name, value);
        }

        public boolean has(String name) {
            return ctx.has(name);
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class StateScope implements Serializable {

        private final Context ctx;

        public StateScope(Context ctx) {
            this.ctx = ctx;
        }

        public Context getCtx() {
            return ctx;
        }

        public Object get(String name) {
            return ctx.get(name);
        }

        public void set(String name, Object value) {
            ctx.setLocal(name, value);
        }

        public boolean has(String name) {
            return ctx.has(name);
        }
    }
}
