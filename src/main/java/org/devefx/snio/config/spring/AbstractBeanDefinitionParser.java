package org.devefx.snio.config.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractBeanDefinitionParser extends NamespaceHandlerSupport implements BeanDefinitionParser {

    protected Class<?> beanClass;

    protected AbstractBeanDefinitionParser(Class<?> beanClass) {
        init();
        this.beanClass = beanClass;
    }

    protected void parseAttribute(Element element, BeanDefinition beanDefinition) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0, n = attributes.getLength(); i < n; i++) {
            Node node = attributes.item(i);
            String value = node.getNodeValue();
            if (!value.isEmpty()) {
                beanDefinition.getPropertyValues().add(node.getNodeName(), value);
            }
        }
    }

    protected void parseElement(Element element, ParserContext parserContext, ElementParser parser) {
        NodeList children = element.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                BeanDefinition definition = super.parse((Element) child, parserContext);
                parser.parser(child.getLocalName(), definition);
            }
        }
    }

    public interface ElementParser {
        void parser(String name, BeanDefinition definition);
    }

}
