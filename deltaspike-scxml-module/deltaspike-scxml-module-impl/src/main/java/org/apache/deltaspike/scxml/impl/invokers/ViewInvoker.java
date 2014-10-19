/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.invokers;

import org.apache.deltaspike.scxml.impl.AsyncTrigger;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
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
    private static String invokePrefix = ".view.";
    private PathResolver pathResolver;

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
                String redirect = viewHandler.getRedirectURL(fc, viewId, SharedUtils.evaluateExpressions(fc, param), true);
                clearViewMapIfNecessary(fc.getViewRoot(), viewId);
                updateRenderTargets(fc, viewId);
                ec.getFlash().setRedirect(true);
                ec.getFlash().setKeepMessages(true);
                ec.redirect(redirect);
                fc.responseComplete();
            } else {
                UIViewRoot view = vh.createView(fc, viewId);
                view.getViewMap(true).put(VIEW_PARAMS_MAP, params);
                view.getViewMap(true).putAll(params);
                view.setViewId(viewId);
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
}
