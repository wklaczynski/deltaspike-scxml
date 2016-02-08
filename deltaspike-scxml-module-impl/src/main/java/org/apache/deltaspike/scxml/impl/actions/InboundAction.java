/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.actions;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.semantics.ErrorConstants;
import org.apache.deltaspike.scxml.api.DialogAction;
import org.apache.deltaspike.scxml.impl.el.DialogELEvaluator;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogAction(value = "inbound", namespaceURI = "http://www.apache.org/scxml/actions")
public class InboundAction extends Action {

    private String name = null;
    private String expr = null;

    public InboundAction() {
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
        TransitionTarget parentTarget = getParentTransitionTarget();
        Context ctx = scInstance.getContext(parentTarget);
        DialogELEvaluator eval = (DialogELEvaluator) scInstance.getEvaluator();

        if (!ctx.has(name)) {
            errRep.onError(ErrorConstants.UNDEFINED_VARIABLE, name + " = null", parentTarget);
        } else {
            Object varObj = ctx.get(name);
            eval.evalSet(ctx, expr, varObj);
            if (appLog.isDebugEnabled()) {
                appLog.debug("<assign>: Set variable '" + name + "' to '" + expr + "'");
            }
            TriggerEvent ev = new TriggerEvent(name + ".inbound", TriggerEvent.CHANGE_EVENT);
            derivedEvents.add(ev);
        }

    }

}
