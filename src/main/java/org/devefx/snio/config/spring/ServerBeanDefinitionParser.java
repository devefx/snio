package org.devefx.snio.config.spring;

import org.devefx.snio.config.ServerFactoryBean;
import org.devefx.snio.config.spring.service.ClassBeanDefinitionParser;
import org.devefx.snio.config.spring.service.ServiceScanBeanDefinitionParser;
import org.devefx.snio.config.spring.service.ServicesBeanDefinitionParser;
import org.devefx.snio.config.spring.util.ServiceRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServerBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private ServiceRegistry registry;

    public ServerBeanDefinitionParser(Class<?> beanClass) {
        super(beanClass);
    }

    @Override
    public void init() {
        registry = new ServiceRegistry();
        registerBeanDefinitionParser("services", new ServicesBeanDefinitionParser(registry));
        registerBeanDefinitionParser("service-scan", new ServiceScanBeanDefinitionParser(registry));
        registerBeanDefinitionParser("server-initializer", new ClassBeanDefinitionParser());
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(ServerFactoryBean.class);

        GenericBeanDefinition serverDefinition = new GenericBeanDefinition();
        serverDefinition.setBeanClass(beanClass);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(serverDefinition);

        parseAttribute(element, beanDefinition);

        parseElement(element, parserContext, new ElementParser() {
            @Override
            public void parser(String name, BeanDefinition definition) {
                if ("server-initializer".equals(name)) {
                    beanDefinition.getPropertyValues().add("serverInitializer", definition);
                }
            }
        });

        beanDefinition.getPropertyValues().add("services", registry.getBeanReferences());
        return beanDefinition;
    }
}
