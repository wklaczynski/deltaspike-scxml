/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api.literal;

import javax.enterprise.util.AnnotationLiteral;
import org.apache.deltaspike.scxml.api.DialogParam;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogParamLiteral extends AnnotationLiteral<DialogParam> implements DialogParam {

    private final String value;

    public DialogParamLiteral() {
        this("");
    }

    public DialogParamLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
    
     public static final DialogParamLiteral INSTANCE = new DialogParamLiteral();
    
}
