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

package org.apache.commons.proxy2.invoker.recorder;

import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.invoker.RecordedInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link InvocationRecorder} records method invocations against its generated proxies.
 * @author James Carman
 */
public class InvocationRecorder
{
    private final ProxyFactory proxyFactory;
    private List<RecordedInvocation> recordedInvocations = new LinkedList<RecordedInvocation>();

    /**
     * Create a new InvocationRecorder instance.
     * @param proxyFactory
     */
    public InvocationRecorder( ProxyFactory proxyFactory )
    {
        this.proxyFactory = proxyFactory;
    }

    /**
     * Get the invocations that have been recorded up to this point.  The list is "live" and should not be modified.
     * @return {@link List} of {@link RecordedInvocation}
     */
    public List<RecordedInvocation> getRecordedInvocations()
    {
        return recordedInvocations;
    }

    /**
     * Generate a recording proxy for the specified class.
     * @param <T>
     * @param type
     * @return the generated proxy
     */
    public <T> T proxy( Class<T> type )
    {
        return proxy(type, type);
    }

    /**
     * Generate a recording proxy for the specified class, qualified as <code>genericType</code>.
     * @param <T>
     * @param genericType
     * @param type
     * @return the generated proxy
     */
    public <T> T proxy( Type genericType, Class<T> type )
    {
        if( proxyFactory.canProxy(type) )
        {
            return proxyFactory.createInvokerProxy(new InvocationRecorderInvoker(genericType), type);
        }
        return ProxyUtils.nullValue(type);
    }

    private class InvocationRecorderInvoker implements Invoker
    {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        private final Type targetType;

        private InvocationRecorderInvoker( Type targetType )
        {
            this.targetType = targetType;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public Object invoke( Object o, Method method, Object[] args ) throws Throwable
        {
            recordedInvocations.add(new RecordedInvocation(method, args));
            final Class returnType = getReturnType(targetType, method);
            return proxy(method.getGenericReturnType(), returnType);
        }
    }

    /**
     * Reset this {@link InvocationRecorder}.
     */
    public void reset()
    {
        recordedInvocations.clear();
    }

    /**
     * Get the raw return type of a method qualified with regard to a particular target type.
     * @param enclosingType
     * @param method
     * @return {@link Class} instance
     * @throws Exception
     */
    public static Class<?> getReturnType( Type enclosingType, Method method ) throws Exception
    {
        Type returnType = method.getGenericReturnType();
        if( returnType instanceof Class<?> )
        {
            return ( Class<?> ) returnType;
        }
        else if( returnType instanceof TypeVariable<?> )
        {
            return resolveVariable(enclosingType, ( TypeVariable<?> ) returnType);
        }
        else if( returnType instanceof ParameterizedType )
        {
            return ( Class<?> ) ( ( ParameterizedType ) returnType ).getRawType();
        }
        return null;
    }

    /**
     * Resolve the raw type of a type variable against a particular owning/inheriting type.
     * @param enclosingType
     * @param typeVar
     * @return {@link Class} instance
     * @throws Exception
     */
    public static Class<?> resolveVariable( Type enclosingType, TypeVariable<?> typeVar ) throws Exception
    {
        if( enclosingType instanceof ParameterizedType )
        {
            ParameterizedType pt = ( ParameterizedType ) enclosingType;
            final Class<?> rawType = ( Class<?> ) pt.getRawType();
            TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
            for( int i = 0; i < typeParameters.length; i++ )
            {
                TypeVariable<?> typeParameter = typeParameters[i];
                if( typeParameter == typeVar )
                {
                    return ( Class<?> ) pt.getActualTypeArguments()[i];
                }
            }
        }
        else if( enclosingType instanceof Class<?> )
        {
            return resolveVariable(( ( Class<?> ) enclosingType ).getGenericSuperclass(), typeVar);
        }
        return null;
    }
}
