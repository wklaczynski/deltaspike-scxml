/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.actions;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.ModelException;
import org.apache.deltaspike.scxml.api.DialogAction;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogAction(value="var", namespaceURI="http://www.apache.org/scxml/deltaspike")
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

    }
}
