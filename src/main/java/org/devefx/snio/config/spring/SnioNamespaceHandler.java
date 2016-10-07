package org.devefx.snio.config.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SnioNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        this.registerBeanDefinitionParser("engine", new EngineBeanDefinitionParser());
    }
}
