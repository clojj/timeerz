package com.github.utiliteez.timeerz.jee.model;

import com.cronutils.model.Cron;
import com.github.utiliteez.timeerz.jee.cdiextension.BeanType;

import javax.enterprise.inject.spi.AnnotatedMethod;

public class ScheduledMethod {

    private BeanType type;
    private Class<?> clazz;
    private AnnotatedMethod<?> method;
    private Cron cron;

    private Object instance;

    public ScheduledMethod(BeanType type, Class<?> clazz, AnnotatedMethod<?> method, Cron cron) {
        this.type = type;
        this.clazz = clazz;
        this.method = method;
        this.cron = cron;
    }

    public BeanType getType() {
        return type;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public AnnotatedMethod getMethod() {
        return method;
    }

    public Cron getCron() {
        return cron;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}
