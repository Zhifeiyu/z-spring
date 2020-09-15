package com.z.springframework.core.factory;

/**
 * @author zfylin
 * @version 2020/09/15
 */
public interface BeanFactory {
    Object getBean(String name) throws Exception;

    <T> T getBean(Class<T> requiredType) throws Exception;
}
