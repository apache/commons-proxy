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

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.exception.ProxyFactoryException;
import org.apache.commons.proxy2.impl.AbstractProxyClassGenerator;
import org.apache.commons.proxy2.impl.AbstractSubclassingProxyFactory;
import org.apache.commons.proxy2.impl.ProxyClassCache;

public class JavassistProxyFactory extends AbstractSubclassingProxyFactory
{
    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    private static final String GET_METHOD_METHOD_NAME = "_javassistGetMethod";

    private static final ProxyClassCache DELEGATING_PROXY_CACHE = new ProxyClassCache(
            new DelegatingProxyClassGenerator());
    private static final ProxyClassCache INTERCEPTOR_PROXY_CACHE = new ProxyClassCache(
            new InterceptorProxyClassGenerator());
    private static final ProxyClassCache INVOKER_PROXY_CACHE = new ProxyClassCache(new InvokerProxyClassGenerator());

    //******************************************************************************************************************
    // Static Methods
    //******************************************************************************************************************

    private static void addGetMethodMethod(CtClass proxyClass) throws CannotCompileException
    {
        final CtMethod method = new CtMethod(JavassistUtils.resolve(Method.class), GET_METHOD_METHOD_NAME,
                JavassistUtils.resolve(new Class[] { String.class, String.class, Class[].class }), proxyClass);
        final String body = "try { return Class.forName($1).getMethod($2, $3); } catch( Exception e ) "
                + "{ throw new RuntimeException(\"Unable to look up method.\", e); }";
        method.setBody(body);
        proxyClass.addMethod(method);
    }

