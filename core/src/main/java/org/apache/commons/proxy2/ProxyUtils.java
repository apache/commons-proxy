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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

/**
 * Provides some helpful proxy utility methods.
 * 
 * @since 1.0
 */
public final class ProxyUtils
{
    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    public static final Object[] EMPTY_ARGUMENTS = ArrayUtils.EMPTY_OBJECT_ARRAY;
    public static final Class<?>[] EMPTY_ARGUMENT_TYPES = ArrayUtils.EMPTY_CLASS_ARRAY;
    private static final Map<Class<?>, Class<?>> WRAPPER_CLASS_MAP;
    private static final Map<Class<?>, Object> NULL_VALUE_MAP;

    //******************************************************************************************************************
    // Static Methods
    //******************************************************************************************************************

    static
    {
        final Map<Class<?>, Class<?>> wrappers = new HashMap<Class<?>, Class<?>>();
        wrappers.put(Integer.TYPE, Integer.class);
        wrappers.put(Character.TYPE, Character.class);
        wrappers.put(Boolean.TYPE, Boolean.class);
        wrappers.put(Short.TYPE, Short.class);
        wrappers.put(Long.TYPE, Long.class);
        wrappers.put(Float.TYPE, Float.class);
        wrappers.put(Double.TYPE, Double.class);
        wrappers.put(Byte.TYPE, Byte.class);
        WRAPPER_CLASS_MAP = Collections.unmodifiableMap(wrappers);
    }

    static
    {
        final Map<Class<?>, Object> nullValues = new HashMap<Class<?>, Object>();
        nullValues.put(Integer.TYPE, Integer.valueOf(0));
        nullValues.put(Long.TYPE, Long.valueOf(0));
        nullValues.put(Short.TYPE, Short.valueOf((short) 0));
        nullValues.put(Byte.TYPE, Byte.valueOf((byte) 0));
        nullValues.put(Float.TYPE, Float.valueOf(0.0f));
        nullValues.put(Double.TYPE, Double.valueOf(0.0));
        nullValues.put(Character.TYPE, Character.valueOf((char) 0));
        nullValues.put(Boolean.TYPE, Boolean.FALSE);
        NULL_VALUE_MAP = Collections.unmodifiableMap(nullValues);
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
        return cls == null ? null : ClassUtils.getAllInterfaces(cls).toArray(ArrayUtils.EMPTY_CLASS_ARRAY);
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
    public static <T> T nullValue(Class<T> type)
    {
        @SuppressWarnings("unchecked") // map only contains matching type/value entries
        final T result = (T) NULL_VALUE_MAP.get(type);
        return result;
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
