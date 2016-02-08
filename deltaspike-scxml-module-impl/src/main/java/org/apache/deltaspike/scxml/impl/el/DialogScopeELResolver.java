/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.el;

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
import org.apache.deltaspike.scxml.impl.DialogUtils;

/**
 *
 * @author waldek
 */
public class DialogScopeELResolver extends ELResolver {

    public static final String DIALOG_SCOPE = "dialogScope";
    public static final String DIALOG_VARIABLE_NAME = "dialog";
    public static final String STATE_VARIABLE_NAME = "state";
    public static final String DIALOG_PARAM_MAP = "org.scxml.attr";

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (DIALOG_VARIABLE_NAME.equals(property.toString())) {
                context.setPropertyResolved(true);
                result = getDialogParams(context);
            } else if (STATE_VARIABLE_NAME.equals(property.toString())) {
                context.setPropertyResolved(true);
                result = getStateParams(context);
            } else if (DIALOG_SCOPE.equals(property.toString())) {
                context.setPropertyResolved(true);
                result = getDialogScope(context);
            }
        } else if (base instanceof DialogParams) {
            context.setPropertyResolved(true);
            DialogParams scope = (DialogParams) base;
            result = scope.get(property.toString());
        } else if (base instanceof StateParams) {
            context.setPropertyResolved(true);
            StateParams scope = (StateParams) base;
            result = scope.get(property.toString());
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
            if (DIALOG_VARIABLE_NAME.equals(property.toString())) {
                result = DialogParams.class;
            } else if (STATE_VARIABLE_NAME.equals(property.toString())) {
                result = StateParams.class;
            } else if (DIALOG_SCOPE.equals(property.toString())) {
                result = DialogScope.class;
            }
        } else if (base instanceof DialogParams) {
            context.setPropertyResolved(true);
            DialogParams scope = (DialogParams) base;
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
        } else if (base instanceof StateParams) {
            context.setPropertyResolved(true);
            StateParams scope = (StateParams) base;
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
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
        } else if (base instanceof DialogParams) {
            context.setPropertyResolved(true);
            DialogParams scope = (DialogParams) base;
            scope.set(property.toString(), value);
        } else if (base instanceof StateParams) {
            context.setPropertyResolved(true);
            StateParams scope = (StateParams) base;
            scope.set(property.toString(), value);
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
            } else if (STATE_VARIABLE_NAME.equals(property.toString())) {
                result = true;
            } else if (DIALOG_SCOPE.equals(property.toString())) {
                result = true;
            }
        } else if (base instanceof DialogScope) {
            result = false;
        } else if (base instanceof DialogParams) {
            result = false;
        } else if (base instanceof StateParams) {
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
                desc.setName(param.getKey());
                desc.setDisplayName("Dialog Scope Object");
                desc.setValue(ELResolver.TYPE, param.getValue().getClass());
                desc.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, true);
                result.add(desc);
            }
        } else if (base instanceof DialogParams) {
            context.setPropertyResolved(true);
            DialogParams scope = (DialogParams) base;
        } else if (base instanceof StateParams) {
            context.setPropertyResolved(true);
            StateParams scope = (StateParams) base;
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

    private DialogParams getDialogParams(ELContext context) {
        DialogParams attrScope = null;
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor == null) {
            executor = getExecutor();
        }

        if (executor != null) {
            attrScope = new DialogParams(executor);
        }
        return attrScope;
    }

    private StateParams getStateParams(ELContext context) {
        StateParams attrScope = null;
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null) {
                attrScope = new StateParams(ctx);
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

    private static class DialogParams extends AbstractMap<String, Object> implements Serializable {

        private final Context ctx;
        private final SCXMLExecutor executor;

        public DialogParams(SCXMLExecutor executor) {
            this.executor = executor;
            this.ctx = executor.getRootContext();
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
            ctx.setLocal(name, value);
        }

        public boolean has(String name) {
            return ctx.has(name);
        }

        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class StateParams implements Serializable {

        private final Context ctx;

        public StateParams(Context ctx) {
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
