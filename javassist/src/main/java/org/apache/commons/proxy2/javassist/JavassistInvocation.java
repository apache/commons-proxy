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

package org.apache.commons.proxy2.javassist;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.ProxyUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A <a href="http://www.jboss.org/products/javassist">Javassist</a>-based {@link Invocation} implementation.  This
 * class actually serves as the superclass for all <a href="http://www.jboss.org/products/javassist">Javassist</a>-based
 * method invocations.  Subclasses are dynamically created to deal with specific interface methods (they're hard-wired).
 *
 * @author James Carman
 * @since 1.0
 */
public abstract class JavassistInvocation implements Invocation
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static WeakHashMap<ClassLoader, Map<String, WeakReference<Class<?>>>> loaderToClassCache = new WeakHashMap<ClassLoader, Map<String,WeakReference<Class<?>>>>();

    /** The invoked method */
    protected final Method method;

    /** The target object */
    protected final Object target;

    /** The method arguments */
    protected final Object[] arguments;

//**********************************************************************************************************************
// Static Methods
//**********************************************************************************************************************

    private static String createCastExpression( Class<?> type, String objectToCast )
    {
        if( !type.isPrimitive() )
        {
            return "( " + ProxyUtils.getJavaClassName(type) + " )" + objectToCast;
        }
        else
        {
            return "( ( " + ProxyUtils.getWrapperClass(type).getName() + " )" + objectToCast + " )." +
                    type.getName() + "Value()";
        }
    }

    private static Class<?> createInvocationClass( ClassLoader classLoader, Method interfaceMethod )
            throws CannotCompileException
    {
        Class<?> invocationClass;
        final CtClass ctClass = JavassistUtils.createClass(
                getSimpleName(interfaceMethod.getDeclaringClass()) + "_" + interfaceMethod.getName() +
                        "_invocation",
                JavassistInvocation.class);
        final CtConstructor constructor = new CtConstructor(
                JavassistUtils.resolve(new Class[] {Method.class, Object.class, Object[].class}),
                ctClass);
        constructor.setBody("{\n\tsuper($$);\n}");
        ctClass.addConstructor(constructor);
        final CtMethod proceedMethod = new CtMethod(JavassistUtils.resolve(Object.class), "proceed",
                JavassistUtils.resolve(new Class[0]), ctClass);
        final Class<?>[] argumentTypes = interfaceMethod.getParameterTypes();
        final StringBuffer proceedBody = new StringBuffer("{\n");
        if( !Void.TYPE.equals(interfaceMethod.getReturnType()) )
        {
            proceedBody.append("\treturn ");
            if( interfaceMethod.getReturnType().isPrimitive() )
            {
                proceedBody.append("new ");
                proceedBody.append(ProxyUtils.getWrapperClass(interfaceMethod.getReturnType()).getName());
                proceedBody.append("( ");
            }
        }
        else
        {
            proceedBody.append("\t");
        }
        proceedBody.append("( (");
        proceedBody.append(ProxyUtils.getJavaClassName(interfaceMethod.getDeclaringClass()));
        proceedBody.append(" )target ).");
        proceedBody.append(interfaceMethod.getName());
        proceedBody.append("(");
        for( int i = 0; i < argumentTypes.length; ++i )
        {
            final Class<?> argumentType = argumentTypes[i];
            proceedBody.append(createCastExpression(argumentType, "arguments[" + i + "]"));
            if( i != argumentTypes.length - 1 )
            {
                proceedBody.append(", ");
            }
        }
        if( !Void.TYPE.equals(interfaceMethod.getReturnType()) && interfaceMethod.getReturnType().isPrimitive() )
        {
            proceedBody.append(") );\n");
        }
        else
        {
            proceedBody.append(");\n");
        }
        if( Void.TYPE.equals(interfaceMethod.getReturnType()) )
        {
            proceedBody.append("\treturn null;\n");
        }
        proceedBody.append("}");
        final String body = proceedBody.toString();
        proceedMethod.setBody(body);
        ctClass.addMethod(proceedMethod);
        invocationClass = ctClass.toClass(classLoader);
        return invocationClass;
    }

    private static Map<String, WeakReference<Class<?>>> getClassCache( ClassLoader classLoader )
    {
        Map<String, WeakReference<Class<?>>> cache = loaderToClassCache.get(classLoader);
        if( cache == null )
        {
            cache = new HashMap<String, WeakReference<Class<?>>>();
            loaderToClassCache.put(classLoader, cache);
        }
        return cache;
    }

    /**
     * Returns a method invocation class specifically coded to invoke the supplied interface method.
     *
     * @param classLoader     the classloader to use
     * @param interfaceMethod the interface method
     * @return a method invocation class specifically coded to invoke the supplied interface method
     * @throws CannotCompileException if a compilation error occurs
     */
    synchronized static Class<?> getMethodInvocationClass( ClassLoader classLoader,
                                                        Method interfaceMethod )
            throws CannotCompileException
    {
        final Map<String, WeakReference<Class<?>>> classCache = getClassCache(classLoader);
        final String key = toClassCacheKey(interfaceMethod);
        final WeakReference<Class<?>> invocationClassRef = classCache.get(key);
        Class<?> invocationClass;
        if( invocationClassRef == null )
        {
            invocationClass = createInvocationClass(classLoader, interfaceMethod);
            classCache.put(key, new WeakReference<Class<?>>(invocationClass));
        }
        else
        {
            synchronized( invocationClassRef )
            {
                invocationClass = invocationClassRef.get();
                if( invocationClass == null )
                {
                    invocationClass = createInvocationClass(classLoader, interfaceMethod);
                    classCache.put(key, new WeakReference<Class<?>>(invocationClass));
                }
            }
        }
        return invocationClass;
    }

    private static String getSimpleName( Class<?> c )
    {
        final String name = c.getName();
        final int ndx = name.lastIndexOf('.');
        return ndx == -1 ? name : name.substring(ndx + 1);
    }

    private static String toClassCacheKey( Method method )
    {
        return String.valueOf(method);
    }

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public JavassistInvocation( Method method, Object target, Object[] arguments )
    {
        this.method = method;
        this.target = target;
        this.arguments = arguments;
    }

//**********************************************************************************************************************
// Invocation Implementation
//**********************************************************************************************************************

    public Object[] getArguments()
    {
        return arguments;
    }

    public Method getMethod()
    {
        return method;
    }

    public Object getProxy()
    {
        return target;
    }
}
