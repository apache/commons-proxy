/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.proxy.factory.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.exception.ObjectProviderException;
import org.apache.commons.proxy.factory.AbstractProxyFactory;

import java.lang.reflect.Method;

/**
 * A <a href="http://www.jboss.org/products/javassist">Javassist</a>-based {@link org.apache.commons.proxy.ProxyFactory}
 * implementation.
 * @author James Carman
 * @version 1.0
 */
public class JavassistProxyFactory extends AbstractProxyFactory
{
    private static int classNumber = 0;
    private static final ClassPool classPool = ClassPool.getDefault();

    public Object createInterceptorProxy( ClassLoader classLoader, Object target, MethodInterceptor interceptor, Class... proxyInterfaces )
    {
        return null;
    }

    public Object createProxy( ClassLoader classLoader, ObjectProvider targetProvider, Class... proxyInterfaces )
    {
        try
        {
            final CtClass proxyClass = createClass();
            final CtField providerField = new CtField( resolve( targetProvider.getClass() ), "provider", proxyClass );
            proxyClass.addField( providerField );
            final CtConstructor proxyConstructor = new CtConstructor( resolve( new Class[]{targetProvider.getClass()} ), proxyClass );
            proxyConstructor.setBody( "{ this.provider = $1; }" );
            proxyClass.addConstructor( proxyConstructor );
            for( Class proxyInterface : proxyInterfaces )
            {
                proxyClass.addInterface( resolve( proxyInterface ) );
                final Method[] methods = proxyInterface.getMethods();
                for( int i = 0; i < methods.length; ++i )
                {
                    final CtMethod method = new CtMethod( resolve( methods[i].getReturnType() ), methods[i].getName(), resolve( methods[i].getParameterTypes() ), proxyClass );
                    method.setBody( "{ return ( $r ) ( ( " + proxyInterface.getName() + " )provider.getObject() )." + methods[i].getName() + "($$); }" );
                    proxyClass.addMethod( method );
                }
            }
            final Class clazz = proxyClass.toClass( classLoader );
            return clazz.getConstructor( targetProvider.getClass() ).newInstance( targetProvider );
        }
        catch( CannotCompileException e )
        {
            throw new ObjectProviderException( "Could not compile class.", e );
        }
        catch( NoSuchMethodException e )
        {
            throw new ObjectProviderException( "Could not find constructor in generated proxy class.", e );
        }
        catch( Exception e )
        {
            throw new ObjectProviderException( "Unable to instantiate proxy from generated proxy class.", e );
        }
    }

    public static CtClass resolve( Class clazz )
    {
        try
        {
            return classPool.get( clazz.getName() );
        }
        catch( NotFoundException e )
        {
            throw new ObjectProviderException( "Unable to find class " + clazz.getName() + " in default Javassist class pool.", e );
        }
    }

    public static CtClass[] resolve( Class[] classes )
    {
        final CtClass[] ctClasses = new CtClass[classes.length];
        for( int i = 0; i < ctClasses.length; ++i )
        {
            ctClasses[i] = resolve( classes[i] );
        }
        return ctClasses;
    }

    public static CtClass createClass()
    {
        return createClass( Object.class );
    }

    public static CtClass createClass( Class superclass )
    {
        return classPool.makeClass( "JavassistUtilsGenerated_" + ( ++classNumber ), resolve( superclass ) );
    }
}
