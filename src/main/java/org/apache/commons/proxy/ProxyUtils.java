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

package org.apache.commons.proxy;

import org.apache.commons.proxy.invoker.NullInvoker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides some helpful proxy utility methods.
 *
 * @author James Carman
 * @since 1.0
 */
public class ProxyUtils
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    public static final Object[] EMPTY_ARGUMENTS = new Object[0];
    public static final Class[] EMPTY_ARGUMENT_TYPES = new Class[0];
    private static final Map wrapperClassMap = new HashMap();

//**********************************************************************************************************************
// Static Methods
//**********************************************************************************************************************

    static
    {
        wrapperClassMap.put(Integer.TYPE, Integer.class);
        wrapperClassMap.put(Character.TYPE, Character.class);
        wrapperClassMap.put(Boolean.TYPE, Boolean.class);
        wrapperClassMap.put(Short.TYPE, Short.class);
        wrapperClassMap.put(Long.TYPE, Long.class);
        wrapperClassMap.put(Float.TYPE, Float.class);
        wrapperClassMap.put(Double.TYPE, Double.class);
        wrapperClassMap.put(Byte.TYPE, Byte.class);
    }

    /**
     * Creates a "null object" which implements the <code>proxyClasses</code>.
     *
     * @param proxyFactory the proxy factory to be used to create the proxy object
     * @param proxyClasses the proxy interfaces
     * @return a "null object" which implements the <code>proxyClasses</code>.
     */
    public static Object createNullObject( ProxyFactory proxyFactory, Class[] proxyClasses )
    {
        return proxyFactory.createInvokerProxy(new NullInvoker(), proxyClasses);
    }

    /**
     * Creates a "null object" which implements the <code>proxyClasses</code>.
     *
     * @param proxyFactory the proxy factory to be used to create the proxy object
     * @param classLoader  the class loader to be used by the proxy factory to create the proxy object
     * @param proxyClasses the proxy interfaces
     * @return a "null object" which implements the <code>proxyClasses</code>.
     */
    public static Object createNullObject( ProxyFactory proxyFactory, ClassLoader classLoader, Class[] proxyClasses )
    {
        return proxyFactory.createInvokerProxy(classLoader, new NullInvoker(), proxyClasses);
    }

    /**
     * <p>Gets an array of {@link Class} objects representing all interfaces implemented by the given class and its
     * superclasses.</p>
     * <p/>
     * <p>The order is determined by looking through each interface in turn as declared in the source file and following
     * its hierarchy up. Then each superclass is considered in the same way. Later duplicates are ignored, so the order
     * is maintained.</p>
     * <p/>
     * <b>Note</b>: Implementation of this method was "borrowed" from
     * <a href="http://commons.apache.org/lang/">Apache Commons Lang</a> to avoid a dependency.</p>
     *
     * @param cls the class to look up, may be <code>null</code>
     * @return an array of {@link Class} objects representing all interfaces implemented by the given class and its
     *         superclasses or <code>null</code> if input class is null.
     */
    public static Class[] getAllInterfaces( Class cls )
    {
        final List interfaces = getAllInterfacesImpl(cls, new LinkedList());
        return interfaces == null ? null : ( Class[] ) interfaces.toArray(new Class[interfaces.size()]);
    }

    private static List getAllInterfacesImpl( Class cls, List list )
    {
        if( cls == null )
        {
            return null;
        }
        while( cls != null )
        {
            Class[] interfaces = cls.getInterfaces();
            for( int i = 0; i < interfaces.length; i++ )
            {
                if( !list.contains(interfaces[i]) )
                {
                    list.add(interfaces[i]);
                }
                getAllInterfacesImpl(interfaces[i], list);
            }
            cls = cls.getSuperclass();
        }
        return list;
    }

    /**
     * Returns the class name as you would expect to see it in Java code.
     * <p/>
     * <b>Examples:</b> <ul> <li>getJavaClassName( Object[].class ) == "Object[]"</li> <li>getJavaClassName(
     * Object[][].class ) == "Object[][]"</li> <li>getJavaClassName( Integer.TYPE ) == "int"</li> </p>
     *
     * @param clazz the class
     * @return the class' name as you would expect to see it in Java code
     */
    public static String getJavaClassName( Class clazz )
    {
        if( clazz.isArray() )
        {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }

    /**
     * Returns the wrapper class for the given primitive type.
     *
     * @param primitiveType the primitive type
     * @return the wrapper class
     */
    public static Class getWrapperClass( Class primitiveType )
    {
        return ( Class ) wrapperClassMap.get(primitiveType);
    }
}

