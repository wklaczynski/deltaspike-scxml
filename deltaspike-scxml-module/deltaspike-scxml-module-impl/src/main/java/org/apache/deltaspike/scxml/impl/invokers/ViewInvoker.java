/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import com.sun.faces.util.RequestStateManager;
import org.apache.deltaspike.scxml.impl.AsyncTrigger;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.servlet.ServletContext;
import org.apache.commons.scxml.*;
import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.invoke.InvokerException;
import org.apache.commons.scxml.model.PathResolverHolder;
import org.apache.commons.scxml.model.State;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.scxml.api.DialogInvoker;
import org.apache.deltaspike.scxml.impl.DialogPublisher;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogInvoker("view")
public class ViewInvoker implements Invoker, Serializable, PathResolverHolder {

    public static final String OUTCOME_EVENT_PREFIX = "faces.outcome.";
    public static final String VIEW_PARAMS_MAP = "___@@@ParamsMap____";
    private String parentStateId;
    private String eventPrefix;
    private String paramPrefix;
    private boolean cancelled;
    private SCInstance parentSCInstance;
    private static final String invokePrefix = ".view.";
    private PathResolver pathResolver;
    private Object state;

    @Override
    public void setParentStateId(String parentStateId) {
        this.parentStateId = parentStateId;
        this.eventPrefix = this.parentStateId + invokePrefix;
        this.paramPrefix = "view.result.";
        this.cancelled = false;
    }

    @Override
    public void setSCInstance(SCInstance scInstance) {
        this.parentSCInstance = scInstance;
    }

