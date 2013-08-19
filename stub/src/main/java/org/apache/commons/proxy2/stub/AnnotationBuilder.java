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
import org.apache.commons.proxy2.provider.ObjectProviderUtils;

public class AnnotationBuilder<A extends Annotation> extends StubBuilder<A>
{
    // underlying proxyfactory implementation based on org.apache.commons.proxy2.jdk.JdkProxyFactory

    private static class InterceptorInvocationHandler implements InvocationHandler, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final ObjectProvider<?> provider;
        private final Interceptor methodInterceptor;

        public InterceptorInvocationHandler(ObjectProvider<?> provider, Interceptor methodInterceptor)
        {
            this.provider = provider;
            this.methodInterceptor = methodInterceptor;
        }

        /**
         * {@inheritDoc}
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (ProxyUtils.isHashCode(method))
            {
                return AnnotationUtils.hashCode((Annotation) proxy);
            }
            if (ProxyUtils.isEqualsMethod(method))
            {
                return args[0] instanceof Annotation
                    && AnnotationUtils.equals((Annotation) proxy, (Annotation) args[0]);
            }
            if ("toString".equals(method.getName()) && method.getParameterTypes().length == 0)
            {
                return AnnotationUtils.toString((Annotation) proxy);
            }
            final ReflectionInvocation invocation = new ReflectionInvocation(provider.getObject(), method, args);
            return methodInterceptor.intercept(invocation);
        }

    }

    private static class ReflectionInvocation implements Invocation, Serializable
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Method method;
        private final Object[] arguments;
        private final Object target;

        public ReflectionInvocation(Object target, Method method, Object[] arguments)
        {
            this.method = method;
            this.arguments = (arguments == null ? ProxyUtils.EMPTY_ARGUMENTS : arguments);
            this.target = target;
        }

        public Object[] getArguments()
        {
            return arguments;
        }

        public Method getMethod()
        {
            return method;
        }

        public Object getProxy()
        {
            return target;
        }

        public Object proceed() throws Throwable
        {
            try
            {
                return method.invoke(target, arguments);
            } catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }
    }

    private static final ProxyFactory PROXY_FACTORY = new AbstractProxyFactory()
    {
        @SuppressWarnings("unchecked")
        public <T> T createInvokerProxy(ClassLoader classLoader, final Invoker invoker, Class<?>... proxyClasses)
        {
            return (T) Proxy.newProxyInstance(classLoader, proxyClasses, new InvocationHandler()
            {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    return invoker.invoke(proxy, method, args);
                }
            });
        }

        @SuppressWarnings("unchecked")
        public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
            Class<?>... proxyClasses)
        {
            return (T) Proxy.newProxyInstance(classLoader, proxyClasses, new InterceptorInvocationHandler(
                ObjectProviderUtils.constant(target), interceptor));
        }

        @SuppressWarnings("unchecked")
        public <T> T createDelegatorProxy(ClassLoader classLoader, final ObjectProvider<?> delegateProvider,
            Class<?>... proxyClasses)
        {
            return (T) Proxy.newProxyInstance(classLoader, proxyClasses, new InterceptorInvocationHandler(
                delegateProvider, new Interceptor()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Object intercept(Invocation invocation) throws Throwable
                    {
                        return invocation.proceed();
                    }
                }));
        }
    };

    public static <A extends Annotation> A buildDefault(Class<A> type)
    {
        return of(type).build();
    }

    public static <A extends Annotation> AnnotationBuilder<A> of(Class<A> type)
    {
        return new AnnotationBuilder<A>(type, AnnotationInvoker.INSTANCE);
    }

    public static <A extends Annotation> AnnotationBuilder<A> of(Class<A> type, ObjectProvider<? extends A> provider)
    {
        return new AnnotationBuilder<A>(type, provider);
    }

    public static <A extends Annotation> AnnotationBuilder<A> of(Class<A> type, A target)
    {
        return new AnnotationBuilder<A>(type, target);
    }

    private AnnotationBuilder(Class<A> type, Invoker invoker)
    {
        super(PROXY_FACTORY, type, invoker);
    }

    private AnnotationBuilder(Class<A> type, ObjectProvider<? extends A> provider)
    {
        super(PROXY_FACTORY, type, provider);
    }

    private AnnotationBuilder(Class<A> type, A target)
    {
        super(PROXY_FACTORY, type, target);
    }

    @Override
    public AnnotationBuilder<A> train(BaseTrainer<?, A> trainer)
    {
        return (AnnotationBuilder<A>) super.train(trainer);
    }

    @Override
    public A build() {
        train(new AnnotationTrainer<A>(type)
        {
            @Override
            protected void train(A trainee)
            {
                when(trainee.annotationType()).thenReturn(type);
            }
        });
        return super.build();
    }
}
