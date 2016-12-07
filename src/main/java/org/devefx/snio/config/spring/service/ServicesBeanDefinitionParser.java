package org.devefx.snio.config.spring.service;

import org.devefx.snio.config.spring.AbstractBeanDefinitionParser;
import org.devefx.snio.config.spring.util.ServiceRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServicesBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private ServiceRegistry registry;

    public ServicesBeanDefinitionParser(ServiceRegistry registry) {
        super(null);
        this.registry = registry;
    }

    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ClassBeanDefinitionParser());
    }

    @Override
    public BeanDefinition parse(Element element, final ParserContext parserContext) {
        parseElement(element, parserContext, new ElementParser() {
            @Override
            public void parser(String name, BeanDefinition definition) {
                registry.register(definition, parserContext.getRegistry());
            }
        });
        return null;
    }

}
