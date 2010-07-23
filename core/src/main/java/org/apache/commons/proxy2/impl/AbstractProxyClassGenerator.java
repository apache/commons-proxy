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

package org.apache.commons.proxy2.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A useful superclass for {@link ProxyClassGenerator} implementations.
 *
 * @author James Carman
 * @since 1.0
 */
public abstract class AbstractProxyClassGenerator implements ProxyClassGenerator
{
//**********************************************************************************************************************
// Static Methods
//**********************************************************************************************************************

    /**
     * Returns all methods that a proxy class must implement from the proxy interfaces.  This method makes sure there
     * are no method signature clashes. For methods with the same signature (name and parameter types), the one
     * encountered first will be returned in the result. Final methods are also excluded from the result.
     *
     * @param proxyClasses the interfaces the proxy class must implement
     * @return all methods that the proxy class must implement
     */
    public static Method[] getImplementationMethods( Class<?>[] proxyClasses )
    {
        final Map<MethodSignature, Method> signatureMethodMap = new HashMap<MethodSignature, Method>();
        final Set<MethodSignature> finalizedSignatures = new HashSet<MethodSignature>();
        for( int i = 0; i < proxyClasses.length; i++ )
        {
            Class<?> proxyInterface = proxyClasses[i];
            final Method[] methods = proxyInterface.getMethods();
            for( int j = 0; j < methods.length; j++ )
            {
                final MethodSignature signature = new MethodSignature(methods[j]);
                if( Modifier.isFinal(methods[j].getModifiers()) )
                {
                    finalizedSignatures.add(signature);
                }
                else if( !signatureMethodMap.containsKey(signature) )
                {
                    signatureMethodMap.put(signature, methods[j]);
                }
            }
        }
        final Collection<Method> resultingMethods = signatureMethodMap.values();
        for (MethodSignature signature : finalizedSignatures) {
            resultingMethods.remove(signatureMethodMap.get(signature));
        }
        return resultingMethods.toArray(new Method[resultingMethods.size()]);
    }
}
