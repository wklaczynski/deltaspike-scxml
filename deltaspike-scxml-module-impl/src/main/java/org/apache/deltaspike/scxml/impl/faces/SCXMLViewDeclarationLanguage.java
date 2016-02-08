/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.faces;

import com.sun.faces.util.RequestStateManager;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.application.Resource;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
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

    public static ViewDeclarationLanguage wrapped;
    private String sufix;
    boolean lock;

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

    private String getSufix() {
        if (sufix == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            sufix = context.getExternalContext().getInitParameter("javax.faces.DIALOG_ACTION_SUFIX");
            if (sufix == null) {
                sufix = ".scxml";
            }
        }
        return sufix;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        if (lock) {
            UIViewRoot viewRoot = wrapped.createView(context, viewId);
            return viewRoot;
        } else if (viewId.endsWith(getSufix())) {
            lock = true;
            try {
                DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
                String path = viewId.substring(0, viewId.lastIndexOf(getSufix()));
                path += ".scxml";
                Flash flash = context.getExternalContext().getFlash();
                Map<String, Object> params = new LinkedHashMap<String, Object>();
                Set<String> keySet = flash.keySet();
                for (String key : keySet) {
                    params.put(key, flash.get(key));
                }
                Map<String, String> pmap = context.getExternalContext().getRequestParameterMap();
                for (String key : pmap.keySet()) {
                    params.put(key, pmap.get(key));
                }

                UIViewRoot scxmlRoot = new UIViewRoot();
                scxmlRoot.setViewId(viewId);
                UIViewRoot oldRoot = context.getViewRoot();
                try {
                    if (context.getViewRoot() == null) {
                        context.setViewRoot(scxmlRoot);
                    }
                    manager.start(path, params);
                } finally {
                    if (oldRoot != null) {
                        context.setViewRoot(oldRoot);
                    }
                }
                UIViewRoot viewRoot = context.getViewRoot();
                return viewRoot;
            } finally {
                lock = false;
            }
        } else {
            lock = true;
            try {
                DialogManager manager = BeanProvider.getContextualReference(DialogManager.class);
                UIViewRoot viewRoot = wrapped.createView(context, viewId);
                if (manager.isStarted()) {
                    ExternalContext ec = context.getExternalContext();
                    Object state = ec.getRequestMap().get("__@@DialogLastState");
                    if (state != null) {
                        context.getAttributes().put(RequestStateManager.FACES_VIEW_STATE, state);
                        StateManagementStrategy strategy = getStateManagementStrategy(context, viewId);
                        context.setViewRoot(viewRoot);
                        ViewHandler vh = context.getApplication().getViewHandler();
                        String renderKitId = vh.calculateRenderKitId(context);
                        viewRoot = strategy.restoreView(context, viewId, renderKitId);
                    }
                }

                return viewRoot;
            } finally {
                lock = false;
            }
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
