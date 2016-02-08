/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.enterprise.context.NormalScope;

@Target(value={METHOD,TYPE,FIELD})
@Retention(value=RUNTIME)
@NormalScope
@Inherited
public @interface StateScoped {
    
}
