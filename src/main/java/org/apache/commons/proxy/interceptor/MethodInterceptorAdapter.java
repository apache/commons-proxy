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
package org.apache.commons.proxy.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * An adapter class to adapt AOP Alliance's {@link MethodInterceptor} interface to Commons Proxy's
 * {@link Interceptor} interface.
 *
 * <p>
 * <b>Dependencies</b>:
 * <ul>
 *   <li>AOP Alliance API version 1.0 or greater</li>
 * </ul>
 * </p>
 * @author James Carman
 * @since 1.0
 */
public class MethodInterceptorAdapter implements Interceptor
{
    private final MethodInterceptor methodInterceptor;

    public MethodInterceptorAdapter( MethodInterceptor methodInterceptor )
    {
        this.methodInterceptor = methodInterceptor;
    }

    public Object intercept( Invocation invocation ) throws Throwable
    {
        return methodInterceptor.invoke( new MethodInvocationAdapter( invocation ) );
    }

    private static class MethodInvocationAdapter implements MethodInvocation
    {
        private final Invocation invocation;

        public MethodInvocationAdapter( Invocation invocation )
        {
            this.invocation = invocation;
        }

        public Method getMethod()
        {
            return invocation.getMethod();
        }

        public Object[] getArguments()
        {
            return invocation.getArguments();
        }

        public Object proceed() throws Throwable
        {
            return invocation.proceed();
        }

        public Object getThis()
        {
            return invocation.getProxy();
        }

        public AccessibleObject getStaticPart()
        {
            return invocation.getMethod();
        }
    }
}
