package org.devefx.snio.config.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class DecoderBeanDefinitionParser implements BeanDefinitionParser {
    private static final String CLASS_ATTRIBUTE = "class";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        String className = element.getAttribute(CLASS_ATTRIBUTE);
        beanDefinition.setBeanClassName(className);
        return beanDefinition;
    }
}
