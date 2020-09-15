package com.z.springframework.beans;

/**
 * @author zfylin
 * @version 2020/09/15
 */
public class BeanWrapper {

    /**
     * Bean的实例化对象
     **/
    private Object wrappedObject;

    public BeanWrapper(Object wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    public Object getWrappedInstance() {
        return this.wrappedObject;
    }

    public Class<?> getWrappedClass() {
        return getWrappedInstance().getClass();
    }
}
