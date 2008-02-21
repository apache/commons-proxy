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

package org.apache.commons.proxy.factory.util;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.exception.ProxyFactoryException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A useful superclass for a {@link ProxyFactory} which supports subclassing rather than merely implementing interfaces.
 * 
 * @author James Carman
 * @since 1.0
 */
public abstract class AbstractSubclassingProxyFactory extends ProxyFactory
{
//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    private static boolean hasSuitableDefaultConstructor( Class superclass )
    {
        final Constructor[] declaredConstructors = superclass.getDeclaredConstructors();
        for( int i = 0; i < declaredConstructors.length; i++ )
        {
            Constructor constructor = declaredConstructors[i];
            if( constructor.getParameterTypes().length == 0 && ( Modifier.isPublic( constructor.getModifiers() ) ||
                                                                 Modifier.isProtected( constructor.getModifiers() ) ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the <code>proxyClasses</code> transformed into an array of only the interface classes.
     *
     * @param proxyClasses the proxy classes
     * @return the <code>proxyClasses</code> transformed into an array of only the interface classes
     */
    protected static Class[] toInterfaces( Class[] proxyClasses )
    {
        final Collection interfaces = new LinkedList();
        for( int i = 0; i < proxyClasses.length; i++ )
        {
            Class proxyInterface = proxyClasses[i];
            if( proxyInterface.isInterface() )
            {
                interfaces.add( proxyInterface );
            }
        }
        return ( Class[] ) interfaces.toArray( new Class[interfaces.size()] );
    }

    private static Class[] toNonInterfaces( Class[] proxyClasses )
    {
        final List superclasses = new LinkedList();
        for( int i = 0; i < proxyClasses.length; i++ )
        {
            Class proxyClass = proxyClasses[i];
            if( !proxyClass.isInterface() )
            {
                superclasses.add( proxyClass );
            }
        }
        return ( Class[] ) superclasses.toArray( new Class[superclasses.size()] );
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Returns true if a suitable superclass can be found, given the desired <code>proxyClasses</code>.
     *
     * @param proxyClasses the proxy classes
     * @return true if a suitable superclass can be found, given the desired <code>proxyClasses</code>
     */
    public boolean canProxy( Class[] proxyClasses )
    {
        try
        {
            getSuperclass( proxyClasses );
            return true;
        }
        catch( ProxyFactoryException e )
        {
            return false;
        }
    }

    /**
     * Returns either {@link Object} if all of the <code>proxyClasses</code> are interfaces or the single non-interface
     * class from <code>proxyClasses</code>.
     *
     * @param proxyClasses the proxy classes
     * @return either {@link Object} if all of the <code>proxyClasses</code> are interfaces or the single non-interface
     *         class from <code>proxyClasses</code>
     * @throws ProxyFactoryException if multiple non-interface classes are contained in <code>proxyClasses</code> or any
     *                               of the non-interface classes are final
     */
    public static Class getSuperclass( Class[] proxyClasses )
    {
        final Class[] superclasses = toNonInterfaces( proxyClasses );
        switch( superclasses.length )
        {
            case 0:
                return Object.class;
            case 1:
                final Class superclass = superclasses[0];
                if( Modifier.isFinal( superclass.getModifiers() ) )
                {
                    throw new ProxyFactoryException(
                            "Proxy class cannot extend " + superclass.getName() + " as it is final." );
                }
                if( !hasSuitableDefaultConstructor( superclass ) )
                {
                    throw new ProxyFactoryException( "Proxy class cannot extend " + superclass.getName() +
                                                     ", because it has no visible \"default\" constructor." );
                }
                return superclass;
            default:
                final StringBuffer errorMessage = new StringBuffer( "Proxy class cannot extend " );
                for( int i = 0; i < superclasses.length; i++ )
                {
                    Class c = superclasses[i];
                    errorMessage.append( c.getName() );
                    if( i != superclasses.length - 1 )
                    {
                        errorMessage.append( ", " );
                    }
                }
                errorMessage.append( "; multiple inheritance not allowed." );
                throw new ProxyFactoryException( errorMessage.toString() );
        }
    }
}

