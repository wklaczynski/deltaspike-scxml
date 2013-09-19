/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 *
 * @author Waldemar Kłaczyński
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Documented
@Target({TYPE, METHOD, PARAMETER, FIELD})
public @interface TypedParamValue {
}
