/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api.literal;

import javax.enterprise.util.AnnotationLiteral;
import org.apache.deltaspike.scxml.api.DialogViewParam;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogViewParamLiteral extends AnnotationLiteral<DialogViewParam> implements DialogViewParam {

    private final String value;

    public DialogViewParamLiteral() {
        this("");
    }

    public DialogViewParamLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
    
     public static final DialogViewParamLiteral INSTANCE = new DialogViewParamLiteral();
    
}
