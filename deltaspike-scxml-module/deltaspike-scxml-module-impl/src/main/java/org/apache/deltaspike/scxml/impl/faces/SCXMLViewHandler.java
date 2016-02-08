package org.apache.deltaspike.scxml.impl.faces;

/*
 * SCXMLViewHandler.java
 *
 * Created on 6 listopad 2007, 21:38
 *
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SCXMLViewHandler extends ViewHandlerWrapper {

    private static final String LOCK = "org.apache.deltaspike.scxml.impl.faces.SCXMLViewHandler:Lock";

    private final ViewHandler wrapped;
    private String sufix;

    public SCXMLViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ViewHandler getWrapped() {
        return this.wrapped;
    }

    private boolean isLocked(FacesContext context) {
        if (context.getAttributes().containsKey(LOCK)) {
            return (Boolean) context.getAttributes().get(LOCK);
        } else {
            return false;
        }
    }

    private void lock(FacesContext context) {
        context.getAttributes().put(LOCK, true);
    }

    private void unlock(FacesContext context) {
        context.getAttributes().remove(LOCK);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        if (isLocked(context)) {
            return super.createView(context, viewId);
        } else {
            lock(context);
            try {
                if (viewId.endsWith(getSufix())) {
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
                } else {
                    UIViewRoot viewRoot = super.createView(context, viewId);
                    return viewRoot;
                }
            } finally {
                unlock(context);
            }
        }
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

}
