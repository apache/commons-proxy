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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * @author James Carman
 * @version 1.0
 */
public abstract class JavassistMethodInvocation implements MethodInvocation
{
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

    private static final WeakHashMap invocationClassCache = new WeakHashMap();

    public synchronized static Class getMethodInvocationClass( ClassLoader classLoader, Method interfaceMethod )
            throws CannotCompileException
    {
        final CacheKey key = new CacheKey( classLoader, interfaceMethod );
        Class invocationClass = ( Class ) invocationClassCache.get( key );
        if( invocationClass == null )
        {
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
            invocationClassCache.put( key, invocationClass );
        }
        return invocationClass;
    }

    private static class CacheKey
    {
        private final ClassLoader classLoader;
        private final Method method;

        public CacheKey( ClassLoader classLoader, Method method )
        {
            this.classLoader = classLoader;
            this.method = method;
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
            final CacheKey cacheKey = ( CacheKey ) o;
            if( classLoader != null ? !classLoader.equals( cacheKey.classLoader ) : cacheKey.classLoader != null )
            {
                return false;
            }
            if( method != null ? !method.equals( cacheKey.method ) : cacheKey.method != null )
            {
                return false;
            }
            return true;
        }

        public int hashCode()
        {
            int result;
            result = ( classLoader != null ? classLoader.hashCode() : 0 );
            result = 29 * result + ( method != null ? method.hashCode() : 0 );
            return result;
        }
    }

    public static Method[] getImplementationMethods( Class... proxyInterfaces )
    {
        final Set<MethodSignature> signatures = new HashSet<MethodSignature>();
        final List<Method> resultingMethods = new LinkedList<Method>();
        for( int i = 0; i < proxyInterfaces.length; i++ )
        {
            Class proxyInterface = proxyInterfaces[i];
            final Method[] methods = proxyInterface.getDeclaredMethods();
            for( int j = 0; j < methods.length; j++ )
            {
                final MethodSignature signature = new MethodSignature( methods[j] );
                if( !signatures.contains( signature ) )
                {
                    signatures.add( signature );
                    resultingMethods.add( methods[j] );
                }
            }
        }
        final Method[] results = new Method[resultingMethods.size()];
        return resultingMethods.toArray( results );
    }

    private static class MethodSignature
    {
        private final String name;
        private final List<Class> parameterTypes;

        public MethodSignature( Method method )
        {
            this.name = method.getName();
            this.parameterTypes = Arrays.<Class>asList( method.getParameterTypes() );
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
            final MethodSignature that = ( MethodSignature ) o;
            if( name != null ? !name.equals( that.name ) : that.name != null )
            {
                return false;
            }
            if( parameterTypes != null ? !parameterTypes.equals( that.parameterTypes ) :
                that.parameterTypes != null )
            {
                return false;
            }
            return true;
        }

        public int hashCode()
        {
            int result;
            result = ( name != null ? name.hashCode() : 0 );
            result = 29 * result + ( parameterTypes != null ? parameterTypes.hashCode() : 0 );
            return result;
        }
    }
}
