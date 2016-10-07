package org.devefx.snio.config.spring;

import org.devefx.snio.config.EngineFactoryBean;
import org.devefx.snio.net.TcpServer;
import org.devefx.snio.net.UdpServer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineBeanDefinitionParser implements BeanDefinitionParser {
    private static final String CONTEXT_LOADER_BEAN_NAME = "org.devefx.snio.config.spring.ContextLoader@11894";
    private static final String TCP_ELEMENT = "tcp";
    private static final String UDP_ELEMENT = "udp";

    private static final String ID_ATTRIBUTE = "id";
    private static final String HOST_ATTRIBUTE = "host";
    private static final String MANAGER_REF_ATTRIBUTE = "manager-ref";
    private static final String AUTO_START_ATTRIBUTE = "auto-start";

    private static final String SERVER_PROPERTY = "server";
    private static final String MANAGER_PROPERTY = "manager";
    private static final String AUTO_START_PROPERTY = "autoStart";

    private final Map<String, BeanDefinitionParser> parsers = new HashMap();

    public EngineBeanDefinitionParser() {
        init();
    }

    public void init() {
        parsers.put(TCP_ELEMENT, new ServerBeanDefinitionParser(TcpServer.class));
        parsers.put(UDP_ELEMENT, new ServerBeanDefinitionParser(UdpServer.class));
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(EngineFactoryBean.class);

        String beanName = element.getAttribute(ID_ATTRIBUTE);
        if (beanName == null || beanName.isEmpty()) {
            beanName = BeanDefinitionReaderUtils.generateBeanName(
                    beanDefinition, parserContext.getRegistry(), false);
        }

        String host = element.getAttribute(HOST_ATTRIBUTE);
        if (host != null && host.length() != 0) {
            beanDefinition.getPropertyValues().addPropertyValue(HOST_ATTRIBUTE, host);
        }

        String managerRef = element.getAttribute(MANAGER_REF_ATTRIBUTE);
        if (managerRef != null && managerRef.length() != 0) {
            beanDefinition.getPropertyValues().addPropertyValue(MANAGER_PROPERTY,
                    new RuntimeBeanReference(managerRef));
        }

        String autoStart = element.getAttribute(AUTO_START_ATTRIBUTE);
        beanDefinition.getPropertyValues().addPropertyValue(AUTO_START_PROPERTY, Boolean.valueOf(autoStart));

        List<BeanDefinition> definitions = new ArrayList<>();

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                BeanDefinitionParser parser = findParserForElement((Element) child, parserContext);
                definitions.add(parser.parse((Element) child, parserContext));
            }
        }

        if (!definitions.isEmpty()) {
            ManagedList<BeanDefinition> list = new ManagedList<>();
            for (BeanDefinition definition : definitions) {
                list.add(definition);
            }
            beanDefinition.getPropertyValues().addPropertyValue(SERVER_PROPERTY, list);
        }

        parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
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
