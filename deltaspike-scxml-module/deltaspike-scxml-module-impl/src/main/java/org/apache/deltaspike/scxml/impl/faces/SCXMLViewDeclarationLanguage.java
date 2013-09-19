/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.faces;

import java.beans.BeanInfo;
import java.io.IOException;
import javax.faces.application.Resource;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SCXMLViewDeclarationLanguage extends ViewDeclarationLanguage {

    ViewDeclarationLanguage wrapped;

    public SCXMLViewDeclarationLanguage(ViewDeclarationLanguage wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BeanInfo getComponentMetadata(FacesContext context, Resource componentResource) {
        return wrapped.getComponentMetadata(context, componentResource);
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId) {
        return wrapped.getViewMetadata(context, viewId);
    }

    @Override
    public Resource getScriptComponentResource(FacesContext context, Resource componentResource) {
        return wrapped.getScriptComponentResource(context, componentResource);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        String scxmlSufix = context.getExternalContext().getInitParameter("javax.faces.DIALOG_ACTION_SUFIX");
        if (scxmlSufix == null) {
            scxmlSufix = ".scxml";
        }

        if (viewId.endsWith(scxmlSufix)) {
            String path = viewId.substring(0, viewId.lastIndexOf(scxmlSufix));
            path += ".scxml";
            DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
            UIViewRoot scxmlRoot = new UIViewRoot();
            scxmlRoot.setViewId(viewId);
            UIViewRoot oldRoot = context.getViewRoot();
            try {
                if (context.getViewRoot() == null) {
                    context.setViewRoot(scxmlRoot);
                }
                manager.start(path);
            } finally {
                if (oldRoot != null) {
                    context.setViewRoot(oldRoot);
                }
            }
            UIViewRoot viewRoot = context.getViewRoot();
            return viewRoot;
        } else {
            UIViewRoot viewRoot = wrapped.createView(context, viewId);
            return viewRoot;
        }
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return wrapped.restoreView(context, viewId);
    }

    @Override
    public void buildView(FacesContext context, UIViewRoot root) throws IOException {
        wrapped.buildView(context, root);
    }

    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException {
        wrapped.renderView(context, view);
    }

    @Override
    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId) {
        return wrapped.getStateManagementStrategy(context, viewId);
    }
}
