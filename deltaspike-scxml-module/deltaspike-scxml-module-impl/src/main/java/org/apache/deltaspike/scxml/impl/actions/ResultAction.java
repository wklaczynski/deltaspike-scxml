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
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.deltaspike.scxml.api.DialogAction;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogAction(value = "result", namespaceURI = "http://www.apache.org/scxml/actions")
public class ResultAction extends Action {

    private String name = null;
    private String expr = null;

    public ResultAction() {
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
        Context ctx = scInstance.getRootContext();
        Evaluator eval = scInstance.getEvaluator();
        Context result = (Context) ctx.get("__@result@__");
        if (result == null) {
            result = eval.newContext(null);
            ctx.set("__@result@__", result);
        }

        result.setLocal(getNamespacesKey(), getNamespaces());
        Object varObj = eval.eval(ctx, expr);
        result.setLocal(getNamespacesKey(), null);
        result.setLocal(name, varObj);
        if (appLog.isDebugEnabled()) {
            appLog.debug("<var>: Defined result variable '" + name + "' with initial value '" + String.valueOf(varObj) + "'");
        }
        TriggerEvent ev = new TriggerEvent("result." + name + ".change", TriggerEvent.CHANGE_EVENT);
        derivedEvents.add(ev);

    }

}
