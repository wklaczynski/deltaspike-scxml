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
import javax.faces.application.Application;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import javax.servlet.ServletContext;
import org.apache.commons.scxml.*;
import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.invoke.InvokerException;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.PathResolverHolder;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;
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
    private String statePrefix;
    private boolean cancelled;
    private SCInstance parentSCInstance;
    private static final String invokePrefix = ".view.";
    private PathResolver pathResolver;
    private String stateStore;
    private String control;
    private String viewId;

    @Override
    public void setParentStateId(String parentStateId) {
        this.parentStateId = parentStateId;
        this.eventPrefix = this.parentStateId + invokePrefix;
        this.statePrefix = this.parentStateId + "view.state.";
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
            viewId = url.getFile();
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
            int pos = viewId.indexOf("META-INF/resources/");
            if (pos>=0) {
                viewId = viewId.substring(pos + 18);
            }

            NavigationCase navCase = findNavigationCase(fc, viewId);
            viewId = navCase.getToViewId(fc);

            ViewHandler vh = fc.getApplication().getViewHandler();

            Map<String, Object> options = new HashMap();
            Map<String, Object> vieparams = new HashMap();
            for (Object key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (skey.startsWith("@view.")) {
                    skey = skey.substring(6);
                    options.put(skey, value.toString());
                } else if (value != null) {
                    vieparams.put(skey, value);
                }
            }

            if (options.containsKey("store")) {
                stateStore = (String) options.get("store");
            } else {
                stateStore = "parallel";
            }
            boolean trans = false;
            if (options.containsKey("transient")) {
                Object val = options.get("transient");
                if (val instanceof String) {
                    trans = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    trans = (Boolean) val;
                }
            }

            if (trans) {
                control = "stateless";
            } else {
                control = "statefull";
            }

            Object viewState = null;
            if (control.equals("statefull")) {
                SCXMLExecutor executor = parentSCInstance.getExecutor();
                Iterator iterator = executor.getCurrentStatus().getStates().iterator();
                State state = ((State) iterator.next());
                String stateKey = "";
                TransitionTarget target = state;
                while (target != null) {
                    stateKey = target.getId() + ":" + stateKey;
                    target = state.getParent();
                }
                Context stateContext = parentSCInstance.getContext(state);
                if (!stateKey.endsWith(":")) {
                    stateKey += ":";
                }
                stateKey = "__@@" + stateKey;

                viewState = stateContext.get(stateKey + "ViewState");
                String lastViewId = (String) stateContext.get(stateKey + "LastViewId");
                if (lastViewId != null) {
                    viewId = lastViewId;
                }

            }

            if (fc.getViewRoot() != null) {
                String currentViewId = fc.getViewRoot().getViewId();
                if (currentViewId.equals(viewId)) {
                    return;
                }
            }

            PartialViewContext pvc = fc.getPartialViewContext();
            if (pvc != null && pvc.isAjaxRequest()) {
                Map<String, List<String>> param = new HashMap<String, List<String>>();
                Map<String, List<String>> navparams = navCase.getParameters();
                if (navparams != null) {
                    params.putAll(navparams);
                }

                Iterator<Map.Entry<String, Object>> it = ((Map<String, Object>) vieparams).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> p = it.next();
                    param.put(p.getKey(), Collections.singletonList(p.getValue().toString()));
                }

                Application application = fc.getApplication();
                ViewHandler viewHandler = application.getViewHandler();

                if (viewState != null) {
                    RenderKit renderKit = fc.getRenderKit();
                    ResponseStateManager rsm = renderKit.getResponseStateManager();
                    String viewStateId = rsm.getViewState(fc, viewState);
                    param.put(ResponseStateManager.VIEW_STATE_PARAM, Arrays.asList(viewStateId));
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
                if (viewState != null) {
                    fc.getAttributes().put(RequestStateManager.FACES_VIEW_STATE, viewState);
                    view = vh.restoreView(fc, viewId);
                    fc.setViewRoot(view);
                    vh.initView(fc);
                } else {
                    view = vh.createView(fc, viewId);
                    view.setViewId(viewId);
                }
                view.getViewMap(true).put(VIEW_PARAMS_MAP, vieparams);
                view.getViewMap(true).putAll(vieparams);
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
                String outcome = event.getName().substring(OUTCOME_EVENT_PREFIX.length());
                TriggerEvent te = new TriggerEvent(eventPrefix + outcome, TriggerEvent.SIGNAL_EVENT);
                ExternalContext ec = context.getExternalContext();

                SCXMLExecutor executor = parentSCInstance.getExecutor();
                Iterator iterator = executor.getCurrentStatus().getStates().iterator();
                State state = ((State) iterator.next());
                Context stateContext = parentSCInstance.getContext(state);

                Map<String, String> parameterMap = ec.getRequestParameterMap();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    stateContext.setLocal(entry.getKey(), entry.getValue());
                }

                if (control.equals("statefull")) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    UIViewRoot viewRoot = fc.getViewRoot();
                    if (viewRoot != null) {
                        String lastViewId = viewRoot.getViewId();
                        RenderKit renderKit = fc.getRenderKit();
                        ResponseStateManager rsm = renderKit.getResponseStateManager();
                        Object viewState = rsm.getState(fc, lastViewId);
                        String stateKey = "";
                        TransitionTarget storeTarget = null;
                        TransitionTarget target = state;
                        while (target != null) {
                            stateKey = target.getId() + ":" + stateKey;
                            if (storeTarget == null) {
                                if ("state".equals(stateStore) && target instanceof State) {
                                    storeTarget = target;
                                }
                                if ("parallel".equals(stateStore) && target instanceof Parallel) {
                                    storeTarget = target;
                                }
                            }
                            target = state.getParent();
                        }
                        Context storeContext = parentSCInstance.getRootContext();

                        if (storeTarget != null) {
                            storeContext = parentSCInstance.getContext(storeTarget);
                        }
                        if (!stateKey.endsWith(":")) {
                            stateKey += ":";
                        }
                        stateKey = "__@@" + stateKey;

                        storeContext.setLocal(stateKey + "ViewState", viewState);
                        storeContext.setLocal(stateKey + "LastViewId", lastViewId);
                    }
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

    protected NavigationCase findNavigationCase(FacesContext context, String outcome) {
        ConfigurableNavigationHandler navigationHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
        return navigationHandler.getNavigationCase(context, null, outcome);
    }

}
