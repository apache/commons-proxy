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

package org.apache.commons.proxy.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.ProxyUtils;

/**
 * An interceptor which logs each method invocation.
 * <b>Note</b>: The implementation of this class was borrowed from
 * HiveMind's logging interceptor.
 * <p/>
 * <p>
 * <b>Dependencies</b>:
 * <ul>
 * <li>Apache Commons Logging version 1.0.4 or greater</li>
 * </ul>
 * </p>
 *
 * @author James Carman
 * @since 1.0
 */
public class LoggingInterceptor implements Interceptor
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static final int BUFFER_SIZE = 100;
    private Log log;

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public LoggingInterceptor( Log log )
    {
        this.log = log;
    }

//**********************************************************************************************************************
// Interceptor Implementation
//**********************************************************************************************************************

    public Object intercept( Invocation invocation ) throws Throwable
    {
        if( log.isDebugEnabled() )
        {
            final String methodName = invocation.getMethod().getName();
            entry(methodName, invocation.getArguments());
            try
            {
                Object result = invocation.proceed();
                if( Void.TYPE.equals(invocation.getMethod().getReturnType()) )
                {
                    voidExit(methodName);
                }
                else
                {
                    exit(methodName, result);
                }
                return result;
            }
            catch( Throwable t )
            {
                exception(methodName, t);
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

    private void convert( StringBuffer buffer, Object input )
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

    private void entry( String methodName, Object[] args )
    {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("BEGIN ");
        buffer.append(methodName);
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
        log.debug(buffer.toString());
    }

    private void exception( String methodName, Throwable t )
    {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("EXCEPTION ");
        buffer.append(methodName);
        buffer.append("() -- ");
        buffer.append(t.getClass().getName());
        log.debug(buffer.toString(), t);
    }

    private void exit( String methodName, Object result )
    {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("END ");
        buffer.append(methodName);
        buffer.append("() [");
        convert(buffer, result);
        buffer.append("]");
        log.debug(buffer.toString());
    }

    private void voidExit( String methodName )
    {
        StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
        buffer.append("END ");
        buffer.append(methodName);
        buffer.append("()");
        log.debug(buffer.toString());
    }
}

