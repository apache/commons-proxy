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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Invoker} implementation which merely returns null for all method invocations.  This class is
 * useful for scenarios where the "null object" design pattern is needed.
 *
 * @author James Carman
 * @since 1.0
 */
public class NullInvoker implements Invoker, Serializable
{   
//**********************************************************************************************************************
// Invoker Implementation
//**********************************************************************************************************************

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        final Class returnType = method.getReturnType();
        return ProxyUtils.nullValue(returnType);
    }
}

