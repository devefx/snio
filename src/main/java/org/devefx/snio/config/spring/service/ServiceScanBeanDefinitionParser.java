package org.devefx.snio.config.spring.service;

import org.devefx.snio.config.spring.util.ServiceRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.*;
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

import java.io.IOException;

public class ServiceScanBeanDefinitionParser implements BeanDefinitionParser {

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    static final String ATTRIBUTE_BASE_PACKAGE = "base-package";

    private ServiceRegistry registry;

    public ServiceScanBeanDefinitionParser(ServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String basePackage = element.getAttribute(ATTRIBUTE_BASE_PACKAGE);

        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                ClassUtils.convertClassNameToResourcePath(basePackage) + "/" + DEFAULT_RESOURCE_PATTERN;
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        try {
            Resource[] resources = resolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
                    if (metadata.isIndependent()) {
                        GenericBeanDefinition definition = new GenericBeanDefinition();
                        definition.setBeanClassName(metadata.getClassName());

                        registry.register(definition, parserContext.getRegistry());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
