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

package org.apache.commons.proxy.interceptor.logging;

import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.ProxyUtils;

import java.lang.reflect.Method;

/**
 * @author James Carman
 * @since 1.1
 */
public abstract class AbstractLoggingInterceptor implements Interceptor
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static final int BUFFER_SIZE = 100;

//**********************************************************************************************************************
// Abstract Methods
//**********************************************************************************************************************

    protected abstract boolean isLoggingEnabled();

    protected abstract void logMessage( String message );

    protected abstract void logMessage( String message, Throwable t );

//**********************************************************************************************************************
// Interceptor Implementation
//**********************************************************************************************************************

    public Object intercept( Invocation invocation ) throws Throwable
    {
        if( isLoggingEnabled() )
        {
            final Method method = invocation.getMethod();
            entering(method, invocation.getArguments());
            try
            {
                Object result = invocation.proceed();
                exiting(method, result);
                return result;
            }
            catch( Throwable t )
            {
                throwing(method, t);
                throw t;
            }
        }
        else
        {
            return invocation.proceed();
        }
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    protected void entering( Method method, Object[] args )
    {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("BEGIN ");
        buffer.append(method.getName());
        buffer.append("(");
        int count = args.length;
        for( int i = 0; i < count; i++ )
        {
            Object arg = args[i];
            if( i > 0 )
            {
                buffer.append(", ");
            }
            convert(buffer, arg);
        }
        buffer.append(")");
        logMessage(buffer.toString());
    }

    protected void convert( StringBuffer buffer, Object input )
    {
        if( input == null )
        {
            buffer.append("<null>");
            return;
        }

        // Primitive types, and non-object arrays
        // use toString().  Less than ideal for int[], etc., but
        // that's a lot of work for a rare case.
        if( !( input instanceof Object[] ) )
        {
            buffer.append(input.toString());
            return;
        }
        buffer.append("(");
        buffer.append(ProxyUtils.getJavaClassName(input.getClass()));
        buffer.append("){");
        Object[] array = ( Object[] ) input;
        int count = array.length;
        for( int i = 0; i < count; i++ )
        {
            if( i > 0 )
            {
                buffer.append(", ");
            }

            // We use convert() again, because it could be a multi-dimensional array
            // (god help us) where each element must be converted.
            convert(buffer, array[i]);
        }
        buffer.append("}");
    }

    protected void exiting( Method method, Object result )
    {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("END ");
        buffer.append(method.getName());
        buffer.append("()");
        if( !Void.TYPE.equals(method.getReturnType()) )
        {
            buffer.append(" [");
            convert(buffer, result);
            buffer.append("]");
        }
        logMessage(buffer.toString());
    }

    protected void throwing( Method method, Throwable t )
    {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("EXCEPTION ");
        buffer.append(method);
        buffer.append("() -- ");
        buffer.append(t.getClass().getName());
        logMessage(buffer.toString(), t);
    }
}
