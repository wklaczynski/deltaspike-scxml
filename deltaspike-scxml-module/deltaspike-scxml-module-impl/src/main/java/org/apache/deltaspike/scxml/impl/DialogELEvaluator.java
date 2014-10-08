/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.el.*;
import javax.faces.context.FacesContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.*;
import org.apache.commons.scxml.env.SimpleContext;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogManager;
import org.w3c.dom.Node;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogELEvaluator implements Evaluator, Serializable {

    private static final Pattern inFct = Pattern.compile("In\\(");
    private static final Pattern dataFct = Pattern.compile("Data\\(");

    public DialogELEvaluator() {
    }

    @Override
    public Object eval(Context ctx, String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext);
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        try {
            fcontext.putContext(Context.class, ctx);
            fcontext.putContext(DialogManager.class, manager);
            fcontext.putContext(SCXMLExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Object.class);
            return ve.getValue(context);
        } catch (PropertyNotFoundException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(Context.class, new SimpleContext());
        }
    }

    public void evalSet(Context ctx, String expr, Object value) throws SCXMLExpressionException {
        if (expr == null) {
            return;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext);
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        try {
            fcontext.putContext(Context.class, ctx);
            fcontext.putContext(DialogManager.class, manager);
            fcontext.putContext(SCXMLExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Object.class);
            ve.setValue(context, value);
        } catch (PropertyNotFoundException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(Context.class, new SimpleContext());
        }
    }
    
    @Override
    public Boolean evalCond(Context ctx, String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext);
        
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();


        try {
            fcontext.putContext(Context.class, ctx);
            fcontext.putContext(DialogManager.class, manager);
            fcontext.putContext(SCXMLExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Boolean.class);
            return (Boolean) ve.getValue(context);
        } catch (PropertyNotFoundException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(Context.class, new SimpleContext());
        }
    }

    @Override
    public Node evalLocation(Context ctx, String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext);
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        try {
            fcontext.putContext(Context.class, ctx);
            fcontext.putContext(DialogManager.class, manager);
            fcontext.putContext(SCXMLExecutor.class, executor);

            String evalExpr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            evalExpr = dataFct.matcher(evalExpr).replaceAll("Data(_ALL_NAMESPACES, ");
            ValueExpression ve = ef.createValueExpression(context, evalExpr, Node.class);
            Node node = (Node) ve.getValue(context);
            return node;
        } catch (PropertyNotFoundException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } catch (ELException e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        } finally {
            fcontext.putContext(Context.class, new SimpleContext());
        }
    }

    @Override
    public Context newContext(Context parent) {
        return new SimpleContext(parent);
    }

    public static class ContextWrapper extends ELContext implements Serializable {

        private final ELContext context;
        private final FunctionMapper functionMapper;
        private final VariableMapper variableMapper;
        private final CompositeELResolver elResolver;

        public ContextWrapper(ELContext context) {
            super();
            this.context = context;
            functionMapper = new BuiltinFunctionMapper(context.getFunctionMapper());
            variableMapper = new BuiltinVariableMapper(context.getVariableMapper());
            elResolver = new CompositeELResolver();
            elResolver.add(new DialogELResolver());
            elResolver.add(context.getELResolver());
        }

        @Override
        public ELResolver getELResolver() {
            return elResolver;
        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return functionMapper;
        }

        @Override
        public VariableMapper getVariableMapper() {
            return variableMapper;
        }

        @Override
        public Object getContext(Class key) {
            Object ret = super.getContext(key);
            if (ret == null) {
                ret = context.getContext(key);
            }
            return ret;
        }
    }

    static class BuiltinVariableMapper extends VariableMapper implements Serializable {

        private final VariableMapper mapper;

        public BuiltinVariableMapper(VariableMapper mapper) {
            super();
            this.mapper = mapper;
        }

        @Override
        public ValueExpression resolveVariable(String variable) {
            return mapper.resolveVariable(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return mapper.setVariable(variable, expression);
        }
    }

    static class BuiltinFunctionMapper extends FunctionMapper implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Log log = LogFactory.getLog(DialogELEvaluator.BuiltinFunctionMapper.class);
        private final FunctionMapper mapper;

        public BuiltinFunctionMapper(FunctionMapper mapper) {
            super();
            this.mapper = mapper;
        }

        @Override
        public Method resolveFunction(final String prefix, final String localName) {
            if (localName.equals("In")) {
                Class[] attrs = new Class[]{Set.class, String.class};
                try {
                    return Builtin.class.getMethod("isMember", attrs);
                } catch (SecurityException e) {
                    log.error("resolving isMember(Set, String)", e);
                } catch (NoSuchMethodException e) {
                    log.error("resolving isMember(Set, String)", e);
                }
            } else if (localName.equals("Data")) {
                // rvalue in expressions, coerce to String
                Class[] attrs =
                        new Class[]{Map.class, Object.class, String.class};
                try {
                    return Builtin.class.getMethod("data", attrs);
                } catch (SecurityException e) {
                    log.error("resolving data(Node, String)", e);
                } catch (NoSuchMethodException e) {
                    log.error("resolving data(Node, String)", e);
                }
            } else if (localName.equals("LData")) {
                // lvalue in expressions, retain as Node
                Class[] attrs =
                        new Class[]{Map.class, Object.class, String.class};
                try {
                    return Builtin.class.getMethod("dataNode", attrs);
                } catch (SecurityException e) {
                    log.error("resolving data(Node, String)", e);
                } catch (NoSuchMethodException e) {
                    log.error("resolving data(Node, String)", e);
                }
            } else if (mapper != null) {
                return mapper.resolveFunction(prefix, localName);
            }
            return null;
        }
    }
}
