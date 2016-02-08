/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogUtils {


    public static SCXMLExecutor getExecutor() {
        DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
        return manager.getExecutor();
    }
}
