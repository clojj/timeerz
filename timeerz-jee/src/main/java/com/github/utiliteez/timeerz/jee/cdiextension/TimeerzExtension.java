package com.github.utiliteez.timeerz.jee.cdiextension;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.github.utiliteez.timeerz.jee.TimeerzManager;
import com.github.utiliteez.timeerz.jee.annotation.Timeer;
import com.github.utiliteez.timeerz.jee.model.ScheduledMethod;

import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeerzExtension<R> implements Extension {

    private static final Set<Class<? extends Annotation>> EJB_ANNOTATIONS = new HashSet<>();
    static {
        EJB_ANNOTATIONS.add(Stateless.class);
        EJB_ANNOTATIONS.add(Singleton.class);
    };

    private BeanManager beanManager;

    private List<ScheduledMethod> scheduledMethods = new ArrayList<>();

    private CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

    public void addTimers(@Observes @WithAnnotations({Timeer.class}) ProcessAnnotatedType<R> pat, BeanManager beanManager) {
        this.beanManager = beanManager;
        AnnotatedType<R> at = pat.getAnnotatedType();
        for (AnnotatedMethod<? super R> method : at.getMethods()) {
            Timeer annotation = method.getAnnotation(Timeer.class);
            if (annotation != null) {
                BeanType type = EJB_ANNOTATIONS.stream().anyMatch(at::isAnnotationPresent) ? BeanType.EJB : BeanType.CDI;
                Class<?> clazz = method.getJavaMember().getDeclaringClass();
                Cron cron = parser.parse(annotation.value());

	            ScheduledMethod scheduledMethod = new ScheduledMethod(type, clazz, method, cron, annotation.exclusive());
	            scheduledMethods.add(scheduledMethod);
            }
        }
    }

    public void afterDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation) {
        // move list of beans to SimpleTimersInit
        Set<Bean<?>> beans = beanManager.getBeans(TimeerzManager.class);
        final Bean<?> appInitBean = beanManager.resolve(beans);
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(appInitBean);
        Object instance = beanManager.getReference(appInitBean, TimeerzManager.class, creationalContext);
        ((TimeerzManager) instance).getScheduledMethods().addAll(scheduledMethods);
    }
}
