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
package org.apache.commons.proxy.factory.javassist;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyUtils;
import org.apache.commons.proxy.exception.ProxyFactoryException;
import org.apache.commons.proxy.factory.util.AbstractProxyClassGenerator;
import org.apache.commons.proxy.factory.util.AbstractSubclassingProxyFactory;
import org.apache.commons.proxy.factory.util.ProxyClassCache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * A <a href="http://www.jboss.org/products/javassist">Javassist</a>-based {@link org.apache.commons.proxy.ProxyFactory}
 * implementation.
 *
 * @author James Carman
 * @version 1.0
 */
public class JavassistProxyFactory extends AbstractSubclassingProxyFactory
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------
    private static final ProxyClassCache delegatingProxyClassCache = new ProxyClassCache(
            new DelegatingProxyClassGenerator() );
    private static final ProxyClassCache interceptingProxyClassCache = new ProxyClassCache(
            new InterceptingProxyClassGenerator() );
    private static final ProxyClassCache invocationHandlerProxyClassCache = new ProxyClassCache(
            new InvocationHandlerProxyClassGenerator() );

//----------------------------------------------------------------------------------------------------------------------
// ProxyFactory Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object createDelegatorProxy( ClassLoader classLoader, ObjectProvider targetProvider,
                                        Class[] proxyClasses )
    {
        try
        {
            final Class clazz = delegatingProxyClassCache.getProxyClass( classLoader, proxyClasses );
            return clazz.getConstructor( ProxyUtils.toClassArray( ObjectProvider.class ) ).newInstance( ProxyUtils.toObjectArray( targetProvider ) );
        }
        catch( Exception e )
        {
            throw new ProxyFactoryException( "Unable to instantiate proxy from generated proxy class.", e );
        }
    }

    public Object createInterceptorProxy( ClassLoader classLoader, Object target, MethodInterceptor interceptor,
                                          Class[] proxyClasses )
    {
        try
        {
            final Class clazz = interceptingProxyClassCache.getProxyClass( classLoader, proxyClasses );
            final Method[] methods = AbstractProxyClassGenerator.getImplementationMethods( proxyClasses );
            return clazz.getConstructor( new Class[] { Method[].class, Object.class, MethodInterceptor.class } )
                    .newInstance( new Object[] { methods, target, interceptor } );
        }
        catch( Exception e )
        {
            throw new ProxyFactoryException( "Unable to instantiate proxy class instance.", e );
        }
    }

    public Object createInvocationHandlerProxy( ClassLoader classLoader, InvocationHandler invocationHandler,
                                                Class[] proxyClasses )
    {
        try
        {
            final Class clazz = invocationHandlerProxyClassCache.getProxyClass( classLoader, proxyClasses );
            final Method[] methods = AbstractProxyClassGenerator.getImplementationMethods( proxyClasses );
            return clazz.getConstructor( new Class[] { Method[].class, InvocationHandler.class } )
                    .newInstance( new Object[] { methods, invocationHandler } );
        }
        catch( Exception e )
        {
            throw new ProxyFactoryException( "Unable to instantiate proxy from generated proxy class.", e );
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class InvocationHandlerProxyClassGenerator extends AbstractProxyClassGenerator
    {
        public Class generateProxyClass( ClassLoader classLoader, Class[] proxyClasses )
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass( getSuperclass( proxyClasses ) );
                final Method[] methods = getImplementationMethods( proxyClasses );
                JavassistUtils.addInterfaces( proxyClass, toInterfaces( proxyClasses ) );
                JavassistUtils.addField( Method[].class, "methods", proxyClass );
                JavassistUtils.addField( InvocationHandler.class, "invocationHandler", proxyClass );
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve(
                                new Class[]{Method[].class, InvocationHandler.class} ),
                        proxyClass );
                proxyConstructor
                        .setBody( "{\n\tthis.methods = $1;\n\tthis.invocationHandler = $2; }" );
                proxyClass.addConstructor( proxyConstructor );
                for( int i = 0; i < methods.length; ++i )
                {
                    final CtMethod method = new CtMethod( JavassistUtils.resolve( methods[i].getReturnType() ),
                                                          methods[i].getName(),
                                                          JavassistUtils.resolve( methods[i].getParameterTypes() ),
                                                          proxyClass );
                    final String body = "{\n\t return ( $r ) invocationHandler.invoke( this, methods[" + i +
                                        "], $args );\n }";
                    method.setBody( body );
                    proxyClass.addMethod( method );
                }
                return proxyClass.toClass( classLoader );
            }
            catch( CannotCompileException e )
            {
                throw new ProxyFactoryException( "Could not compile class.", e );
            }
        }
    }

    private static class InterceptingProxyClassGenerator extends AbstractProxyClassGenerator
    {
        public Class generateProxyClass( ClassLoader classLoader, Class[] proxyClasses )
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass( getSuperclass( proxyClasses ) );
                final Method[] methods = getImplementationMethods( proxyClasses );
                JavassistUtils.addInterfaces( proxyClass, toInterfaces( proxyClasses ) );
                JavassistUtils.addField( Method[].class, "methods", proxyClass );
                JavassistUtils.addField( Object.class, "target", proxyClass );
                JavassistUtils.addField( MethodInterceptor.class, "interceptor", proxyClass );
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve(
                                new Class[]{Method[].class, Object.class, MethodInterceptor.class} ),
                        proxyClass );
                proxyConstructor
                        .setBody( "{\n\tthis.methods = $1;\n\tthis.target = $2;\n\tthis.interceptor = $3; }" );
                proxyClass.addConstructor( proxyConstructor );
                for( int i = 0; i < methods.length; ++i )
                {
                    final CtMethod method = new CtMethod( JavassistUtils.resolve( methods[i].getReturnType() ),
                                                          methods[i].getName(),
                                                          JavassistUtils.resolve( methods[i].getParameterTypes() ),
                                                          proxyClass );
                    final Class invocationClass = JavassistMethodInvocation
                            .getMethodInvocationClass( classLoader, methods[i] );
                    final String body = "{\n\t return ( $r ) interceptor.invoke( new " + invocationClass.getName() +
                                        "( methods[" + i + "], target, $args ) );\n }";
                    method.setBody( body );
                    proxyClass.addMethod( method );
                }
                return proxyClass.toClass( classLoader );
            }
            catch( CannotCompileException e )
            {
                throw new ProxyFactoryException( "Could not compile class.", e );
            }
        }
    }

    private static class DelegatingProxyClassGenerator extends AbstractProxyClassGenerator
    {
        public Class generateProxyClass( ClassLoader classLoader, Class[] proxyClasses )
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass( getSuperclass( proxyClasses ) );
                JavassistUtils.addField( ObjectProvider.class, "provider", proxyClass );
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve( new Class[]{ObjectProvider.class} ),
                        proxyClass );
                proxyConstructor.setBody( "{ this.provider = $1; }" );
                proxyClass.addConstructor( proxyConstructor );
                JavassistUtils.addInterfaces( proxyClass, toInterfaces( proxyClasses ) );
                final Method[] methods = getImplementationMethods( proxyClasses );
                for( int i = 0; i < methods.length; ++i )
                {
                    final Method method = methods[i];
                    final CtMethod ctMethod = new CtMethod( JavassistUtils.resolve( method.getReturnType() ),
                                                            method.getName(),
                                                            JavassistUtils.resolve( method.getParameterTypes() ),
                                                            proxyClass );
                    ctMethod.setBody( "{ return ( $r ) ( ( " + method.getDeclaringClass().getName() +
                                      " )provider.getObject() )." +
                                      method.getName() + "($$); }" );
                    proxyClass.addMethod( ctMethod );

                }
                return proxyClass.toClass( classLoader );
            }
            catch( CannotCompileException e )
            {
                throw new ProxyFactoryException( "Could not compile class.", e );
            }
        }
    }
}

