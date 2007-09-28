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

package org.apache.commons.proxy.interceptor;

import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.Interceptor;

/**
 * Decorates another <code>MethodInterceptor</code> by only calling it if the method is accepted by the supplied
 * <code>MethodFilter</code>.
 *
 * @author James Carman
 * @since 1.0
 */
public class FilteredInterceptor implements Interceptor
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Interceptor inner;
    private final MethodFilter filter;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public FilteredInterceptor( Interceptor inner, MethodFilter filter )
    {
        this.inner = inner;
        this.filter = filter;
    }

//----------------------------------------------------------------------------------------------------------------------
// MethodInterceptor Implementation
//----------------------------------------------------------------------------------------------------------------------

    public Object intercept( Invocation invocation ) throws Throwable
    {
        if( filter.accepts( invocation.getMethod() ) )
        {
            return inner.intercept( invocation );
        }
        else
        {
            return invocation.proceed();
        }
    }
}

