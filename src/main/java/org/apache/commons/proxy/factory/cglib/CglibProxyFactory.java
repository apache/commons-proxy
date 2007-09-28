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

package org.apache.commons.proxy.factory.cglib;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.factory.util.AbstractSubclassingProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A <a href="http://cglib.sourceforge.net/">CGLIB</a>-based {@link org.apache.commons.proxy.ProxyFactory}
 * implementation.
 * <p/>
 * <p/>
 * <b>Dependencies</b>: <ul> <li>CGLIB version 2.0.2 or greater</li> </ul> </p>
 *
 * @author James Carman
 * @since 1.0
 */
public class CglibProxyFactory extends AbstractSubclassingProxyFactory
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static CallbackFilter callbackFilter = new PublicCallbackFilter();

//----------------------------------------------------------------------------------------------------------------------
// ProxyFactory Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object createDelegatorProxy( ClassLoader classLoader, ObjectProvider targetProvider,
                                        Class[] proxyClasses )
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader( classLoader );
        enhancer.setInterfaces( toInterfaces( proxyClasses ) );
        enhancer.setSuperclass( getSuperclass( proxyClasses ) );
        enhancer.setCallbackFilter( callbackFilter );
        enhancer.setCallbacks( new Callback[]{ new ObjectProviderDispatcher( targetProvider ), NoOp.INSTANCE } );
        return enhancer.create();
    }

    public Object createInterceptorProxy( ClassLoader classLoader, Object target, Interceptor interceptor,
                                          Class[] proxyClasses )
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader( classLoader );
        enhancer.setInterfaces( toInterfaces( proxyClasses ) );
        enhancer.setSuperclass( getSuperclass( proxyClasses ) );
        enhancer.setCallbackFilter( callbackFilter );
        enhancer.setCallbacks( new Callback[]{ new InterceptorBridge( target, interceptor ), NoOp.INSTANCE } );
        return enhancer.create();
    }

    public Object createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                      Class[] proxyClasses )
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader( classLoader );
        enhancer.setInterfaces( toInterfaces( proxyClasses ) );
        enhancer.setSuperclass( getSuperclass( proxyClasses ) );
        enhancer.setCallbackFilter( callbackFilter );
        enhancer.setCallbacks( new Callback[]{ new InvokerBridge( invoker ), NoOp.INSTANCE } );
        return enhancer.create();
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class PublicCallbackFilter implements CallbackFilter
    {
        public int accept( Method method )
        {
            return Modifier.isPublic( method.getModifiers() ) ? 0 : 1;
        }
    }

    private class InvokerBridge implements net.sf.cglib.proxy.InvocationHandler
    {
        private final Invoker original;

        public InvokerBridge( Invoker original )
        {
            this.original = original;
        }

        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            return original.invoke( object, method, objects );
        }
    }

    private class InterceptorBridge implements net.sf.cglib.proxy.MethodInterceptor
    {
        private final Interceptor inner;
        private final Object target;

        public InterceptorBridge( Object target, Interceptor inner )
        {
            this.inner = inner;
            this.target = target;
        }

        public Object intercept( Object object, Method method, Object[] args, MethodProxy methodProxy ) throws Throwable
        {
            return inner.intercept( new MethodProxyInvocation( target, method, args, methodProxy ) );
        }
    }

    private class MethodProxyInvocation implements Invocation
    {
        private final MethodProxy methodProxy;
        private final Method method;
        private final Object[] args;
        private final Object target;

        public MethodProxyInvocation( Object target, Method method, Object[] args, MethodProxy methodProxy )
        {
            this.target = target;
            this.method = method;
            this.methodProxy = methodProxy;
            this.args = args;
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

        public Object getProxy()
        {
            return target;
        }
    }

    private class ObjectProviderDispatcher implements Dispatcher
    {
        private final ObjectProvider delegateProvider;

        public ObjectProviderDispatcher( ObjectProvider delegateProvider )
        {
            this.delegateProvider = delegateProvider;
        }

        public Object loadObject()
        {
            return delegateProvider.getObject();
        }
    }
}

