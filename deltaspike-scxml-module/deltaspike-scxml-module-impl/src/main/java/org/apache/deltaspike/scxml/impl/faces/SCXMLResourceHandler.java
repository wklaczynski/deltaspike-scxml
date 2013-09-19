/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.faces;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SCXMLResourceHandler extends ResourceHandlerWrapper {

    private final static Logger logger = Logger.getLogger(SCXMLResourceHandler.class.getName());
    private ResourceHandler wrapped;

    public SCXMLResourceHandler(ResourceHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ResourceHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public void handleResourceRequest(FacesContext context) throws IOException {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        super.handleResourceRequest(context);
    }
}
