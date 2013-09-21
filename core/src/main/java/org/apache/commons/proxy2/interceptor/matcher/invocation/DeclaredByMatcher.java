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

package org.apache.commons.proxy2.interceptor.matcher.invocation;

import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.interceptor.matcher.InvocationMatcher;

/**
 * InvocationMatcher based on declaring class of the method invoked.
 */
public class DeclaredByMatcher implements InvocationMatcher
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final boolean exactMatch;
    private final Class<?> declaredByType;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Equivalent to {@link #DeclaredByMatcher(Class, boolean)}{@code (declaredByType, false)}
     * @param declaredByType
     */
    public DeclaredByMatcher(Class<?> declaredByType)
    {
        this(declaredByType, false);
    }

    /**
     * Create a {@link DeclaredByMatcher} instance.
     * 
     * @param declaredByType
     *            type by which method must be declared
     * @param exactMatch
     *            if {@code false}, {@code declaredByType} may be a subclass of
     *            the actual declaring class of the invocation method.
     */
    public DeclaredByMatcher(Class<?> declaredByType, boolean exactMatch)
    {
        this.declaredByType = declaredByType;
        this.exactMatch = exactMatch;
    }

//----------------------------------------------------------------------------------------------------------------------
// InvocationMatcher Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean matches(Invocation invocation)
    {
        final Class<?> owner = invocation.getMethod().getDeclaringClass();
        return exactMatch ?
                declaredByType.equals(owner) :
                owner.isAssignableFrom(declaredByType);
    }
}
