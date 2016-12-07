package org.devefx.snio.config.spring.util;

import org.devefx.snio.Service;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;

public class ServiceRegistry {

    static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    public ManagedSet<BeanReference> beanReferences = new ManagedSet<>();

    public ManagedSet<BeanReference> getBeanReferences() {
        return beanReferences;
    }

    public void register(BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
        String beanClassName = beanDefinition.getBeanClassName();

        Class<?> beanType = loadClass(beanClassName);
        if (beanType == null) {
            return;
        }

        boolean isService = isService(beanType);
        boolean isHandler = isHandler(beanType);

        if (isHandler || isService) {

            String beanName = findBeanDefinitionName(beanClassName, registry);
            if (beanName == null) {
                beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
                registry.registerBeanDefinition(beanName, beanDefinition);
            }

            if (isService) {
                beanReferences.add(new RuntimeBeanReference(beanName));
            }
        }

    }

    private String findBeanDefinitionName(String beanClassName, BeanDefinitionRegistry registry) {
        String[] names = registry.getBeanDefinitionNames();
        for (String beanName : names) {
            BeanDefinition definition = registry.getBeanDefinition(beanName);
            if (definition.getBeanClassName().equals(beanClassName)) {
                return beanName;
            }
        }
        return null;
    }

    private Class<?> loadClass(String className) {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    private boolean isService(Class<?> beanType) {
        return Service.class.isAssignableFrom(beanType);
    }

    private boolean isHandler(Class<?> beanType) {
        return AnnotationUtils.findAnnotation(beanType, Controller.class) != null;
    }

}
