package org.devefx.snio.config.spring;

import org.devefx.snio.Server;
import org.devefx.snio.net.tcp.TCPServer;
import org.devefx.snio.net.udp.UDPServer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class ServersBeanDefinitionParser extends NamespaceHandlerSupport implements BeanDefinitionParser {

    ServersBeanDefinitionParser() {
        init();
    }

    @Override
    public void init() {
        registerBeanDefinitionParser("tcp", new ServerBeanDefinitionParser(TCPServer.class));
        registerBeanDefinitionParser("udp", new ServerBeanDefinitionParser(UDPServer.class));
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(ServersFactoryBean.class);

        ManagedList<BeanDefinition> managedList = new ManagedList<>();

        NodeList children = element.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                BeanDefinition definition = super.parse((Element) child, parserContext);
                managedList.add(definition);
            }
        }

        if (!managedList.isEmpty()) {
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(managedList);
        }

        return beanDefinition;
    }

    static class ServersFactoryBean implements FactoryBean<List<Server>> {

        private List<Server> servers;

        public ServersFactoryBean(List<Server> servers) {
            this.servers = servers;
        }

        @Override
        public List<Server> getObject() throws Exception {
            return servers;
        }

        @Override
        public Class<?> getObjectType() {
            return List.class;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }
    }

}
