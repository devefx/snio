package org.devefx.snio.config.spring.service;

import org.devefx.snio.config.spring.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ServicesBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private ManagedSet<BeanDefinition> serviceManagedSet;

    public ServicesBeanDefinitionParser(ManagedSet<BeanDefinition> serviceManagedList) {
        super(null);
        this.serviceManagedSet = serviceManagedList;
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
                serviceManagedSet.add(definition);
            }
        });
        return null;
    }

}
