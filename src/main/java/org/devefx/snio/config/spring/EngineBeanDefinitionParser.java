package org.devefx.snio.config.spring;

import org.devefx.snio.config.EngineFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class EngineBeanDefinitionParser extends AbstractBeanDefinitionParser {

    public EngineBeanDefinitionParser() {
        super(EngineFactoryBean.class);
    }

    @Override
    public void init() {
        registerBeanDefinitionParser("servers", new ServersBeanDefinitionParser());
        registerBeanDefinitionParser("manager", new ManagerBeanDefinitionParser());
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(beanClass);

        String beanName = BeanDefinitionReaderUtils.generateBeanName(
                beanDefinition, parserContext.getRegistry(), false);

        parseAttribute(element, beanDefinition);

        parseElement(element, parserContext, new ElementParser() {
            @Override
            public void parser(String name, BeanDefinition definition) {
                beanDefinition.getPropertyValues().add(name, definition);
            }
        });

        parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
        return beanDefinition;
    }

}