    //******************************************************************************************************************
    // ProxyFactory Implementation
    //******************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> targetProvider,
            Class<?>... proxyClasses)
    {
        try
        {
            @SuppressWarnings("unchecked") // type inference
            final Class<? extends T> clazz = (Class<? extends T>) DELEGATING_PROXY_CACHE.getProxyClass(classLoader,
                    proxyClasses);
            return clazz.getConstructor(ObjectProvider.class).newInstance(targetProvider);
        }
        catch (Exception e)
        {
            throw new ProxyFactoryException("Unable to instantiate proxy2 from generated proxy2 class.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
            Class<?>... proxyClasses)
    {
        try
        {
            @SuppressWarnings("unchecked") // type inference
            final Class<? extends T> clazz = (Class<? extends T>) INTERCEPTOR_PROXY_CACHE.getProxyClass(classLoader,
                    proxyClasses);
            return clazz.getConstructor(Object.class, Interceptor.class).newInstance(target, interceptor);
        }
        catch (Exception e)
        {
            throw new ProxyFactoryException("Unable to instantiate proxy2 class instance.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, Invoker invoker, Class<?>... proxyClasses)
    {
        try
        {
            @SuppressWarnings("unchecked") // type inference
            final Class<? extends T> clazz = (Class<? extends T>) INVOKER_PROXY_CACHE.getProxyClass(classLoader,
                    proxyClasses);
            return clazz.getConstructor(Invoker.class).newInstance(invoker);
        }
        catch (Exception e)
        {
            throw new ProxyFactoryException("Unable to instantiate proxy2 from generated proxy2 class.", e);
        }
    }

    //******************************************************************************************************************
    // Inner Classes
    //******************************************************************************************************************

    private static class DelegatingProxyClassGenerator extends AbstractProxyClassGenerator
    {
        @Override
        public Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses)
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass(getSuperclass(proxyClasses));
                JavassistUtils.addField(ObjectProvider.class, "provider", proxyClass);
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve(new Class[] { ObjectProvider.class }), proxyClass);
                proxyConstructor.setBody("{ this.provider = $1; }");
                proxyClass.addConstructor(proxyConstructor);
                JavassistUtils.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                final Method[] methods = getImplementationMethods(proxyClasses);
                for (int i = 0; i < methods.length; ++i)
                {
                    if (!ProxyUtils.isEqualsMethod(methods[i]) && !ProxyUtils.isHashCode(methods[i]))
                    {
                        final Method method = methods[i];
                        final CtMethod ctMethod = new CtMethod(JavassistUtils.resolve(method.getReturnType()),
                                method.getName(), JavassistUtils.resolve(method.getParameterTypes()), proxyClass);
                        final String body = "{ return ( $r ) ( ( " + method.getDeclaringClass().getName()
                                + " )provider.getObject() )." + method.getName() + "($$); }";
                        ctMethod.setBody(body);
                        proxyClass.addMethod(ctMethod);
                    }
                }
                return proxyClass.toClass(classLoader, null);
            }
            catch (CannotCompileException e)
            {
                throw new ProxyFactoryException("Could not compile class.", e);
            }
        }
    }

    private static class InterceptorProxyClassGenerator extends AbstractProxyClassGenerator
    {
        @Override
        public Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses)
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass(getSuperclass(proxyClasses));
                final Method[] methods = getImplementationMethods(proxyClasses);
                JavassistUtils.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                JavassistUtils.addField(Object.class, "target", proxyClass);
                JavassistUtils.addField(Interceptor.class, "interceptor", proxyClass);
                addGetMethodMethod(proxyClass);
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                final CtConstructor proxyConstructor = new CtConstructor(JavassistUtils.resolve(new Class[] {
                        Object.class, Interceptor.class }), proxyClass);
                proxyConstructor.setBody("{\n\tthis.target = $1;\n\tthis.interceptor = $2; }");
                proxyClass.addConstructor(proxyConstructor);
                for (int i = 0; i < methods.length; ++i)
                {
                    if (!ProxyUtils.isEqualsMethod(methods[i]) && !ProxyUtils.isHashCode(methods[i]))
                    {
                        final CtMethod method = new CtMethod(JavassistUtils.resolve(methods[i].getReturnType()),
                                methods[i].getName(), JavassistUtils.resolve(methods[i].getParameterTypes()),
                                proxyClass);
                        final Class<?> invocationClass = JavassistInvocation.getMethodInvocationClass(classLoader,
                                methods[i]);

                        final String body = "{\n\t return ( $r ) interceptor.intercept( new "
                                + invocationClass.getName() + "( this, target, " + GET_METHOD_METHOD_NAME + "(\""
                                + methods[i].getDeclaringClass().getName() + "\", \"" + methods[i].getName()
                                + "\", $sig), $args ) );\n }";
                        method.setBody(body);
                        proxyClass.addMethod(method);
                    }

                }
                return proxyClass.toClass(classLoader, null);
            }
            catch (CannotCompileException e)
            {
                throw new ProxyFactoryException("Could not compile class.", e);
            }
        }

    }

    private static void addEqualsMethod(CtClass proxyClass) throws CannotCompileException
    {
        final CtMethod equalsMethod = new CtMethod(JavassistUtils.resolve(Boolean.TYPE), "equals",
                JavassistUtils.resolve(new Class[] { Object.class }), proxyClass);
        final String body = "{\n\treturn this == $1;\n}";
        equalsMethod.setBody(body);
        proxyClass.addMethod(equalsMethod);
    }

    private static void addHashCodeMethod(CtClass proxyClass) throws CannotCompileException
    {
        final CtMethod hashCodeMethod = new CtMethod(JavassistUtils.resolve(Integer.TYPE), "hashCode", new CtClass[0],
                proxyClass);
        hashCodeMethod.setBody("{\n\treturn System.identityHashCode(this);\n}");
        proxyClass.addMethod(hashCodeMethod);
    }

    private static class InvokerProxyClassGenerator extends AbstractProxyClassGenerator
    {
        @Override
        public Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses)
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass(getSuperclass(proxyClasses));
                final Method[] methods = getImplementationMethods(proxyClasses);
                JavassistUtils.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                JavassistUtils.addField(Invoker.class, "invoker", proxyClass);
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve(new Class[] { Invoker.class }), proxyClass);
                proxyConstructor.setBody("{\n\tthis.invoker = $1; }");
                proxyClass.addConstructor(proxyConstructor);
                addGetMethodMethod(proxyClass);
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                for (int i = 0; i < methods.length; ++i)
                {
                    if (!ProxyUtils.isEqualsMethod(methods[i]) && !ProxyUtils.isHashCode(methods[i]))
                    {
                        final CtMethod method = new CtMethod(JavassistUtils.resolve(methods[i].getReturnType()),
                                methods[i].getName(), JavassistUtils.resolve(methods[i].getParameterTypes()),
                                proxyClass);
                        final String body = "{\n\t return ( $r ) invoker.invoke( this, " + GET_METHOD_METHOD_NAME
                                + "(\"" + methods[i].getDeclaringClass().getName() + "\", \"" + methods[i].getName()
                                + "\", $sig), $args );\n }";
                        method.setBody(body);
                        proxyClass.addMethod(method);
                    }
                }
                return proxyClass.toClass(classLoader, null);
            }
            catch (CannotCompileException e)
            {
                throw new ProxyFactoryException("Could not compile class.", e);
            }
        }
    }
}
