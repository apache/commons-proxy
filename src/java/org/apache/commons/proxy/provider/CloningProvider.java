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

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyUtils;
import org.apache.commons.proxy.exception.ObjectProviderException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Merely calls <code>clone()</code> (reflectively) on the given {@link Cloneable} object.
 *
 * @author James Carman
 * @since 1.0
 */
public class CloningProvider implements ObjectProvider
{
    private final Cloneable cloneable;
    private Method cloneMethod;

    public CloningProvider( Cloneable cloneable )
    {
        this.cloneable = cloneable;
    }

    private synchronized Method getCloneMethod()
    {
        if( cloneMethod == null )
        {
            try
            {
                cloneMethod = cloneable.getClass().getMethod( "clone", ProxyUtils.EMPTY_ARGUMENT_TYPES );
            }
            catch( NoSuchMethodException e )
            {
                throw new ObjectProviderException(
                        "Class " + cloneable.getClass().getName() + " does not have a public clone() method." );
            }
        }
        return cloneMethod;
    }

    public Object getObject()
    {
        try
        {
            return getCloneMethod().invoke( cloneable, ProxyUtils.EMPTY_ARGUMENTS );
        }
        catch( IllegalAccessException e )
        {
            throw new ObjectProviderException(
                    "Class " + cloneable.getClass().getName() + " does not have a public clone() method.", e );
        }
        catch( InvocationTargetException e )
        {
            throw new ObjectProviderException(
                    "Attempt to clone object of type " + cloneable.getClass().getName() + " threw an exception.", e );
        }
    }

}
