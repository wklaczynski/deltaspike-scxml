/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import org.apache.deltaspike.scxml.api.DialogViewParam;
import org.apache.deltaspike.scxml.impl.TypedViewParamValue;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogViewParamProducer {

    @Inject
    Instance<ViewParamsContext> context;

    @Produces
    @TypedViewParamValue
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
        if (context.get().containsKey(parameterName)) {
            result = context.get().get(parameterName);
        }
        return result;
    }
}
