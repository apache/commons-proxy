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
package org.apache.commons.proxy;

import junit.framework.TestCase;
import org.apache.commons.proxy.exception.ProxyFactoryException;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.apache.commons.proxy.factory.javassist.JavassistProxyFactory;
import org.apache.commons.proxy.factory.reflect.ReflectionProxyFactory;
import org.apache.commons.proxy.factory.util.AbstractProxyFactory;
import org.apache.commons.proxy.util.DuplicateEcho;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

public class TestProxyUtils extends TestCase
{
    private Properties prevProperties;

    protected void setUp() throws Exception
    {
        prevProperties = System.getProperties();
        System.setProperties( new Properties() );
    }

    protected void tearDown() throws Exception
    {
        System.setProperties( prevProperties );
    }

    public void testCreateNullObject() throws Exception
    {
        final Echo nullEcho = ( Echo ) ProxyUtils
                .createNullObject( new JavassistProxyFactory(), new Class[]{ Echo.class } );
        assertNull( nullEcho.echoBack( "hello" ) );
        assertNull( nullEcho.echoBack( "hello", "world" ) );
        assertEquals( ( int ) 0, nullEcho.echoBack( 12345 ) );
    }

    public void testCreateNullObjectWithClassLoader() throws Exception
    {
        final Echo nullEcho = ( Echo ) ProxyUtils.createNullObject( new JavassistProxyFactory(),
                                                                    Echo.class.getClassLoader(),
                                                                    new Class[]{ Echo.class } );
        assertNull( nullEcho.echoBack( "hello" ) );
        assertNull( nullEcho.echoBack( "hello", "world" ) );
        assertEquals( ( int ) 0, nullEcho.echoBack( 12345 ) );
    }

    public void testGetAllInterfaces()
    {
        assertNull( ProxyUtils.getAllInterfaces( null ) );
        assertEquals( Arrays.asList( new Class[] { DuplicateEcho.class, Serializable.class, Echo.class } ), Arrays.asList( ProxyUtils.getAllInterfaces( EchoImpl.class ) ) );
    }

    public void testGetJavaClassName() throws Exception
    {
        assertEquals( "java.lang.Object[]", ProxyUtils.getJavaClassName( Object[].class ) );
        assertEquals( "java.lang.Object[][]", ProxyUtils.getJavaClassName( Object[][].class ) );
        assertEquals( "java.lang.String[][][]", ProxyUtils.getJavaClassName( String[][][].class ) );
        assertEquals( "int", ProxyUtils.getJavaClassName( Integer.TYPE ) );
        assertEquals( "float", ProxyUtils.getJavaClassName( Float.TYPE ) );
        assertEquals( "long", ProxyUtils.getJavaClassName( Long.TYPE ) );
        assertEquals( "double", ProxyUtils.getJavaClassName( Double.TYPE ) );
        assertEquals( "short", ProxyUtils.getJavaClassName( Short.TYPE ) );
        assertEquals( "byte", ProxyUtils.getJavaClassName( Byte.TYPE ) );
        assertEquals( "char", ProxyUtils.getJavaClassName( Character.TYPE ) );
        assertEquals( "boolean", ProxyUtils.getJavaClassName( Boolean.TYPE ) );
    }



    public void testGetProxyFactory() throws Exception
    {
        assertTrue( ProxyUtils.getProxyFactory() instanceof JavassistProxyFactory );
        System.setProperty( ProxyUtils.PROXY_FACTORY_PROPERTY, "java.lang.String" );
        assertTrue( ProxyUtils.getProxyFactory() instanceof JavassistProxyFactory );
        System.setProperty( ProxyUtils.PROXY_FACTORY_PROPERTY, CglibProxyFactory.class.getName() );
        assertTrue( ProxyUtils.getProxyFactory() instanceof CglibProxyFactory );
        System.setProperty( ProxyUtils.PROXY_FACTORY_PROPERTY, ReflectionProxyFactory.class.getName() );
        assertTrue( ProxyUtils.getProxyFactory() instanceof ReflectionProxyFactory );
        System.setProperty( ProxyUtils.PROXY_FACTORY_PROPERTY, "" );
        ClassLoader cl = new IsolatingClassLoader( JavassistProxyFactory.class,
                                                   Thread.currentThread().getContextClassLoader() );
        assertTrue( ProxyUtils.getProxyFactory( cl ) instanceof CglibProxyFactory );
        cl = new IsolatingClassLoader( CglibProxyFactory.class, cl );
        assertTrue( ProxyUtils.getProxyFactory( cl ) instanceof ReflectionProxyFactory );
        cl = new IsolatingClassLoader( ReflectionProxyFactory.class, cl );
        try
        {
            ProxyUtils.getProxyFactory( cl );
            fail();
        }
        catch( ProxyFactoryException e )
        {
        }
    }

    public void testPrivateProxyFactoryConstructor()
    {
        System.setProperty( ProxyUtils.PROXY_FACTORY_PROPERTY, PrivateProxyFactory.class.getName() );
        assertTrue( ProxyUtils.getProxyFactory() instanceof JavassistProxyFactory );

    }

    public void testNonConcreteProxyFactoryClass()
    {
        System.setProperty( ProxyUtils.PROXY_FACTORY_PROPERTY, AbstractProxyFactory.class.getName() );
        assertTrue( ProxyUtils.getProxyFactory() instanceof JavassistProxyFactory );
    }

    private static class IsolatingClassLoader extends ClassLoader
    {
        private Class isolatedClass;

        public IsolatingClassLoader( Class isolatedClass, ClassLoader parent )
        {
            super( parent );
            this.isolatedClass = isolatedClass;
        }

        public Class loadClass( String name ) throws ClassNotFoundException
        {
            if( isolatedClass.getName().equals( name ) )
            {
                throw new ClassNotFoundException( name + " not found." );
            }
            return getParent().loadClass( name );
        }
    }

    public static class PrivateProxyFactory extends ReflectionProxyFactory
    {
        private PrivateProxyFactory()
        {
        }
    }
}