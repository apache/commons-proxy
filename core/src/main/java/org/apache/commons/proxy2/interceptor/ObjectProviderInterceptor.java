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

package org.apache.commons.proxy2.interceptor;

import org.apache.commons.lang3.Validate;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.ObjectProvider;

/**
 * A {@link ObjectProviderInterceptor} merely returns the value returned from
 * {@link org.apache.commons.proxy2.ObjectProvider#getObject()}.
 */
public class ObjectProviderInterceptor implements Interceptor
{
    private static final long serialVersionUID = 1L;

    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    private final ObjectProvider<?> provider;

    //******************************************************************************************************************
    // Constructors
    //******************************************************************************************************************

    public ObjectProviderInterceptor(ObjectProvider<?> provider)
    {
        this.provider = Validate.notNull(provider, "Provider cannot be null.");
    }

    //******************************************************************************************************************
    // Interceptor Implementation
    //******************************************************************************************************************

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        return provider.getObject();
    }
}
