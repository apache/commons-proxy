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

import org.apache.commons.proxy2.ProxyUtils;

import java.lang.reflect.Method;

/**
 * @author James Carman
 */
public class RecordedInvocation
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private final Method invokedMethod;
    private final Object[] arguments;

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public RecordedInvocation( Method invokedMethod, Object[] arguments )
    {
        this.invokedMethod = invokedMethod;
        this.arguments = arguments;
    }

//**********************************************************************************************************************
// Canonical Methods
//**********************************************************************************************************************

    public Method getInvokedMethod()
    {
        return invokedMethod;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(invokedMethod.getDeclaringClass().getName());
        buffer.append(".");
        buffer.append(invokedMethod.getName());
        buffer.append("(");
        int count = arguments.length;
        for( int i = 0; i < count; i++ )
        {
            Object arg = arguments[i];
            if( i > 0 )
            {
                buffer.append(", ");
            }
            convert(buffer, arg);
        }
        buffer.append(")");
        return buffer.toString();
    }

    protected void convert( StringBuffer buffer, Object input )
    {
        if( input == null )
        {
            buffer.append("<null>");
            return;
        }

        // Primitive types, and non-object arrays
        // use toString().
        if( !( input instanceof Object[] ) )
        {
            buffer.append(input.toString());
            return;
        }
        else
        {
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
                // where each element must be converted.
                convert(buffer, array[i]);
            }
            buffer.append("}");
        }
    }
}
