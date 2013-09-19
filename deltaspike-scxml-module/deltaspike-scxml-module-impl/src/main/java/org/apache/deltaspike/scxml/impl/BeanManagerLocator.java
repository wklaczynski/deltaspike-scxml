/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class BeanManagerLocator {
    
    public BeanManager getBeanManager() {
        try {
            InitialContext initialContext = new InitialContext();
            return (BeanManager) initialContext.lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            throw new IllegalStateException("Couldn't get BeanManager through JNDI");
        }
    }
    
}