    @Override
    public void invoke(String source, Map params) throws InvokerException {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            ViewParamsContext viewParamsContext = BeanProvider.getContextualReference(ViewParamsContext.class);
            viewParamsContext.putAll(params);
            URL url = new URL(source);
            String viewId = url.getFile();
            DialogPublisher publisher = BeanProvider.getContextualReference(DialogPublisher.class);
            ServletContext ctx = publisher.getServletContext();
            String realPath = ctx.getRealPath("/");
            if (viewId.contains(realPath)) {
                viewId = viewId.substring(realPath.length());
            }
            String contextPath = ctx.getContextPath();
            if (viewId.contains(contextPath)) {
                viewId = viewId.substring(viewId.indexOf(contextPath));
                viewId = viewId.substring(contextPath.length());
            }

//            String lastViewId = (String) ec.getRequestMap().get("__@@DialogLastViewId");
//            Object state = null;
//            if (viewId.equals(lastViewId)) {
//                state = ec.getRequestMap().get("__@@DialogLastState");
//            }

            ViewHandler vh = fc.getApplication().getViewHandler();
            if (fc.getViewRoot() != null) {
                String currentViewId = fc.getViewRoot().getViewId();
                if (currentViewId.equals(viewId)) {
                    return;
                }
            }

            PartialViewContext pvc = fc.getPartialViewContext();
            if (pvc != null && pvc.isAjaxRequest()) {
                Map<String, List<String>> param = new HashMap<String, List<String>>();
                Iterator<Map.Entry<String, Object>> it = ((Map<String, Object>) params).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> p = it.next();
                    param.put(p.getKey(), Collections.singletonList(p.getValue().toString()));
                }

                Application application = fc.getApplication();
                ViewHandler viewHandler = application.getViewHandler();

                if (state != null) {
                    RenderKit renderKit = fc.getRenderKit();
                    ResponseStateManager rsm = renderKit.getResponseStateManager();
                    String viewState = rsm.getViewState(fc, state);
                    param.put(ResponseStateManager.VIEW_STATE_PARAM, Arrays.asList(viewState));
                }

                String redirect = viewHandler.getRedirectURL(fc, viewId, SharedUtils.evaluateExpressions(fc, param), true);
                clearViewMapIfNecessary(fc.getViewRoot(), viewId);
                updateRenderTargets(fc, viewId);
                ec.getFlash().setRedirect(true);
                ec.getFlash().setKeepMessages(true);
                ec.redirect(redirect);
                fc.responseComplete();
            } else {
                UIViewRoot view;
                if (state != null) {
                    fc.getAttributes().put(RequestStateManager.FACES_VIEW_STATE, state);
                    view = vh.restoreView(fc, viewId);
                } else {
                    view = vh.createView(fc, viewId);
                }
                view.setViewId(viewId);
                view.getViewMap(true).put(VIEW_PARAMS_MAP, params);
                view.getViewMap(true).putAll(params);
                fc.setViewRoot(view);
                fc.renderResponse();
            }
        } catch (MalformedURLException ex) {
            throw new InvokerException(ex);
        } catch (IOException ex) {
            throw new InvokerException(ex);
        }
    }

    private void clearViewMapIfNecessary(UIViewRoot root, String newId) {

        if (root != null && !root.getViewId().equals(newId)) {
            Map<String, Object> viewMap = root.getViewMap(false);
            if (viewMap != null) {
                viewMap.clear();
            }
        }

    }

    private void updateRenderTargets(FacesContext ctx, String newId) {

        if (ctx.getViewRoot() == null || !ctx.getViewRoot().getViewId().equals(newId)) {
            PartialViewContext pctx = ctx.getPartialViewContext();
            if (!pctx.isRenderAll()) {
                pctx.setRenderAll(true);
            }
        }

    }

    @Override
    public void parentEvents(TriggerEvent[] evts) throws InvokerException {
        if (cancelled) {
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot view = context.getViewRoot();
        Map params = (Map) view.getViewMap(true).get(VIEW_PARAMS_MAP);
        if (params != null) {
            ViewParamsContext viewParamsContext = BeanProvider.getContextualReference(ViewParamsContext.class);
            viewParamsContext.putAll(params);
        }

        for (TriggerEvent event : evts) {
            if (event.getType() == TriggerEvent.SIGNAL_EVENT && event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
                SCXMLExecutor executor = parentSCInstance.getExecutor();
                String outcome = event.getName().substring(OUTCOME_EVENT_PREFIX.length());
                TriggerEvent te = new TriggerEvent(eventPrefix + outcome, TriggerEvent.SIGNAL_EVENT);
                ExternalContext ec = context.getExternalContext();

                Iterator iterator = executor.getCurrentStatus().getStates().iterator();
                State state = ((State) iterator.next());
                Context stateContext = parentSCInstance.getContext(state);

                Map<String, String> parameterMap = ec.getRequestParameterMap();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    stateContext.setLocal(entry.getKey(), entry.getValue());
                }
                new AsyncTrigger(executor, te).start();
            }
        }
    }

    @Override
    public void cancel() throws InvokerException {
        ViewParamsContext context = BeanProvider.getContextualReference(ViewParamsContext.class);
        context.clear();

        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = fc.getViewRoot();
        if (viewRoot != null) {
            String lastViewId = viewRoot.getViewId();
            RenderKit renderKit = fc.getRenderKit();
            ResponseStateManager rsm = renderKit.getResponseStateManager();
            state = rsm.getState(fc, lastViewId);
        }

        cancelled = true;
    }

    @Override
    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    @Override
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    public static void restoreView(String viewId, Object state) {
        FacesContext fc = FacesContext.getCurrentInstance();
        String currentViewId = fc.getViewRoot().getViewId();

        if (viewId != null && !viewId.equals(currentViewId)) {
            ViewHandler vh = fc.getApplication().getViewHandler();

            try {
                fc.setProcessingEvents(false);
                ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(fc, viewId);
                UIViewRoot viewRoot = vdl.getViewMetadata(fc, viewId).createMetadataView(fc);
                fc.setViewRoot(viewRoot);
                Object[] rawState = (Object[]) state;
                if (rawState != null) {
                    Map<String, Object> rstate = (Map<String, Object>) rawState[1];
                    if (rstate != null) {
                        String cid = viewRoot.getClientId(fc);
                        Object stateObj = rstate.get(cid);
                        if (stateObj != null) {
                            fc.getAttributes().put("com.sun.faces.application.view.restoreViewScopeOnly", true);
                            viewRoot.restoreState(fc, stateObj);
                            fc.getAttributes().remove("com.sun.faces.application.view.restoreViewScopeOnly");
                        }
                    }
                }
                fc.setProcessingEvents(true);
                vdl.buildView(fc, viewRoot);
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }

            UIViewRoot root = vh.restoreView(fc, viewId);
            root.setViewId(viewId);
            fc.setViewRoot(root);
        }
    }

}
