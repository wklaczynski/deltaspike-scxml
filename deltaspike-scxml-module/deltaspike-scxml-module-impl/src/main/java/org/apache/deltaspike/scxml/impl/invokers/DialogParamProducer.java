/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import org.apache.deltaspike.scxml.api.DialogParam;
import org.apache.deltaspike.scxml.impl.TypedParamValue;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogParamProducer {

    @Inject
    Instance<ViewParamsContext> context;

    @Produces
    @TypedParamValue
    protected Object getTypedParamValue(InjectionPoint ip) {
        Object v = getParameterValue(getParameterName(ip), ip);
        return v;
    }

    private String getParameterName(InjectionPoint ip) {
        String parameterName = ip.getAnnotated().getAnnotation(DialogParam.class).value();
        if ("".equals(parameterName)) {
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
