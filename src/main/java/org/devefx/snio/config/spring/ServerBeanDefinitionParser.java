package org.devefx.snio.config.spring;

import org.devefx.snio.config.ServerFactoryBean;
import org.devefx.snio.config.spring.service.ClassBeanDefinitionParser;
import org.devefx.snio.config.spring.service.ServiceScanBeanDefinitionParser;
import org.devefx.snio.config.spring.service.ServicesBeanDefinitionParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServerBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private ManagedSet<BeanDefinition> serviceManagedSet;

    public ServerBeanDefinitionParser(Class<?> beanClass) {
        super(beanClass);
    }

    @Override
    public void init() {
        serviceManagedSet = new ManagedSet<>();
        registerBeanDefinitionParser("services", new ServicesBeanDefinitionParser(serviceManagedSet));
        registerBeanDefinitionParser("service-scan", new ServiceScanBeanDefinitionParser(serviceManagedSet));
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

        beanDefinition.getPropertyValues().add("services", serviceManagedSet);
        return beanDefinition;
    }
}
