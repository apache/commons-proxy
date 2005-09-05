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
package org.apache.commons.proxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * An {@link InvocationHandler} implementation which merely returns null for all method invocations.  This class is
 * useful for scenarios where the "null object" design pattern is needed.
 *
 * @author James Carman
 * @version 1.0
 */
public class NullInvocationHandler implements InvocationHandler
{
//----------------------------------------------------------------------------------------------------------------------
// InvocationHandler Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        final Class<?> returnType = method.getReturnType();
        if( returnType.isPrimitive() )
        {
            if( Integer.TYPE.equals( returnType ) )
            {
                return 0;
            }
            else if( Long.TYPE.equals( returnType ) )
            {
                return 0L;
            }
            else if( Double.TYPE.equals( returnType ) )
            {
                return 0.0;
            }
            else if( Float.TYPE.equals( returnType ) )
            {
                return 0.0f;
            }
            else if( Short.TYPE.equals( returnType ) )
            {
                return ( short )0;
            }
            else if( Character.TYPE.equals( returnType ) )
            {
                return ( char )0;
            }
            else if( Byte.TYPE.equals( returnType ) )
            {
                return ( byte )0;
            }
            return 0;
        }
        else
        {
            return null;
        }
    }
}

