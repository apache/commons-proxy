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

package org.apache.commons.proxy2.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.Validate;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;

/**
 * Delegates a method invocation to the object provided by an
 * {@link ObjectProvider}.
 * 
 * @param <T>
 */
public class DelegatingInvoker<T> implements Invoker
{
    private static final long serialVersionUID = 1L;

    private final ObjectProvider<? extends T> delegateProvider;

    public DelegatingInvoker(ObjectProvider<? extends T> delegateProvider)
    {
        super();
        this.delegateProvider = Validate.notNull(delegateProvider);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable
    {
        try
        {
            return method.invoke(delegateProvider.getObject(), arguments);
        }
        catch (InvocationTargetException e)
        {
            throw e.getTargetException();
        }
    }

}
