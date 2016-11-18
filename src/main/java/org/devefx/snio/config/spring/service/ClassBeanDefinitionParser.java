package org.devefx.snio.config.spring.service;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ClassBeanDefinitionParser implements BeanDefinitionParser {

    static final String CLASS_ATTRIBUTE = "class";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName(element.getAttribute(CLASS_ATTRIBUTE));
        return beanDefinition;
    }
}
