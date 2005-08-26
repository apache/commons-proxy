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
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.exception.ObjectProviderException;
import org.apache.commons.proxy.exception.ProxyFactoryException;
import org.apache.commons.proxy.factory.AbstractProxyFactory;

import java.lang.reflect.Method;

/**
 * A <a href="http://www.jboss.org/products/javassist">Javassist</a>-based {@link org.apache.commons.proxy.ProxyFactory}
 * implementation.
 *
 * @author James Carman
 * @version 1.0
 */
public class JavassistProxyFactory extends AbstractProxyFactory
{
    private static int classNumber = 0;
    private static final ClassPool classPool = ClassPool.getDefault();

    private void addField( Class fieldType, String fieldName, CtClass enclosingClass )
    {
        try
        {
            enclosingClass.addField( new CtField( resolve( fieldType ), fieldName, enclosingClass ) );
        }
        catch( CannotCompileException e )
        {
            throw new ProxyFactoryException( "Unable to add field named " + fieldName + " of type " + fieldType.getName() + " to class " + enclosingClass.getName(), e );
        }
    }

    public Object createInterceptorProxy( ClassLoader classLoader, Object target, MethodInterceptor interceptor, Class... proxyInterfaces )
    {
        try
        {
            final CtClass proxyClass = createClass();
            addField( target.getClass(), "target", proxyClass );
            addField( MethodInterceptor.class, "interceptor", proxyClass );
            final CtConstructor proxyConstructor = new CtConstructor( resolve( new Class[]{target.getClass(), MethodInterceptor.class} ), proxyClass );
            proxyConstructor.setBody( "{ this.target = $1;\nthis.interceptor = $2; }" );
            proxyClass.addConstructor( proxyConstructor );
            for( Class proxyInterface : proxyInterfaces )
            {
                proxyClass.addInterface( resolve( proxyInterface ) );
                final Method[] methods = proxyInterface.getMethods();
                for( int i = 0; i < methods.length; ++i )
                {
                    final CtMethod method = new CtMethod( resolve( methods[i].getReturnType() ), methods[i].getName(), resolve( methods[i].getParameterTypes() ), proxyClass );
                    final Class invocationClass = createMethodInvocationClass( methods[i], target.getClass(), classLoader );
                    final String body = "{\n\t return ( $r ) interceptor.invoke( new " + invocationClass.getName() + "( target, $$ ) );\n }";
                    log.debug( method.getName() + "() method body:\n" + body );
                    method.setBody( body );
                    proxyClass.addMethod( method );
                }
            }
            final Class clazz = proxyClass.toClass( classLoader );
            return clazz.getConstructor( target.getClass(), MethodInterceptor.class ).newInstance( target, interceptor );
        }
        catch( CannotCompileException e )
        {
            throw new ProxyFactoryException( "Could not compile class.", e );
        }
        catch( NoSuchMethodException e )
        {
            throw new ProxyFactoryException( "Could not find constructor in generated proxy class.", e );
        }
        catch( Exception e )
        {
            throw new ProxyFactoryException( "Unable to instantiate proxy from generated proxy class.", e );
        }
    }

    private Class createMethodInvocationClass( Method method, Class targetClass, ClassLoader classLoader )
    {
        try
        {
            final CtClass invocationClass = createClass();
            invocationClass.addInterface( resolve( MethodInvocation.class ) );
            addField( targetClass, "target", invocationClass );
            final Class[] argumentTypes = method.getParameterTypes();
            final Class[] constructorArgs = new Class[argumentTypes.length + 1];
            constructorArgs[0] = targetClass;
            for( int i = 0; i < argumentTypes.length; i++ )
            {
                Class argumentType = argumentTypes[i];
                final CtField argumentField = new CtField( resolve( argumentType ), "argument" + i, invocationClass );
                invocationClass.addField( argumentField );
                constructorArgs[i + 1] = argumentType;
            }
            final CtConstructor constructor = new CtConstructor( resolve( constructorArgs ), invocationClass );
            final StringBuffer constructorBody = new StringBuffer( "{\n" );
            constructorBody.append( "\tthis.target = $1;\n" );
            for( int i = 0; i < argumentTypes.length; i++ )
            {
                constructorBody.append( "\tthis.argument" ).append( i ).append( " = $" ).append( 2 + i ).append( ";\n" );

            }
            constructorBody.append( "}" );
            log.debug( "Constructor body:\n" + constructorBody );
            constructor.setBody( constructorBody.toString() );
            invocationClass.addConstructor( constructor );
            // proceed()...
            final CtMethod proceedMethod = new CtMethod( resolve( Object.class ), "proceed", new CtClass[0], invocationClass );
            final String proceedBody = generateProceedBody( method, argumentTypes );
            log.debug( "Proceed method body:\n" + proceedBody );
            proceedMethod.setBody( proceedBody.toString() );
            invocationClass.addMethod( proceedMethod );
            return invocationClass.toClass( classLoader );
        }
        catch( CannotCompileException e )
        {
            throw new ProxyFactoryException( "Could not compile Javassist generated class.", e );
        }
    }

    private String generateProceedBody( Method method, Class[] argumentTypes )
    {
        final StringBuffer proceedBody = new StringBuffer( "{\n" );
        if( !Void.TYPE.equals( method.getReturnType() ) )
        {
            proceedBody.append( "\treturn " );
        }
        else
        {
            proceedBody.append( "\t" );
        }
        proceedBody.append( "target." ).append( method.getName() ).append( "(" );
        for( int i = 0; i < argumentTypes.length; ++i )
        {
            proceedBody.append( "argument" ).append( i );
            if( i != argumentTypes.length - 1 )
            {
                proceedBody.append( ", " );
            }
        }
        proceedBody.append( ");\n" );
        if( Void.TYPE.equals( method.getReturnType() ) )
        {
            proceedBody.append( "return null;" );
        }
        proceedBody.append( "}" );
        return proceedBody.toString();
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
            throw new ProxyFactoryException( "Could not compile class.", e );
        }
        catch( NoSuchMethodException e )
        {
            throw new ProxyFactoryException( "Could not find constructor in generated proxy class.", e );
        }
        catch( Exception e )
        {
            throw new ProxyFactoryException( "Unable to instantiate proxy from generated proxy class.", e );
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
        return classPool.makeClass( "JavassistProxyFactoryGenerated_" + ( ++classNumber ), resolve( superclass ) );
    }
}
