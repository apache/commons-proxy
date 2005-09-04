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
import org.aopalliance.intercept.MethodInvocation;

import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class JavassistMethodInvocation implements MethodInvocation
{
    private static WeakHashMap<ClassLoader, Map<String, WeakReference<Class>>> loaderToClassCache = new WeakHashMap<ClassLoader, Map<String, WeakReference<Class>>>();
    protected final Method method;
    protected final Object target;
    protected final Object[] arguments;

    public JavassistMethodInvocation( Method method, Object target, Object[] arguments )
    {
        this.method = method;
        this.target = target;
        this.arguments = ( arguments == null || arguments.length == 0 ? null : arguments );
    }

    public Method getMethod()
    {
        return method;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    public Object getThis()
    {
        return target;
    }

    public AccessibleObject getStaticPart()
    {
        return method;
    }

    private static Map<String, WeakReference<Class>> getClassCache( ClassLoader classLoader )
    {
        Map<String, WeakReference<Class>> cache = loaderToClassCache.get( classLoader );
        if( cache == null )
        {
            cache = new HashMap<String, WeakReference<Class>>();
            loaderToClassCache.put( classLoader, cache );
        }
        return cache;
    }

    private static String toClassCacheKey( Method method )
    {
        return String.valueOf( method );
    }

    public synchronized static Class getMethodInvocationClass( ClassLoader classLoader, Method interfaceMethod )
            throws CannotCompileException
    {
        final Map<String, WeakReference<Class>> classCache = getClassCache( classLoader );
        final String key = toClassCacheKey( interfaceMethod );
        final WeakReference<Class> invocationClassRef = classCache.get( key );
        Class invocationClass;
        if( invocationClassRef == null )
        {
            invocationClass = createInvocationClass( classLoader, interfaceMethod );
            classCache.put( key, new WeakReference<Class>( invocationClass ) );
        }
        else
        {
            synchronized( invocationClassRef )
            {
                invocationClass = invocationClassRef.get();
                if( invocationClass == null )
                {
                    invocationClass = createInvocationClass( classLoader, interfaceMethod );
                    classCache.put( key, new WeakReference<Class>( invocationClass ) );
                }
            }
        }
        return invocationClass;
    }

    private static Class createInvocationClass( ClassLoader classLoader, Method interfaceMethod )
            throws CannotCompileException
    {
        Class invocationClass;
        final CtClass ctClass = JavassistUtils.createClass(
                interfaceMethod.getDeclaringClass().getSimpleName() + "_" + interfaceMethod.getName() +
                "_invocation",
                JavassistMethodInvocation.class );
        final CtConstructor constructor = new CtConstructor(
                JavassistUtils.resolve( new Class[]{Method.class, Object.class, Object[].class} ), ctClass );
        constructor.setBody( "{\n\tsuper($$);\n}" );
        ctClass.addConstructor( constructor );
        final CtMethod proceedMethod = new CtMethod( JavassistUtils.resolve( Object.class ), "proceed",
                                                     JavassistUtils.resolve( new Class[0] ), ctClass );
        final Class[] argumentTypes = interfaceMethod.getParameterTypes();
        final StringBuffer proceedBody = new StringBuffer( "{\n" );
        if( !Void.TYPE.equals( interfaceMethod.getReturnType() ) )
        {
            proceedBody.append( "\treturn " );
        }
        else
        {
            proceedBody.append( "\t" );
        }
        proceedBody.append( "( (" );
        proceedBody.append( interfaceMethod.getDeclaringClass().getName() );
        proceedBody.append( " )target )." );
        proceedBody.append( interfaceMethod.getName() );
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
        if( Void.TYPE.equals( interfaceMethod.getReturnType() ) )
        {
            proceedBody.append( "\treturn null;\n" );
        }
        proceedBody.append( "}" );
        proceedMethod.setBody( proceedBody.toString() );
        ctClass.addMethod( proceedMethod );
        invocationClass = ctClass.toClass( classLoader );
        return invocationClass;
    }
}
