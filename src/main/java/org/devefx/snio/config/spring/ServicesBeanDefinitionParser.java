package org.devefx.snio.config.spring;

import org.devefx.snio.config.ServiceFactoryBean;
import org.devefx.snio.config.ServiceFactoryBean.ServicesFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class ServicesBeanDefinitionParser implements BeanDefinitionParser {
	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private static final String ID_ATTRIBUTE = "id";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String REGISTER_ATTRIBUTE = "register";
    private static final String SCAN_PACKAGE = "scan-package";
    private ServiceBeanDefinitionParser parser = new ServiceBeanDefinitionParser();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(ServicesFactoryBean.class);

        List<BeanDefinition> definitions = new ArrayList<>();

        String packageName = element.getAttribute(SCAN_PACKAGE);
        if (packageName != null && !packageName.isEmpty()) {
        	scanPackage(parserContext.getRegistry(), definitions, packageName);
		}
        
        NodeList children = element.getChildNodes();
        if (children.getLength() != 0) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    definitions.add(parser.parse((Element) child, parserContext));
                }
            }
        }

        if (!definitions.isEmpty()) {
            ManagedList<BeanDefinition> list = new ManagedList<>();
            for (BeanDefinition definition : definitions) {
                list.add(definition);
            }
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(list);
        }

        return beanDefinition;
    }
    
    private static void scanPackage(BeanDefinitionRegistry registry, List<BeanDefinition> list, String packageName) {
    	String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + 
				ClassUtils.convertClassNameToResourcePath(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
		try {
			Resource[] resources = resolver.getResources(packageSearchPath);
			for (Resource resource: resources) {
				if (resource.isReadable()) {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
					AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
					if (metadata.isIndependent()) {
			            GenericBeanDefinition serviceDefinition = new GenericBeanDefinition();
			            serviceDefinition.setBeanClassName(metadata.getClassName());
			            
			            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			            beanDefinition.setBeanClass(ServiceFactoryBean.class);
			            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(serviceDefinition);
			            list.add(beanDefinition);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    class ServiceBeanDefinitionParser implements BeanDefinitionParser {
        @Override
        public BeanDefinition parse(Element element, ParserContext parserContext) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(ServiceFactoryBean.class);

            GenericBeanDefinition serviceDefinition = new GenericBeanDefinition();
            String className = element.getAttribute(CLASS_ATTRIBUTE);
            serviceDefinition.setBeanClassName(className);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(serviceDefinition);

            String register = element.getAttribute(REGISTER_ATTRIBUTE);
            if (register != null && Boolean.valueOf(register)) {
                BeanDefinitionRegistry registry = parserContext.getRegistry();
                String beanName = element.getAttribute(ID_ATTRIBUTE);
                if (beanName == null || beanName.isEmpty()) {
                    beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry, false);
                }
                registry.registerBeanDefinition(beanName, beanDefinition);
            }
            return beanDefinition;
        }
    }

}
