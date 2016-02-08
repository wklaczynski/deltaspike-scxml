/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.actions;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.ModelException;
import org.apache.deltaspike.scxml.api.DialogAction;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogAction(value="var", namespaceURI="http://www.apache.org/scxml/actions")
public class VarAction extends Action {

    private String name = null;
    private String expr = null;

    public VarAction() {
        super();
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void execute(EventDispatcher evtDispatcher, ErrorReporter errRep, SCInstance scInstance, Log appLog, Collection derivedEvents) throws ModelException, SCXMLExpressionException {
        Context ctx = scInstance.getContext(getParentTransitionTarget());
        Evaluator eval = scInstance.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Object varObj = eval.eval(ctx, expr);
        ctx.setLocal(getNamespacesKey(), null);
        ctx.setLocal(name, varObj);
        if (appLog.isDebugEnabled()) {
            appLog.debug("<var>: Defined variable '" + name + "' with initial value '" + String.valueOf(varObj) + "'");
        }
        TriggerEvent ev = new TriggerEvent(name + ".change", TriggerEvent.CHANGE_EVENT);
        derivedEvents.add(ev);
    }
    
}
