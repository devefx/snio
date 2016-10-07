package org.devefx.snio.config;

import org.devefx.snio.Service;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;

public class ServiceFactoryBean implements InitializingBean, FactoryBean<Service>, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Service service;

    public ServiceFactoryBean(Service service) {
        this.service = service;
    }

    @Override
    public Service getObject() throws Exception {
        return service;
    }

    @Override
    public Class<?> getObjectType() {
        return service.getClass();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Field field : getObjectType().getDeclaredFields()) {
            Object bean = null;
            if (field.isAnnotationPresent(Resource.class)) {
                Resource resource = field.getAnnotation(Resource.class);
                String beanName = resource.name();
                if (beanName == null || beanName.isEmpty()) {
                    beanName = field.getName();
                }
                if (applicationContext.containsBean(beanName)) {
                    bean = applicationContext.getBean(beanName);
                } else {
                    bean = applicationContext.getBean(field.getType());
                }
            } else if (field.isAnnotationPresent(Autowired.class)) {
                bean = applicationContext.getBean(field.getType());
            }
            if (bean != null) {
                field.setAccessible(true);
                field.set(service, bean);
            }
        }
    }

    public static class ServicesFactoryBean implements FactoryBean<List<Service>> {
        private List<Service> services;

        public ServicesFactoryBean(List<Service> services) {
            this.services = services;
        }

        @Override
        public List<Service> getObject() throws Exception {
            return services;
        }

        @Override
        public Class<?> getObjectType() {
            return services.getClass();
        }

        @Override
        public boolean isSingleton() {
            return false;
        }
    }
}