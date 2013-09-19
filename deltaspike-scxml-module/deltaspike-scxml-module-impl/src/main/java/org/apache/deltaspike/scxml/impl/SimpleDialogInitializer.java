/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author Waldemar Kłaczyński
 */
@WebListener
public class SimpleDialogInitializer implements ServletContextListener, ServletRequestListener {

    @Inject
    DialogPublisher publisher;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        publisher.setServletContext(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        publisher.setServletContext(null);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
    }
}
