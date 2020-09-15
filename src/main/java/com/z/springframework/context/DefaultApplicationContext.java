package com.z.springframework.context;

import com.z.springframework.annotation.Autowired;
import com.z.springframework.beans.BeanWrapper;
import com.z.springframework.beans.config.BeanDefinition;
import com.z.springframework.beans.support.BeanDefinitionReader;
import com.z.springframework.core.factory.ApplicationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zfylin
 * @version 2020/09/15
 */
public class DefaultApplicationContext implements ApplicationContext {

    private String configLocation;

    private BeanDefinitionReader reader;

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 实例化Bean容器
     */
    private Map<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public DefaultApplicationContext(String configLocation) {
        this.configLocation = configLocation;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refresh() throws Exception {
        // 1.读取配置文件
        reader = new BeanDefinitionReader(this.configLocation);

        // 2. 扫描相关的类, 封装成BeanDefinition
        List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3. 注册, 把配置信息放到容器里面(伪IOC容器)
        doRegisterBeanDefinition(beanDefinitions);

        // 4. 初始化Bean
        doAutowired();
    }

    @Override
    public Object getBean(String beanName) throws Exception {
        Object instance = getSingleton(beanName);
        if (instance != null) {
            return instance;
        }

        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        //调用反射初始化Bean
        instance = instantiateBean(beanName, beanDefinition);

        //把这个对象封装到BeanWrapper中
        BeanWrapper beanWrapper = new BeanWrapper(instance);

        //把BeanWrapper保存到IOC容器中去
        //注册一个类名（首字母小写，如helloService）
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);
        //注册一个全类名（如com.lqb.HelloService）
        this.factoryBeanInstanceCache.put(beanDefinition.getBeanClassName(), beanWrapper);

        //注入
        populateBean(beanName, new BeanDefinition(), beanWrapper);

        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    private Object getSingleton(String beanName) {
        BeanWrapper beanWrapper = factoryBeanInstanceCache.get(beanName);
        return beanWrapper == null ? null : beanWrapper.getWrappedInstance();
    }

    @Override
    @SuppressWarnings("all")
    public <T> T getBean(Class<T> requiredType) throws Exception {
        return (T) getBean(requiredType.getName());
    }

    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            // TODO: 一个接口只能一个实现？？
            // @Autowired 是通过 byType 的方式去注入的， 使用该注解，要求接口只能有一个实现类。
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The \"" + beanDefinition.getFactoryBeanName() + "\" is exists!!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    private void doAutowired() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) {
        //1、拿到要实例化的对象的类名
        String className = beanDefinition.getBeanClassName();

        //2、反射实例化，得到一个对象
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();

            //############填充如下代码###############
//            AdvisedSupport config = getAopConfig();
//            config.setTargetClass(clazz);
//            config.setTarget(instance);
//
//            //符合PointCut的规则的话，将创建代理对象
//            if(config.pointCutMatch()) {
//                instance = createProxy(config).getProxy();
//            }
            //#############填充完毕##############

        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }

    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) {

        Class<?> clazz = beanWrapper.getWrappedClass();

        // 获得所有的成员变量
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 如果没有被Autowired注解的成员变量则直接跳过
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired autowired = field.getAnnotation(Autowired.class);
            //拿到需要注入的类名
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            // 强制访问该成员变量
            field.setAccessible(true);

            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }
                // 将容器中的实例注入到成员变量中
                field.set(beanWrapper.getWrappedInstance(),
                        this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
