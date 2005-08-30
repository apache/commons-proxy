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
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.proxy.DelegateProvider;
import org.apache.commons.proxy.exception.DelegateProviderException;
import org.apache.commons.proxy.exception.ProxyFactoryException;
import org.apache.commons.proxy.factory.AbstractProxyFactory;

import java.lang.reflect.AccessibleObject;
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
            throw new ProxyFactoryException( "Unable to add field named " + fieldName + " of type " +
                                             fieldType.getName() + " to class " + enclosingClass.getName(), e );
        }
    }

    public Object createInterceptingProxy( ClassLoader classLoader, Object target, MethodInterceptor interceptor,
                                           Class... proxyInterfaces )
    {
        try
        {
            final CtClass proxyClass = createClass();
            addField( target.getClass(), "target", proxyClass );
            addField( MethodInterceptor.class, "interceptor", proxyClass );
            final CtConstructor proxyConstructor = new CtConstructor(
                    resolve( new Class[]{target.getClass(), MethodInterceptor.class} ), proxyClass );
            proxyConstructor.setBody( "{ this.target = $1;\nthis.interceptor = $2; }" );
            proxyClass.addConstructor( proxyConstructor );
            for( Class proxyInterface : proxyInterfaces )
            {
                proxyClass.addInterface( resolve( proxyInterface ) );
                final Method[] methods = proxyInterface.getMethods();
                for( int i = 0; i < methods.length; ++i )
                {
                    final CtMethod method = new CtMethod( resolve( methods[i].getReturnType() ), methods[i].getName(),
                                                          resolve( methods[i].getParameterTypes() ), proxyClass );
                    final Class invocationClass = createMethodInvocationClass( methods[i], target.getClass(),
                                                                               classLoader );
                    final String body = "{\n\t return ( $r ) interceptor.invoke( new " + invocationClass.getName() +
                                        "( target, $$ ) );\n }";
                    log.debug( method.getName() + "() method body:\n" + body );
                    method.setBody( body );
                    proxyClass.addMethod( method );
                }
            }
            final Class clazz = proxyClass.toClass( classLoader );
            return clazz.getConstructor( target.getClass(), MethodInterceptor.class )
                    .newInstance( target, interceptor );
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
            addField( method.getDeclaringClass(), "target", invocationClass );
            addField( Object[].class, "arguments", invocationClass );
            final Class[] argumentTypes = method.getParameterTypes();
            final Class[] constructorArgs = new Class[argumentTypes.length + 1];
            constructorArgs[0] = targetClass;
            for( int i = 0; i < argumentTypes.length; i++ )
            {
                constructorArgs[i + 1] = argumentTypes[i];
            }
            final CtConstructor constructor = new CtConstructor( resolve( constructorArgs ), invocationClass );
            final StringBuffer constructorBody = new StringBuffer( "{\n" );
            constructorBody.append( "\tthis.target = $1;\n" );
            if( argumentTypes.length == 0 )
            {
                constructorBody.append( "\tthis.arguments = null;\n" );
            }
            else
            {
                constructorBody.append( "\tthis.arguments = new Object[" );
                constructorBody.append( argumentTypes.length );
                constructorBody.append( "];\n" );
            }
            for( int i = 0; i < argumentTypes.length; i++ )
            {
                constructorBody.append( "\tthis.arguments[" );
                constructorBody.append( i );
                constructorBody.append( "] = $" );
                constructorBody.append( i + 2 );
                constructorBody.append( ";\n" );
            }
            constructorBody.append( "}" );
            log.debug( "Constructor body:\n" + constructorBody );
            constructor.setBody( constructorBody.toString() );
            invocationClass.addConstructor( constructor );
            // proceed()...
            final CtMethod proceedMethod = new CtMethod( resolve( Object.class ), "proceed", new CtClass[0],
                                                         invocationClass );
            final String proceedBody = generateProceedBody( method, argumentTypes );
            log.debug( "Proceed method body:\n" + proceedBody );
            proceedMethod.setBody( proceedBody.toString() );
            invocationClass.addMethod( proceedMethod );
            addGetMethodMethod( invocationClass, argumentTypes, method );
            addGetArgumentsMethod( invocationClass );
            addGetStaticPartMethod( invocationClass );
            addGetThisMethod( invocationClass );
            return invocationClass.toClass( classLoader );
        }
        catch( CannotCompileException e )
        {
            throw new ProxyFactoryException( "Could not compile Javassist generated class.", e );
        }
    }

    private void addGetMethodMethod( CtClass invocationClass, Class[] argumentTypes, Method method )
            throws CannotCompileException
    {
        final CtMethod getMethodMethod = new CtMethod( resolve( Method.class ), "getMethod", resolve( new Class[0] ),
                                                       invocationClass );
        final StringBuffer getMethodBody = new StringBuffer();
        getMethodBody.append( "{\n\tfinal Class[] parameterTypes = new Class[" );
        getMethodBody.append( argumentTypes.length );
        getMethodBody.append( "];\n" );
        for( int i = 0; i < argumentTypes.length; ++i )
        {
            getMethodBody.append( "\tparameterTypes[" );
            getMethodBody.append( i );
            getMethodBody.append( "] = " );
            getMethodBody.append( argumentTypes[i].getName() );
            getMethodBody.append( ".class;\n" );
        }
        getMethodBody.append( "\ttry\n\t{\n\t\treturn " );
        getMethodBody.append( method.getDeclaringClass().getName() );
        getMethodBody.append( ".class.getMethod(\"" );
        getMethodBody.append( method.getName() );
        getMethodBody
                .append( "\", parameterTypes );\n\t}\n\tcatch( NoSuchMethodException e )\n\t{\n\t\treturn null;\n\t}" );
        getMethodBody.append( "}" );
        log.debug( "getMethod() body:\n" + getMethodBody.toString() );
        getMethodMethod.setBody( getMethodBody.toString() );
        invocationClass.addMethod( getMethodMethod );
    }

    private void addGetStaticPartMethod( CtClass invocationClass ) throws CannotCompileException
    {
        final CtMethod getStaticPartMethod = new CtMethod( resolve( AccessibleObject.class ), "getStaticPart",
                                                           resolve( new Class[0] ), invocationClass );
        final String getStaticPartBody = "{\n\treturn getMethod();\n}";
        log.debug( "getStaticPart() body:\n" + getStaticPartBody );
        getStaticPartMethod.setBody( getStaticPartBody );
        invocationClass.addMethod( getStaticPartMethod );
    }

    private void addGetThisMethod( CtClass invocationClass ) throws CannotCompileException
    {
        final CtMethod getThisMethod = new CtMethod( resolve( Object.class ), "getThis", resolve( new Class[0] ),
                                                     invocationClass );
        final String getThisMethodBody = "{\n\treturn target;\n}";
        log.debug( "getThis() body:\n" + getThisMethodBody );
        getThisMethod.setBody( getThisMethodBody );
        invocationClass.addMethod( getThisMethod );
    }

    private void addGetArgumentsMethod( CtClass invocationClass ) throws CannotCompileException
    {
        final CtMethod method = new CtMethod( resolve( Object[].class ), "getArguments", resolve( new Class[0] ),
                                              invocationClass );
        final String body = "{\n\treturn arguments;\n}";
        log.debug( "getArguments() body:\n" + body );
        method.setBody( body );
        invocationClass.addMethod( method );
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
        proceedBody.append( "target." );
        proceedBody.append( method.getName() );
        proceedBody.append( "(" );
        for( int i = 0; i < argumentTypes.length; ++i )
        {
            proceedBody.append( "(" );
            proceedBody.append( argumentTypes[i].getName() );
            proceedBody.append( ")arguments[" );
            proceedBody.append( i );
            proceedBody.append( "]" );
            if( i != argumentTypes.length - 1 )
            {
                proceedBody.append( ", " );
            }
        }
        proceedBody.append( ");\n" );
        if( Void.TYPE.equals( method.getReturnType() ) )
        {
            proceedBody.append( "\treturn null;\n" );
        }
        proceedBody.append( "}" );
        return proceedBody.toString();
    }

    public Object createDelegatingProxy( ClassLoader classLoader, DelegateProvider targetProvider,
                                         Class... proxyInterfaces )
    {
        try
        {
            final CtClass proxyClass = createClass();
            final CtField providerField = new CtField( resolve( targetProvider.getClass() ), "provider", proxyClass );
            proxyClass.addField( providerField );
            final CtConstructor proxyConstructor = new CtConstructor( resolve( new Class[]{targetProvider.getClass()} ),
                                                                      proxyClass );
            proxyConstructor.setBody( "{ this.provider = $1; }" );
            proxyClass.addConstructor( proxyConstructor );
            for( Class proxyInterface : proxyInterfaces )
            {
                proxyClass.addInterface( resolve( proxyInterface ) );
                final Method[] methods = proxyInterface.getMethods();
                for( int i = 0; i < methods.length; ++i )
                {
                    final CtMethod method = new CtMethod( resolve( methods[i].getReturnType() ), methods[i].getName(),
                                                          resolve( methods[i].getParameterTypes() ), proxyClass );
                    method.setBody( "{ return ( $r ) ( ( " + proxyInterface.getName() + " )provider.getDelegate() )." +
                                    methods[i].getName() + "($$); }" );
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
            return classPool.get( getJavaClassName( clazz ) );
        }
        catch( NotFoundException e )
        {
            throw new DelegateProviderException(
                    "Unable to find class " + clazz.getName() + " in default Javassist class pool.", e );
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

    public static String getJavaClassName( Class inputClass )
    {
        if( inputClass.isArray() )
        {
            return getJavaClassName( inputClass.getComponentType() ) + "[]";
        }
        return inputClass.getName();
    }
}
