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

package org.apache.commons.proxy2;

import java.lang.reflect.Method;
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
public final class ProxyUtils
{
    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    public static final Object[] EMPTY_ARGUMENTS = new Object[0];
    public static final Class<?>[] EMPTY_ARGUMENT_TYPES = new Class[0];
    private static final Map<Class<?>, Class<?>> WRAPPER_CLASS_MAP = new HashMap<Class<?>, Class<?>>();
    private static final Map<Class<?>, Object> NULL_VALUE_MAP = new HashMap<Class<?>, Object>();

    //******************************************************************************************************************
    // Static Methods
    //******************************************************************************************************************

    static
    {
        WRAPPER_CLASS_MAP.put(Integer.TYPE, Integer.class);
        WRAPPER_CLASS_MAP.put(Character.TYPE, Character.class);
        WRAPPER_CLASS_MAP.put(Boolean.TYPE, Boolean.class);
        WRAPPER_CLASS_MAP.put(Short.TYPE, Short.class);
        WRAPPER_CLASS_MAP.put(Long.TYPE, Long.class);
        WRAPPER_CLASS_MAP.put(Float.TYPE, Float.class);
        WRAPPER_CLASS_MAP.put(Double.TYPE, Double.class);
        WRAPPER_CLASS_MAP.put(Byte.TYPE, Byte.class);
    }

    static
    {
        NULL_VALUE_MAP.put(Integer.TYPE, Integer.valueOf(0));
        NULL_VALUE_MAP.put(Long.TYPE, Long.valueOf(0));
        NULL_VALUE_MAP.put(Short.TYPE, Short.valueOf((short) 0));
        NULL_VALUE_MAP.put(Byte.TYPE, Byte.valueOf((byte) 0));
        NULL_VALUE_MAP.put(Float.TYPE, Float.valueOf(0.0f));
        NULL_VALUE_MAP.put(Double.TYPE, Double.valueOf(0.0));
        NULL_VALUE_MAP.put(Character.TYPE, Character.valueOf((char) 0));
        NULL_VALUE_MAP.put(Boolean.TYPE, Boolean.FALSE);
    }

    /**
     * <p>
     * Gets an array of {@link Class} objects representing all interfaces implemented by the given class and its
     * superclasses.
     * </p>
     * <p/>
     * <p>
     * The order is determined by looking through each interface in turn as declared in the source file and following
     * its hierarchy up. Then each superclass is considered in the same way. Later duplicates are ignored, so the order
     * is maintained.
     * </p>
     * <p/>
     * <b>Note</b>: Implementation of this method was "borrowed" from <a href="http://commons.apache.org/lang/">Apache
     * Commons Lang</a> to avoid a dependency.
     * </p>
     * 
     * @param cls
     *            the class to look up, may be <code>null</code>
     * @return an array of {@link Class} objects representing all interfaces implemented by the given class and its
     *         superclasses or <code>null</code> if input class is null.
     */
    public static Class<?>[] getAllInterfaces(Class<?> cls)
    {
        final List<Class<?>> interfaces = getAllInterfacesImpl(cls, new LinkedList<Class<?>>());
        return interfaces == null ? null : (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    private static List<Class<?>> getAllInterfacesImpl(final Class<?> cls, List<Class<?>> list)
    {
        if (cls == null)
        {
            return null;
        }
        Class<?> currentClass = cls;
        while (currentClass != null)
        {
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++)
            {
                if (!list.contains(interfaces[i]))
                {
                    list.add(interfaces[i]);
                }
                getAllInterfacesImpl(interfaces[i], list);
            }
            currentClass = currentClass.getSuperclass();
        }
        return list;
    }

    /**
     * Returns the class name as you would expect to see it in Java code.
     * <p/>
     * <b>Examples:</b>
     * <ul>
     * <li>getJavaClassName( Object[].class ) == "Object[]"</li>
     * <li>getJavaClassName( Object[][].class ) == "Object[][]"</li>
     * <li>getJavaClassName( Integer.TYPE ) == "int"</li>
     * </p>
     * 
     * @param clazz
     *            the class
     * @return the class' name as you would expect to see it in Java code
     */
    public static String getJavaClassName(Class<?> clazz)
    {
        if (clazz.isArray())
        {
            return getJavaClassName(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }

    /**
     * Returns the wrapper class for the given primitive type.
     * 
     * @param primitiveType
     *            the primitive type
     * @return the wrapper class
     */
    public static Class<?> getWrapperClass(Class<?> primitiveType)
    {
        return WRAPPER_CLASS_MAP.get(primitiveType);
    }

    /**
     * Returns the proper "null value" as specified by the Java language.
     * 
     * @param type
     *            the type
     * @return the null value
     */
    @SuppressWarnings("unchecked")
    public static <T> T nullValue(Class<T> type)
    {
        return (T) NULL_VALUE_MAP.get(type);
    }

    /**
     * Learn whether the specified method is/overrides {@link Object#equals(Object)}.
     * 
     * @param method
     *            to compare
     * @return <code>true</code> for a method with signature <code>boolean equals(Object)</code>
     */
    public static boolean isEqualsMethod(Method method)
    {
        return "equals".equals(method.getName()) && Boolean.TYPE.equals(method.getReturnType())
                && method.getParameterTypes().length == 1 && Object.class.equals(method.getParameterTypes()[0]);
    }

    /**
     * Learn whether the specified method is/overrides {@link Object#hashCode()}.
     * 
     * @param method
     *            to compare
     * @return true for a method with signature <code>int hashCode()</code>
     */
    public static boolean isHashCode(Method method)
    {
        return "hashCode".equals(method.getName()) && Integer.TYPE.equals(method.getReturnType())
                && method.getParameterTypes().length == 0;
    }

    /**
     * Get a {@link ProxyFactory} that delegates to discoverable {@link ProxyFactory} service providers.
     * 
     * @return {@link ProxyFactory}
     */
    public static ProxyFactory proxyFactory()
    {
        return DefaultProxyFactory.INSTANCE;
    }

    private ProxyUtils()
    {
        // Hiding constructor in utility class!
    }
}
