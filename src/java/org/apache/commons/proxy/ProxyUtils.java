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
package org.apache.commons.proxy;

import org.apache.commons.proxy.handler.NullInvocationHandler;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author James Carman
 * @version 1.0
 */
public class ProxyUtils
{
    /**
     * Creates a "null object" which implements the <code>proxyClasses</code>.
     * @param proxyFactory the proxy factory to be used to create the proxy object
     * @param proxyClasses the proxy interfaces
     * @return a "null object" which implements the <code>proxyClasses</code>.
     */
    public static Object createNullObject( ProxyFactory proxyFactory, Class... proxyClasses )
    {
        return proxyFactory.createInvocationHandlerProxy( new NullInvocationHandler(), proxyClasses );
    }

    /**
     * Creates a "null object" which implements the <code>proxyClasses</code>.
     * @param proxyFactory the proxy factory to be used to create the proxy object
     * @param classLoader the class loader to be used by the proxy factory to create the proxy object
     * @param proxyClasses the proxy interfaces
     * @return a "null object" which implements the <code>proxyClasses</code>.
     */
    public static Object createNullObject( ProxyFactory proxyFactory, ClassLoader classLoader, Class... proxyClasses )
    {
        return proxyFactory.createInvocationHandlerProxy( classLoader, new NullInvocationHandler(), proxyClasses );
    }


    /**
     * <p>Gets an array of {@link Class} objects representing all interfaces implemented by the given
     * class and its superclasses.</p>
     *
     * <p>The order is determined by looking through each interface in turn as
     * declared in the source file and following its hierarchy up. Then each
     * superclass is considered in the same way. Later duplicates are ignored,
     * so the order is maintained.</p>
     * <p>
     * <b>Note</b>: Implementation of this method was "borrowed" from
     * <a href="http://jakarta.apache.org/commons/lang/">Jakarta Commons Lang</a> to avoid a dependency.
     * </p>
     * @param cls  the class to look up, may be <code>null</code>
     * @return an array of {@link Class} objects representing all interfaces implemented by the given
     * class and its superclasses or <code>null</code> if input class is null.
     */
    public static Class[] getAllInterfaces( Class cls )
    {
        final List<Class> interfaces = getAllInterfacesImpl( cls );
        return ( Class[] ) interfaces.toArray( new Class[interfaces.size()] );
    }

    private static List<Class> getAllInterfacesImpl( Class cls )
    {
        if( cls == null )
        {
            return null;
        }
        List<Class> list = new ArrayList<Class>();
        while( cls != null )
        {
            Class[] interfaces = cls.getInterfaces();
            for( int i = 0; i < interfaces.length; i++ )
            {
                if( !list.contains( interfaces[i] ) )
                {
                    list.add( interfaces[i] );
                }
                List superInterfaces = getAllInterfacesImpl( interfaces[i] );
                for( Iterator it = superInterfaces.iterator(); it.hasNext(); )
                {
                    Class intface = ( Class ) it.next();
                    if( !list.contains( intface ) )
                    {
                        list.add( intface );
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        return list;
    }
}
