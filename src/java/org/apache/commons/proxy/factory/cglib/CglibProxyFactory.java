/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.proxy.factory.cglib;

import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.factory.util.AbstractProxyFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * A <a href="http://cglib.sourceforge.net/">CGLIB</a>-based {@link org.apache.commons.proxy.ProxyFactory}
 * implementation.
 *
 * @author James Carman
 * @version 1.0
 */
public class CglibProxyFactory extends AbstractProxyFactory
{
    public Object createInterceptingProxy( ClassLoader classLoader, Object target, MethodInterceptor interceptor,
                                           Class... proxyInterfaces )
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader( classLoader );
        enhancer.setInterfaces( proxyInterfaces );
        enhancer.setCallback( new InterceptorBridge( target, interceptor ) );
        return enhancer.create();
    }

    public Object createDelegatingProxy( ClassLoader classLoader, ObjectProvider targetProvider,
                                         Class... proxyInterfaces )
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader( classLoader );
        enhancer.setInterfaces( proxyInterfaces );
        enhancer.setCallback( new DelegateProviderDispatcher( targetProvider ) );
        return enhancer.create();
    }

    private class InterceptorBridge implements net.sf.cglib.proxy.MethodInterceptor
    {
        private final MethodInterceptor inner;
        private final Object target;

        public InterceptorBridge( Object target, MethodInterceptor inner )
        {
            this.inner = inner;
            this.target = target;
        }

        public Object intercept( Object object, Method method, Object[] args, MethodProxy methodProxy ) throws Throwable
        {
            return inner.invoke( new MethodProxyMethodInvocation( target, method, args, methodProxy ) );
        }
    }

    private class MethodProxyMethodInvocation implements MethodInvocation
    {
        private final MethodProxy methodProxy;
        private final Method method;
        private final Object[] args;
        private final Object target;

        public MethodProxyMethodInvocation( Object target, Method method, Object[] args, MethodProxy methodProxy )
        {
            this.target = target;
            this.method = method;
            this.methodProxy = methodProxy;
            this.args = args == null || args.length == 0 ? null : args;
        }

        public Method getMethod()
        {
            return method;
        }

        public Object[] getArguments()
        {
            return args;
        }

        public Object proceed() throws Throwable
        {
            return methodProxy.invoke( target, args );
        }

        public Object getThis()
        {
            return target;
        }

        public AccessibleObject getStaticPart()
        {
            return method;
        }
    }

    private class DelegateProviderDispatcher implements Dispatcher
    {
        private final ObjectProvider delegateProvider;

        public DelegateProviderDispatcher( ObjectProvider delegateProvider )
        {
            this.delegateProvider = delegateProvider;
        }

        public Object loadObject()
        {
            return delegateProvider.getDelegate();
        }
    }

}
