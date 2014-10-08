/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.*;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.scxml.api.DialogParam;
import org.apache.deltaspike.scxml.api.DialogViewParam;
import org.apache.deltaspike.scxml.api.literal.DialogParamLiteral;
import org.apache.deltaspike.scxml.impl.context.DialogContextImpl;
import org.apache.deltaspike.scxml.impl.context.ParallelContextImpl;
import org.apache.deltaspike.scxml.impl.context.StateContextImpl;
import org.apache.deltaspike.scxml.impl.invokers.DialogViewParamProducer;
import org.apache.deltaspike.scxml.api.literal.DialogViewParamLiteral;
import org.apache.deltaspike.scxml.impl.invokers.DialogParamProducer;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogExtension implements Extension, Deactivatable {

    private static final Logger LOG = Logger.getLogger(DialogExtension.class.getName());
    private Boolean isActivated = true;
    private final Map<Class<? extends Annotation>, TypedParamProducerBlueprint> producerBlueprints;
    private final Map<Class<?>, Member> converterMembersByType;

    public DialogExtension() {
        producerBlueprints = new HashMap<Class<? extends Annotation>, TypedParamProducerBlueprint>();
        producerBlueprints.put(DialogViewParam.class, new TypedParamProducerBlueprint(DialogViewParamLiteral.INSTANCE));
        producerBlueprints.put(DialogParam.class, new TypedParamProducerBlueprint(DialogParamLiteral.INSTANCE));
        converterMembersByType = new HashMap<Class<?>, Member>();
    }

    public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event, BeanManager manager) {
        AnnotatedTypeBuilder<T> modifiedType = null;
        for (AnnotatedField<? super T> field : event.getAnnotatedType().getFields()) {
            boolean bootstrapped = false;
    
        }

        for (AnnotatedMethod<? super T> method : event.getAnnotatedType().getMethods()) {
            if (method.isAnnotationPresent(DialogParam.class)) {
            }
        }
    
    }

        
            
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());

        if (this.isActivated) {
            this.isActivated = ClassUtils.tryToLoadClassForName("org.apache.commons.scxml.SCXMLExecutor") != null;

            if (!this.isActivated && LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "@{0} deactivated because common-scxml is missing.", DialogExtension.class.getName());
            }
        }
        if (this.isActivated) {
            event.addContext(new DialogContextImpl(manager));
            event.addContext(new StateContextImpl(manager));
            event.addContext(new ParallelContextImpl(manager));
        }
    }

    void processRequestViewParamProducer(@Observes ProcessProducerMethod<Object, DialogViewParamProducer> event) {
        if (!this.isActivated) {
            return;
        }
        if (event.getAnnotatedProducerMethod().getBaseType().equals(Object.class) && event.getAnnotatedProducerMethod().isAnnotationPresent(TypedViewParamValue.class)) {
            producerBlueprints.get(DialogViewParam.class).setProducer(event.getBean());
        }
    }

    void processRequestParamProducer(@Observes ProcessProducerMethod<Object, DialogParamProducer> event) {
        if (!this.isActivated) {
            return;
        }
        if (event.getAnnotatedProducerMethod().getBaseType().equals(Object.class) && event.getAnnotatedProducerMethod().isAnnotationPresent(TypedParamValue.class)) {
            producerBlueprints.get(DialogParam.class).setProducer(event.getBean());
        }
    }
    
    
    <X> void detectInjections(@Observes ProcessInjectionTarget<X> event) {
        if (!this.isActivated) {
            return;
        }
        for (InjectionPoint ip : event.getInjectionTarget().getInjectionPoints()) {
            Annotated annotated = ip.getAnnotated();
            for (Class<? extends Annotation> paramAnnotationType : producerBlueprints.keySet()) {
                if (annotated.isAnnotationPresent(paramAnnotationType)) {
                    Collection<Annotation> allowed = Arrays.asList(new DefaultLiteral(), new AnyLiteral(), annotated.getAnnotation(paramAnnotationType));
                    boolean error = false;
                    for (Annotation q : ip.getQualifiers()) {
                        if (!allowed.contains(q)) {
                            event.addDefinitionError(new IllegalArgumentException(String.format("Additional qualifiers not permitted at @%s injection point: %s", ip)));
                            error = true;
                            break;
                        }
                    }
                    if (error) {
                        break;
                    }
                    Type targetType = getActualBeanType(ip.getType());
                    if (!(targetType instanceof Class)) {
                        event.addDefinitionError(new IllegalArgumentException(String.format("@%s injection point must be a raw type: %s",
                                paramAnnotationType.getSimpleName(), ip)));
                        break;
                    }
                    Class<?> targetClass = (Class<?>) targetType;
                    if (!Object.class.isAssignableFrom(targetClass)) {
                        event.addDefinitionError(new IllegalArgumentException(String.format("No assignable type at @%s injection point: %s",
                                paramAnnotationType.getSimpleName(), ip)));
                    } else {
                        producerBlueprints.get(paramAnnotationType).addTargetType(targetClass);
                    }
                }
            }
        }
    }

    void installBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        if (!this.isActivated) {
            return;
        }
        for (TypedParamProducerBlueprint blueprint : producerBlueprints.values()) {
            if (blueprint.getProducer() != null) {
                for (Class<?> type : blueprint.getTargetTypes()) {
                    event.addBean(createTypedParamProducer(blueprint.getProducer(), type, blueprint.getQualifier(), beanManager));
                }
            }
        }

        producerBlueprints.clear();
    }

    private <T> Bean<T> createTypedParamProducer(Bean<Object> delegate, Class<T> targetType, Annotation qualifier, BeanManager beanManager) {
        BeanBuilder<T> beanBuilder = new BeanBuilder<T>(beanManager)
                .readFromType(beanManager.createAnnotatedType(targetType))
                .addQualifier(qualifier);
        return beanBuilder.create();
    }

    private Type getActualBeanType(Type t) {
        if (t instanceof ParameterizedType && ((ParameterizedType) t).getRawType().equals(Instance.class)) {
            return ((ParameterizedType) t).getActualTypeArguments()[0];
        }
        return t;
    }

    public static class TypedParamProducerBlueprint {

        private Bean<Object> producer;
        private final Set<Class<?>> targetTypes;
        private final Annotation qualifier;

        public TypedParamProducerBlueprint(Annotation qualifier) {
            this.qualifier = qualifier;
            targetTypes = new HashSet<Class<?>>();
        }

        public Bean<Object> getProducer() {
            return producer;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void setProducer(Bean producer) {
            this.producer = producer;
        }

        public Set<Class<?>> getTargetTypes() {
            return targetTypes;
        }

        public void addTargetType(Class<?> targetType) {
            targetTypes.add(targetType);
        }

        public Annotation getQualifier() {
            return qualifier;
        }
    }
}
