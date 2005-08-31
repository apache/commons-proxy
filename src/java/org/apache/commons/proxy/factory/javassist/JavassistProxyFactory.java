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
import org.apache.commons.proxy.exception.ProxyFactoryException;
import org.apache.commons.proxy.factory.AbstractProxyFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A <a href="http://www.jboss.org/products/javassist">Javassist</a>-based {@link org.apache.commons.proxy.ProxyFactory}
 * implementation.
 *
 * @author James Carman
 * @version 1.0
 */
public class JavassistProxyFactory extends AbstractProxyFactory
{
    private static HashMap<ProxyClassDescriptor, Class> delegatingProxyClassCache = new HashMap<ProxyClassDescriptor, Class>();
    private static HashMap<ProxyClassDescriptor, Class> interceptingProxyClassCache = new HashMap<ProxyClassDescriptor, Class>();

    public Object createInterceptingProxy( ClassLoader classLoader, Object target, MethodInterceptor interceptor,
                                           Class... proxyInterfaces )
    {
        synchronized( interceptingProxyClassCache )
        {
            final Method[] methods = getImplementationMethods( proxyInterfaces );
            final ProxyClassDescriptor key = new ProxyClassDescriptor( methods, classLoader );
            Class clazz = interceptingProxyClassCache.get( key );
            if( clazz == null )
            {
                log.debug( "Generating intercepting proxy class for interfaces " + Arrays.asList( proxyInterfaces ) +
                           " using class loader " + classLoader + "..." );
                try
                {
                    final CtClass proxyClass = JavassistUtils.createClass();
                    JavassistUtils.addInterfaces( proxyClass, proxyInterfaces );
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
                    clazz = proxyClass.toClass( classLoader );
                    interceptingProxyClassCache.put( key, clazz );
                }
                catch( CannotCompileException e )
                {
                    throw new ProxyFactoryException( "Could not compile class.", e );
                }
            }
            try
            {
                return clazz.getConstructor( Method[].class, Object.class, MethodInterceptor.class )
                        .newInstance( methods, target, interceptor );
            }
            catch( Exception e )
            {
                throw new ProxyFactoryException( "Unable to instantiate proxy class instance.", e );

            }
        }
    }

    public Object createDelegatingProxy( ClassLoader classLoader, ObjectProvider targetProvider,
                                         Class... proxyInterfaces )
    {
        synchronized( delegatingProxyClassCache )
        {
            final Method[] methods = getImplementationMethods( proxyInterfaces );
            final ProxyClassDescriptor key = new ProxyClassDescriptor( methods, classLoader );
            Class clazz = delegatingProxyClassCache.get( key );
            if( clazz == null )
            {
                log.debug( "Generating delegating proxy class for interfaces " + Arrays.asList( proxyInterfaces ) +
                           " using class loader " + classLoader + "..." );
                try
                {
                    final CtClass proxyClass = JavassistUtils.createClass();
                    JavassistUtils.addField( ObjectProvider.class, "provider", proxyClass );
                    final CtConstructor proxyConstructor = new CtConstructor(
                            JavassistUtils.resolve( new Class[]{ObjectProvider.class} ),
                            proxyClass );
                    proxyConstructor.setBody( "{ this.provider = $1; }" );
                    proxyClass.addConstructor( proxyConstructor );
                    addMethods( proxyInterfaces, proxyClass, new DelegatingMethodBodyProvider() );
                    clazz = proxyClass.toClass( classLoader );
                    delegatingProxyClassCache.put( key, clazz );
                }
                catch( CannotCompileException e )
                {
                    throw new ProxyFactoryException( "Could not compile class.", e );
                }
            }
            try
            {
                return clazz.getConstructor( ObjectProvider.class ).newInstance( targetProvider );
            }
            catch( Exception e )
            {
                throw new ProxyFactoryException( "Unable to instantiate proxy from generated proxy class.", e );
            }
        }
    }

    private void addMethods( Class[] proxyInterfaces, CtClass proxyClass, MethodBodyProvider methodBodyProvider )
            throws CannotCompileException
    {
        JavassistUtils.addInterfaces( proxyClass, proxyInterfaces );
        final Method[] methods = getImplementationMethods( proxyInterfaces );
        for( int i = 0; i < methods.length; ++i )
        {
            final Method method = methods[i];
            final CtMethod ctMethod = new CtMethod( JavassistUtils.resolve( method.getReturnType() ),
                                                    method.getName(),
                                                    JavassistUtils.resolve( method.getParameterTypes() ),
                                                    proxyClass );
            ctMethod.setBody( methodBodyProvider.getMethodBody( method ) );
            proxyClass.addMethod( ctMethod );
        }
    }

    private interface MethodBodyProvider
    {
        public String getMethodBody( Method method );
    }

    private class DelegatingMethodBodyProvider implements MethodBodyProvider
    {
        public String getMethodBody( Method method )
        {
            return "{ return ( $r ) ( ( " + method.getDeclaringClass().getName() + " )provider.getDelegate() )." +
                   method.getName() + "($$); }";
        }
    }

    private static class ProxyClassDescriptor
    {
        private final List<Method> methods;
        private final ClassLoader classLoader;

        public ProxyClassDescriptor( Method[] methods, ClassLoader classLoader )
        {
            this.methods = new ArrayList<Method>( Arrays.asList( methods ) );
            this.classLoader = classLoader;
        }

        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            final ProxyClassDescriptor that = ( ProxyClassDescriptor ) o;
            if( classLoader != null ? !classLoader.equals( that.classLoader ) : that.classLoader != null )
            {
                return false;
            }
            if( methods != null ? !methods.equals( that.methods ) : that.methods != null )
            {
                return false;
            }
            return true;
        }

        public int hashCode()
        {
            int result;
            result = ( methods != null ? methods.hashCode() : 0 );
            result = 29 * result + ( classLoader != null ? classLoader.hashCode() : 0 );
            return result;
        }
    }
}
