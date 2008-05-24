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

package org.apache.commons.proxy.invoker;

import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ProxyUtils;

import java.lang.reflect.Method;

/**
 * A chain invoker will invoke the method on each object in the chain until one of them
 * returns a non-default value
 *
 * @author James Carman
 * @since 1.1
 */
public class ChainInvoker implements Invoker
{
    private final Object[] targets;

    public ChainInvoker(Object[] targets)
    {
        this.targets = targets;
    }

    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable
    {
        for (int i = 0; i < targets.length; i++)
        {
            Object target = targets[i];
            Object value = method.invoke(target, arguments);
            if (value != null && !value.equals(ProxyUtils.getDefaultValue(method.getReturnType())))
            {
                return value;
            }
        }
        return ProxyUtils.getDefaultValue(method.getReturnType());
    }
}

