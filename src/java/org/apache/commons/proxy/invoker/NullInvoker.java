/* $Id:NullInvoker.java 325897 2005-10-17 10:11:52 -0400 (Mon, 17 Oct 2005) jcarman $
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
package org.apache.commons.proxy.invoker;

import org.apache.commons.proxy.Invoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Invoker} implementation which merely returns null for all method invocations.  This class is
 * useful for scenarios where the "null object" design pattern is needed.
 *
 * @author James Carman
 * @since 1.0
 */
public class NullInvoker implements Invoker
{
    private static Map primitiveValueMap = new HashMap();
    static
    {
        primitiveValueMap.put( Integer.TYPE, new Integer( 0 ) );
        primitiveValueMap.put( Long.TYPE, new Long( 0 ) );
        primitiveValueMap.put( Short.TYPE, new Short( ( short )0 ) );
        primitiveValueMap.put( Byte.TYPE, new Byte( ( byte )0 ) );
        primitiveValueMap.put( Float.TYPE, new Float( 0.0f ) );
        primitiveValueMap.put( Double.TYPE, new Double( 0.0 ) );
        primitiveValueMap.put( Character.TYPE, new Character( ( char )0 ) );
        primitiveValueMap.put( Boolean.TYPE, Boolean.FALSE );
    }
//----------------------------------------------------------------------------------------------------------------------
// InvocationHandler Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        final Class returnType = method.getReturnType();
        if( returnType.isPrimitive() )
        {
            return primitiveValueMap.get( returnType );
        }
        else
        {
            return null;
        }
    }
}

