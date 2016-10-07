package org.devefx.snio.config.spring;

import org.devefx.snio.Server;
import org.devefx.snio.config.ServerFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class ServerBeanDefinitionParser implements BeanDefinitionParser {
    private static final String DECODER_ELEMENT = "decoder";
    private static final String SERVICES_ELEMENT = "services";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String PORT_ATTRIBUTE = "port";

    private final Map<String, BeanDefinitionParser> parsers = new HashMap();
    private final Class<?> beanClass;

    public ServerBeanDefinitionParser(Class<? extends Server> beanClass) {
        init();
        this.beanClass = beanClass;
    }

    public void init() {
        parsers.put(DECODER_ELEMENT, new DecoderBeanDefinitionParser());
        parsers.put(SERVICES_ELEMENT, new ServicesBeanDefinitionParser());
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(ServerFactoryBean.class);

        // server
        GenericBeanDefinition serverDefinition = new GenericBeanDefinition();
        serverDefinition.setBeanClass(beanClass);

        String name = element.getAttribute(NAME_ATTRIBUTE);
        serverDefinition.getPropertyValues().addPropertyValue(NAME_ATTRIBUTE, name);

        String port = element.getAttribute(PORT_ATTRIBUTE);
        if (port != null && port.length() != 0) {
            serverDefinition.getPropertyValues().addPropertyValue(PORT_ATTRIBUTE, port);
        }

        // children
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                BeanDefinitionParser parser = findParserForElement((Element) child, parserContext);
                BeanDefinition definition = parser.parse((Element) child, parserContext);
                if (DECODER_ELEMENT.equals(child.getLocalName())) {
                    serverDefinition.getPropertyValues().addPropertyValue(DECODER_ELEMENT, definition);
                } else if (SERVICES_ELEMENT.equals(child.getLocalName())) {
                    beanDefinition.getPropertyValues().addPropertyValue(SERVICES_ELEMENT, definition);
                }
            }
        }

        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(serverDefinition);
        return beanDefinition;
    }

    private BeanDefinitionParser findParserForElement(Element element, ParserContext parserContext) {
        String localName = parserContext.getDelegate().getLocalName(element);
        BeanDefinitionParser parser = parsers.get(localName);
        if(parser == null) {
            parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionParser for element [" + localName + "]", element);
        }
        return parser;
    }
}
