/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SharedUtils {

    static boolean isMixedExpression(String expression) {

        if (null == expression) {
            return false;
        }

        // if it doesn't start and end with delimiters
        return (!(expression.startsWith("#{") && expression.endsWith("}")))
                && isExpression(expression);

    }


    static boolean isExpression(String expression) {

        if (null == expression) {
            return false;
        }

        //check to see if attribute has an expression
        int start = expression.indexOf("#{");
        return start != -1 && expression.indexOf('}', start + 2) != -1;
    }

    static Map<String, List<String>> evaluateExpressions(FacesContext context, Map<String, List<String>> map) {
        if (map != null && !map.isEmpty()) {
            Map<String, List<String>> ret = new HashMap<String, List<String>>(map.size());
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                ret.put(entry.getKey(), evaluateExpressions(context, entry.getValue()));
            }

            return ret;
        }

        return map;
    }

    static List<String> evaluateExpressions(FacesContext context, List<String> values) {
        if (!values.isEmpty()) {
            List<String> ret = new ArrayList<String>(values.size());
            Application app = context.getApplication();
            for (String val : values) {
                if (val != null) {
                    String value = val.trim();
                    if (isExpression(value)) {
                        value = app.evaluateExpressionGet(context, value, String.class);
                    }
                    ret.add(value);
                }
            }

            return ret;
        }
        return values;
    }
}
