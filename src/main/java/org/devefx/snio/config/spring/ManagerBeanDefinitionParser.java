package org.devefx.snio.config.spring;

import org.devefx.snio.Manager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ManagerBeanDefinitionParser implements BeanDefinitionParser {

    static final String REF_ATTRIBUTE = "ref";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(ManagerFactoryBean.class);
        String ref = element.getAttribute(REF_ATTRIBUTE);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference(ref));
        return beanDefinition;
    }

    static class ManagerFactoryBean implements FactoryBean<Manager> {

        private Manager manager;

        public ManagerFactoryBean(Manager manager) {
            this.manager = manager;
        }

        @Override
        public Manager getObject() throws Exception {
            return manager;
        }

        @Override
        public Class<?> getObjectType() {
            return manager.getClass();
        }

        @Override
        public boolean isSingleton() {
            return false;
        }
    }

}
