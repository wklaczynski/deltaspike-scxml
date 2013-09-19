/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.faces;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogManager;
import org.apache.deltaspike.scxml.impl.invokers.ViewInvoker;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogNavigationHandler extends ConfigurableNavigationHandler {

    private final static Logger logger = Logger.getLogger(DialogNavigationHandler.class.getName());
    private NavigationHandler wrappedNavigationHandler;

    public DialogNavigationHandler(NavigationHandler navigationHandler) {
        this.wrappedNavigationHandler = navigationHandler;
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome) {
        ConfigurableNavigationHandler wrappedConfigurableNavigationHandler = (ConfigurableNavigationHandler) wrappedNavigationHandler;
        return wrappedConfigurableNavigationHandler.getNavigationCase(context, fromAction, outcome);
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases() {
        ConfigurableNavigationHandler wrappedConfigurableNavigationHandler = (ConfigurableNavigationHandler) wrappedNavigationHandler;
        return wrappedConfigurableNavigationHandler.getNavigationCases();
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome) {
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        if (manager.isStarted()) {
            if (outcome == null) {
                return;
            }
            SCXMLExecutor executor = manager.getRootExecutor();
            try {
                executor.triggerEvent(new TriggerEvent(ViewInvoker.OUTCOME_EVENT_PREFIX + outcome, TriggerEvent.SIGNAL_EVENT));
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
            if (executor.getCurrentStatus().isFinal()) {
                manager.stop();
            }
        } else {
            wrappedNavigationHandler.handleNavigation(context, fromAction, outcome);
        }
    }
}
