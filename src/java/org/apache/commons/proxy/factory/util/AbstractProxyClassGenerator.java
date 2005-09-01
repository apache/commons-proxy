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
package org.apache.commons.proxy.factory.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A useful superclass for {@link ProxyClassGenerator} implementations.
 *
 * @author James Carman
 * @version 1.0
 */
public abstract class AbstractProxyClassGenerator implements ProxyClassGenerator
{
    /**
     * Returns all methods that a proxy class must implement from the
     * proxy interfaces.  This method makes sure there are no method signature clashes.
     * For methods with the same signature (name and parameter types), the one encountered
     * first will be returned in the result.
     * @param proxyInterfaces the interfaces the proxy class must implement
     * @return all methods that the proxy class must implement
     */
    public static Method[] getImplementationMethods( Class... proxyInterfaces )
    {
        final Set<MethodSignature> signatures = new HashSet<MethodSignature>();
        final List<Method> resultingMethods = new LinkedList<Method>();
        for( int i = 0; i < proxyInterfaces.length; i++ )
        {
            Class proxyInterface = proxyInterfaces[i];
            final Method[] methods = proxyInterface.getMethods();
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
}
