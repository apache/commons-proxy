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
package org.apache.commons.proxy.provider;

import org.apache.commons.proxy.exception.ObjectProviderException;

import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Provides a reference to a session bean by looking up the home object and calling (via reflection)
 * the no-argument create() method.  This will work for both local and remote session beans.
 *
 * @author James Carman
 * @version 1.0
 */
public class SessionBeanProvider extends AbstractObjectProvider
{
    private final String jndiName;
    private final Class serviceInterface;
    private final Class homeInterface;

    public SessionBeanProvider( String jndiName, Class serviceInterface, Class homeInterface )
    {
        this.jndiName = jndiName;
        this.serviceInterface = serviceInterface;
        this.homeInterface = homeInterface;
    }

    public Object getObject()
    {
        try
        {
            Object homeObject = PortableRemoteObject.narrow( new InitialContext().lookup( jndiName ), homeInterface );
            final Method createMethod = homeObject.getClass().getMethod( "create" );
            return serviceInterface.cast( createMethod.invoke( homeObject ) );
        }
        catch( NoSuchMethodException e )
        {
            throw new ObjectProviderException(
                    "Unable to find no-arg create() method on home interface " + homeInterface.getName() + ".", e );
        }
        catch( IllegalAccessException e )
        {
            throw new ObjectProviderException(
                    "No-arg create() method on home interface " + homeInterface.getName() + " is not accessible.",
                    e ); // Should never happen!
        }
        catch( NamingException e )
        {
            throw new ObjectProviderException( "Unable to lookup EJB home object in JNDI.", e );
        }
        catch( InvocationTargetException e )
        {
            throw new ObjectProviderException(
                    "No-arg create() method on home interface " + homeInterface.getName() + " threw an exception.", e );
        }
    }
}
