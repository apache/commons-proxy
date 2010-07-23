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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.ProxyUtils;

/**
 * {@link AnnotationFactory} provides a simplified API over {@link StubProxyFactory}
 * to stub a Java {@link Annotation}.  Non-stubbed methods will return the values
 * that would have been returned from a "real" annotation whose methods' values
 * were unspecified.
 *
 * @author Matt Benson
 */
public class AnnotationFactory {
    private static final Invoker ANNOTATION_INVOKER = new Invoker() {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        public Object invoke(Object proxy, Method method, Object[] arguments)
                throws Throwable {
            Object result = method.getDefaultValue();
            return result == null && method.getReturnType().isPrimitive() ? ProxyUtils
                    .nullValue(method.getReturnType())
                    : result;
        }
    };

    private ProxyFactory proxyFactory;

    /**
     * Create a new AnnotationFactory instance.
     */
    public AnnotationFactory() {
        this(ProxyUtils.proxyFactory());
    }

    /**
     * Create a new AnnotationFactory instance.
     * @param proxyFactory
     */
    public AnnotationFactory(ProxyFactory proxyFactory) {
        super();
        this.proxyFactory = proxyFactory;
    }

    /**
     * Create an annotation of the type supported by <code>configurer</code>.
     * @param <A>
     * @param configurer
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(
            AnnotationStubConfigurer<A> configurer) {
        return create(Thread.currentThread().getContextClassLoader(),
                configurer);
    }

    /**
     * Create an annotation of the type supported by <code>configurer</code> in the specified classpath.
     * @param <A>
     * @param classLoader
     * @param configurer
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(ClassLoader classLoader,
            AnnotationStubConfigurer<A> configurer) {
        return create(classLoader, configurer.getStubType(), configurer);
    }

    /**
     * Create an annotation of <code>annotationType</code> with fully default behavior.
     * @param <A>
     * @param classLoader
     * @param annotationType
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(Class<A> annotationType) {
        return create(Thread.currentThread().getContextClassLoader(),
                annotationType, new AnnotationStubConfigurer<A>(annotationType));
    }

    /**
     * Create an annotation of <code>annotationType</code> with fully default behavior.
     * @param <A>
     * @param classLoader
     * @param annotationType
     * @return stubbed annotation proxy
     */
    public <A extends Annotation> A create(ClassLoader classLoader,
            Class<A> annotationType) {
        return create(classLoader, annotationType,
                new AnnotationStubConfigurer<A>(annotationType));
    }

    private <A extends Annotation> A create(ClassLoader classLoader,
            Class<A> annotationType, AnnotationStubConfigurer<A> configurer) {
        StubProxyFactory stubFactory = new StubProxyFactory(proxyFactory,
                configurer);
        @SuppressWarnings("unchecked")
        final A result = (A) stubFactory.createInvokerProxy(classLoader, ANNOTATION_INVOKER,
                annotationType);
        return result;
    }
}
