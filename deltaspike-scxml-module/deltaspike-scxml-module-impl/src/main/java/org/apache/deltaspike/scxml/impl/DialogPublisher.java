/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.servlet.ServletContext;
import org.apache.commons.scxml.PathResolver;
import org.apache.commons.scxml.env.SimpleErrorHandler;
import org.apache.commons.scxml.env.URLResolver;
import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.io.SCXMLParser;
import org.apache.commons.scxml.model.CustomAction;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.scxml.api.DialogAction;
import org.apache.deltaspike.scxml.api.DialogInvoker;
import org.xml.sax.SAXException;

/**
 *
 * @author Waldemar Kłaczyński
 */
@ApplicationScoped
public class DialogPublisher implements Serializable, Extension, Deactivatable {

    private static final Logger LOG = Logger.getLogger(DialogExtension.class.getName());
    private Boolean isActivated = true;
    private List<CustomAction> customActions = Collections.synchronizedList(new ArrayList<CustomAction>());
    private Map<String, Class<Invoker>> customInvokers = Collections.synchronizedMap(new HashMap<String, Class<Invoker>>());
    
    private ServletContext servletContext;

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public List<CustomAction> getCustomActions() {
        return customActions;
    }

    public void setCustomActions(List<CustomAction> customActions) {
        this.customActions = customActions;
    }

    public Map<String, Class<Invoker>> getCustomInvokers() {
        return customInvokers;
    }

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());

        if (this.isActivated) {
            this.isActivated = ClassUtils.tryToLoadClassForName("org.apache.commons.scxml.SCXMLExecutor") != null;

            if (!this.isActivated && LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "@{0} deactivated because common-scxml is missing.", DialogExtension.class.getName());
            }
        }
    }

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event, final BeanManager beanManager) {
        if (!this.isActivated) {
            return;
        }
        if (event.getAnnotatedType().isAnnotationPresent(DialogAction.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();
            DialogAction a = type.getAnnotation(DialogAction.class);
            Class<X> javaClass = type.getJavaClass();
            CustomAction action = new CustomAction(a.namespaceURI(), a.value(), javaClass);
            customActions.add(action);
        } else if (event.getAnnotatedType().isAnnotationPresent(DialogInvoker.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();
            DialogInvoker a = type.getAnnotation(DialogInvoker.class);
            Class<X> javaClass = type.getJavaClass();
            customInvokers.put(a.value(), (Class<Invoker>) javaClass);
        }
    }
    private Map<String, SCXML> models = new HashMap<String, SCXML>();

    public SCXML getModel(String source) {
        SCXML scxml = models.get(source);
        if (scxml == null) {
            try {
                scxml = createModel(source);
                //models.put(source, scxml);
            } catch (Throwable ex) {
                LOG.log(Level.SEVERE,"SCXML Get Model Error:", ex);
                throw new IllegalStateException(ex);
            }
        }
        return scxml;

    }

    private SCXML createModel(String source) throws IOException, SAXException, ModelException {

        if (source != null) {
            SimpleErrorHandler errHandler = new SimpleErrorHandler();
            ServletContext ctx = getServletContext();

            URL url = null;
            URL scxmlURL;
            try {
                scxmlURL = new URL(source);
            } catch (MalformedURLException malformedURLException) {
                scxmlURL = ctx.getResource(source);
            }


            if (scxmlURL == null) {
                throw new IllegalStateException(String.format("Resorce %s not found.", url.toString()));
            }
            BaseUrlResolver urlResolver = new BaseUrlResolver(scxmlURL);
            SCXML scxml = SCXMLParser.parse(
                    scxmlURL.toString(),
                    errHandler,
                    urlResolver,
                    getCustomActions());
            return scxml;
        } else {
            throw new IllegalStateException(String.format("Resorce %s not found.", source.toString()));
        }
    }

    private static class BaseUrlResolver implements PathResolver {

        private URL baseURL = null;

        public BaseUrlResolver(final URL baseURL) {
            this.baseURL = baseURL;
        }

        @Override
        public String resolvePath(String ctxPath) {
            URL combined;
            try {
                combined = new URL(baseURL, ctxPath);
                return combined.toString();
            } catch (MalformedURLException e) {
                LOG.log(Level.SEVERE,"Malformed URL", e);
            }
            return null;
        }

        @Override
        public PathResolver getResolver(String ctxPath) {
            URL combined;
            try {
                combined = new URL(baseURL, ctxPath);
                return new URLResolver(combined);
            } catch (MalformedURLException e) {
                LOG.log(Level.SEVERE,"Malformed URL", e);
            }
            return null;
        }
    }
}
