package org.devefx.snio.config;

import org.devefx.snio.Service;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;

public class ServiceFactoryBean implements FactoryBean<Service> {

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