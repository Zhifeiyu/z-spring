package com.z.springframework.beans.support;

import com.z.springframework.annotation.Component;
import com.z.springframework.beans.config.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author zfylin
 * @version 2020/09/15
 */
public class BeanDefinitionReader {

    private Properties config = new Properties();

    /**
     * 需要扫描的包
     */
    private static final String SCAN_PACKAGE = "scan.package";

    /**
     * 所有注册的 Bean className
     */
    private List<String> registryBeanClasses = new ArrayList<>();

    public BeanDefinitionReader(String... locations) {
        try (
                InputStream is = this.getClass().getClassLoader()
                        .getResourceAsStream(locations[0].replace("classpath:", ""))
        ) {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    public Properties getConfig() {
        return config;
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));
        assert url != null;
        File classPath = new File(url.getFile());

        for (File file : Objects.requireNonNull(classPath.listFiles())) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                registryBeanClasses.add(className);
            }
        }
    }

    /**
     * 扫描到的Bean配置信息转换为BeanDefinition对象
     */
    public List<BeanDefinition> loadBeanDefinitions() {
        List<BeanDefinition> result = new ArrayList<>();
        try {
            for (String className : registryBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                // 接口,不能实例化的, 不封装
                if (beanClass.isInterface()) {
                    continue;
                }

                Annotation[] annotations = beanClass.getAnnotations();
                if (annotations.length == 0) {
                    continue;
                }

                for (Annotation annotation : annotations) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType.isAnnotationPresent(Component.class)) {
                        // beanName有三种情况:
                        // 1、默认是类名首字母小写
                        // 2、自定义名字（这里暂不考虑）
                        // 3、接口注入
                        result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                        Class<?>[] interfaces = beanClass.getInterfaces();
                        for (Class<?> i : interfaces) {
                            // 封装接口和实现类之间的关系
                            result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 相关属性封装到BeanDefinition
     */
    private BeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    /**
     * 首字母变为小写
     */
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
