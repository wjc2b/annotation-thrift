package com.wjc.thrift.client.scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.*;

/**
 * @author wjc
 * @date 2024-04-16 10:16
 * @desription
 */
public class ThriftClientBeanScanProcessor implements ApplicationContextAware, BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftClientBeanScanProcessor.class);

    private static final String SPRING_THRIFT_CLIENT_PACKAGE_TO_SCAN = "spring.thrift.client.package-to-scan";

    private static final String DEFAULT_SCAN_PACKAGE = "";

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) beanFactory;
        ThriftClientBeanScanner beanScanner = new ThriftClientBeanScanner(definitionRegistry);
        beanScanner.setResourceLoader(applicationContext);
        beanScanner.setBeanNameGenerator(new AnnotationBeanNameGenerator());
        beanScanner.setScopedProxyMode(ScopedProxyMode.INTERFACES);
        setScannedPackages(beanScanner,applicationContext.getEnvironment().getProperty(SPRING_THRIFT_CLIENT_PACKAGE_TO_SCAN));
    }

    /**
     * 如果有多个包路径，就先拆解，再扫描。反之直接扫描路径。
     * @param beanScanner
     * @param basePackages
     */
    private void setScannedPackages(ThriftClientBeanScanner beanScanner, String basePackages) {

        if (StringUtils.isBlank(basePackages)) {
            beanScanner.scan(DEFAULT_SCAN_PACKAGE);
            return;
        }

        int delimiterIndex = StringUtils.indexOf(basePackages,",");
        if (delimiterIndex>-1){
            StringTokenizer stringTokenizer = new StringTokenizer(basePackages, ",");
            Set<String> packageToScanSet = new HashSet<>();
            if (stringTokenizer.hasMoreTokens()){
                do{
                    String s = stringTokenizer.nextToken();
                    packageToScanSet.add(s);
                    LOGGER.info("Subpackage {} is to be scanned by {}", s, beanScanner);
                }while(stringTokenizer.hasMoreTokens());
            }
            List<String> packageToScanList = new ArrayList<>(packageToScanSet);
            String[] packageToScan = packageToScanSet.toArray(new String[packageToScanList.size()]);
            beanScanner.scan(packageToScan);
        }else{
            LOGGER.info("Base package {} is to be scanned by {}", basePackages, beanScanner);
            beanScanner.scan(basePackages);
        }
    }
}
