package com.zematix.jworldcup.backend.configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Static accessor to ApplicationContext object.
 * @see <a href="https://foojay.io/today/statically-spilling-your-spring-beans/">Statically Spilling Your (Spring) Beans</a>
 */
@Component
public class StaticContextAccessor {

    @SuppressWarnings("rawtypes")
	private static final Map<Class, DynamicInvocationhandler> classHandlers = new HashMap<>();
    private static ApplicationContext context;

    public StaticContextAccessor(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {
            return getProxy(clazz);
        }
        return context.getBean(clazz);
    }

    @SuppressWarnings("unchecked")
	private static <T> T getProxy(Class<T> clazz) {
        DynamicInvocationhandler<T> invocationhandler = new DynamicInvocationhandler<>();
        classHandlers.put(clazz, invocationhandler);
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                invocationhandler
        );
    }

    //Use the context to get the actual beans and feed them to the invocationhandlers
    @SuppressWarnings("unchecked")
	@PostConstruct
    private void init() {
        classHandlers.forEach((clazz, invocationHandler) -> {
            Object bean = context.getBean(clazz);
            invocationHandler.setActualBean(bean);
        });
    }

    static class DynamicInvocationhandler<T> implements InvocationHandler {

        private T actualBean;

        public void setActualBean(T actualBean) {
            this.actualBean = actualBean;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (actualBean == null) {
                throw new RuntimeException("Not initialized yet! :(");
            }
            return method.invoke(actualBean, args);
        }
    }
}