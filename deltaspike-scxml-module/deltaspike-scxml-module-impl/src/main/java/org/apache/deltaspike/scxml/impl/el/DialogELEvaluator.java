package org.apache.deltaspike.scxml.impl.el;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.facelets.compiler.CompilationMessageHolder;
import com.sun.faces.facelets.compiler.CompilationMessageHolderImpl;
import com.sun.faces.facelets.compiler.Compiler;
import com.sun.faces.facelets.tag.TagLibrary;
import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;
import javax.el.*;
import javax.faces.context.FacesContext;
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
    private final ApplicationAssociate associate;

    public DialogELEvaluator() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        associate = ApplicationAssociate.getInstance(ctx.getExternalContext());
    }

    @Override
    public Object eval(Context ctx, String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();
        
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
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
            messageHolder.processCompilationMessages(fc);
        }
    }

    public void evalSet(Context ctx, String expr, Object value) throws SCXMLExpressionException {
        if (expr == null) {
            return;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();

        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
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
            messageHolder.processCompilationMessages(fc);
        }
    }

    @Override
    public Boolean evalCond(Context ctx, String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();
        
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
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
            messageHolder.processCompilationMessages(fc);
        }
    }

    @Override
    public Node evalLocation(Context ctx, String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        CompilationMessageHolder messageHolder = new CompilationMessageHolderImpl();
        
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        SCXMLExecutor executor = manager.getExecutor();
        FacesContext fc = FacesContext.getCurrentInstance();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext fcontext = fc.getELContext();
        ELContext context = new ContextWrapper(fcontext, executor, messageHolder);
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

    public class ContextWrapper extends ELContext implements Serializable {

        private final ELContext context;
        private final CompositeFunctionMapper functionMapper;
        private final VariableMapper variableMapper;
        private final CompositeELResolver elResolver;

        private ContextWrapper(ELContext context, SCXMLExecutor executor, CompilationMessageHolder messageHolder) {
            super();
            this.context = context;
            functionMapper = new CompositeFunctionMapper();
            variableMapper = new BuiltinVariableMapper(context.getVariableMapper());

            functionMapper.add(new BuiltinFunctionMapper());
            
            Compiler compiler = associate.getCompiler();
            TagLibrary tagLibrary = compiler.createTagLibrary(messageHolder);
            Map namespaces = executor.getStateMachine().getNamespaces();
            
            functionMapper.add(new TagsFunctionMapper(namespaces, tagLibrary));
            
            functionMapper.add(context.getFunctionMapper());

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

}
