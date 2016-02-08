/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.deltaspike.scxml.api.DialogManager;
import org.apache.deltaspike.scxml.api.DialogViewParam;
import org.apache.deltaspike.scxml.impl.TypedParamValue;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogParamProducer {

    @Inject
    Instance<DialogManager> managers;

    @Produces
    @TypedParamValue
    protected Object getTypedParamValue(InjectionPoint ip) {
        Object v = getParameterValue(getParameterName(ip), ip);
        return v;
    }

    private String getParameterName(InjectionPoint ip) {
        String parameterName = ip.getAnnotated().getAnnotation(DialogViewParam.class).value();
        if (parameterName.isEmpty()) {
            parameterName = ip.getMember().getName();
        }
        return parameterName;
    }

    private Object getParameterValue(String parameterName, InjectionPoint ip) {
        Object result = null;
        DialogManager manager = managers.get();
        if (manager != null) {
            SCXMLExecutor executor = manager.getExecutor();
            if (executor != null) {
                Context context = executor.getRootContext();

                if (context.has(parameterName)) {
                    result = context.get(parameterName);
                }
            }

        }
        return result;
    }
}
