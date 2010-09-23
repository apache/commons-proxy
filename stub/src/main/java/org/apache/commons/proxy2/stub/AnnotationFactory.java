/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.proxy2.stub;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.AnnotationUtils;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.impl.AbstractProxyFactory;

/**
 * {@link AnnotationFactory} provides a simplified API over {@link StubProxyFactory}
 * to stub a Java {@link Annotation}.  Like "real" runtime proxies, instances created via
 * {@link AnnotationFactory} are {@link Proxy}-based.  Non-stubbed methods including
 * {@link Annotation#annotationType()} will return methods' default values and
 * {@link Annotation#equals(Object)}/{@link Annotation#hashCode()}/{@link Annotation#toString()}
 * return values consistent with those methods' documented expectations.
 *
 * @author Matt Benson
 */
public class AnnotationFactory {
    //underlying proxyfactory implementation based on org.apache.commons.proxy2.jdk.JdkProxyFactory

    private static class InterceptorInvocationHandler implements InvocationHandler, Serializable {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Object target;
        private final Interceptor methodInterceptor;

        public InterceptorInvocationHandler(Object target, Interceptor methodInterceptor) {
            this.target = target;
            this.methodInterceptor = methodInterceptor;
        }

        /**
         * {@inheritDoc}
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ProxyUtils.isHashCode(method)) {
                return AnnotationUtils.hashCode((Annotation) proxy);
            }
            if (ProxyUtils.isEqualsMethod(method)) {
                return args[0] instanceof Annotation
                    && AnnotationUtils.equals((Annotation) proxy, (Annotation) args[0]);
            }
            if ("toString".equals(method.getName()) && method.getParameterTypes().length == 0) {
                return AnnotationUtils.toString((Annotation) proxy);
            }
            final ReflectionInvocation invocation = new ReflectionInvocation(target, method, args);
            return methodInterceptor.intercept(invocation);
        }

    }

    private static class ReflectionInvocation implements Invocation, Serializable {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Method method;
        private final Object[] arguments;
        private final Object target;

        public ReflectionInvocation(Object target, Method method, Object[] arguments) {
            this.method = method;
            this.arguments = (arguments == null ? ProxyUtils.EMPTY_ARGUMENTS : arguments);
            this.target = target;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public Method getMethod() {
            return method;
        }

        public Object getProxy() {
            return target;
        }

        public Object proceed() throws Throwable {
            try {
                return method.invoke(target, arguments);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    private static final ProxyFactory PROXY_FACTORY = new AbstractProxyFactory() {

        public <T> T createInvokerProxy(ClassLoader classLoader, final Invoker invoker, Class<?>... proxyClasses) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
            Class<?>... proxyClasses) {
            return (T) Proxy.newProxyInstance(classLoader, proxyClasses, new InterceptorInvocationHandler(target,
                interceptor));
        }

        public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> delegateProvider,
            Class<?>... proxyClasses) {
            throw new UnsupportedOperationException();
        }
    };

    private static final Invoker ANNOTATION_INVOKER = new Invoker() {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            Object result = method.getDefaultValue();
            return result == null && method.getReturnType().isPrimitive() ? ProxyUtils
                .nullValue(method.getReturnType()) : result;
        }
    };

    private static final ThreadLocal<Object> CONFIGURER = new ThreadLocal<Object>();

    private static final StubConfigurer<Annotation> SHARED_CONFIGURER = new StubConfigurer<Annotation>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends Annotation> getStubType() {
            return AnnotationFactory.getStubType();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void configure(Annotation stub) {
            when(stub.annotationType()).thenReturn(getStubType());
            Object o = CONFIGURER.get();
            if (o instanceof StubConfigurer<?>) {
                @SuppressWarnings("unchecked")
                final StubConfigurer<Annotation> configurer = (StubConfigurer<Annotation>) o;
                configurer.configure(requireStubInterceptor(), stub);
            }
        }
    };

    private ProxyFactory proxyFactory;

    /**
     * Create a new AnnotationFactory instance.
     */
    public AnnotationFactory() {
        this.proxyFactory = new StubProxyFactory(PROXY_FACTORY, SHARED_CONFIGURER);
    }

    /**
     * Create an annotation of the type supported by <code>configurer</code>.
     * @param <A>
     * @param configurer
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(StubConfigurer<A> configurer) {
        @SuppressWarnings("unchecked")
        final A result = (A) createInternal(Thread.currentThread().getContextClassLoader(), configurer);
        return result;
    }

    /**
     * Create an annotation of the type supported by <code>configurer</code> in the specified classpath.
     * @param <A>
     * @param classLoader
     * @param configurer
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(ClassLoader classLoader, StubConfigurer<A> configurer) {
        @SuppressWarnings("unchecked")
        final A result = (A) createInternal(classLoader, configurer);
        return result;
    }

    /**
     * Create an annotation of <code>annotationType</code> with fully default behavior.
     * @param <A>
     * @param classLoader
     * @param annotationType
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(Class<A> annotationType) {
        @SuppressWarnings("unchecked")
        final A result = (A) createInternal(Thread.currentThread().getContextClassLoader(), annotationType);
        return result;
    }

    /**
     * Create an annotation of <code>annotationType</code> with fully default behavior.
     * @param <A>
     * @param classLoader
     * @param annotationType
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(ClassLoader classLoader, Class<A> annotationType) {
        @SuppressWarnings("unchecked")
        final A result = (A) createInternal(classLoader, annotationType);
        return result;
    }

    private <A extends Annotation> A createInternal(ClassLoader classLoader, Object configurer) {
        final Object existingConfigurer = CONFIGURER.get();
        try {
            CONFIGURER.set(configurer);
            @SuppressWarnings("unchecked")
            final A result = (A) proxyFactory.createInvokerProxy(classLoader, ANNOTATION_INVOKER, getStubType());
            return result;
        } finally {
            if (existingConfigurer == null) {
                CONFIGURER.remove();
            } else {
                CONFIGURER.set(existingConfigurer);
            }
        }
    }

    private static <A extends Annotation> Class<? extends A> getStubType() {
        Object o = CONFIGURER.get();
        if (o instanceof Class<?>) {
            @SuppressWarnings("unchecked")
            final Class<? extends A> result = (Class<? extends A>) o;
            return result;
        }
        @SuppressWarnings("unchecked")
        final StubConfigurer<A> configurer = (StubConfigurer<A>) o;
        return configurer.getStubType();
    }
}
