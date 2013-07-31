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

package org.apache.commons.proxy2.stub;

import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.provider.ConstantProvider;

public class StubBuilder<T>
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final ProxyFactory proxyFactory;
    private final T target;
    private final StubInterceptorBuilder interceptorBuilder;
    private final Class<T> type;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public StubBuilder(ProxyFactory proxyFactory, Class<T> type)
    {
        this.proxyFactory = proxyFactory;
        this.type = type;
        this.target = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, type);
        this.interceptorBuilder = new StubInterceptorBuilder(proxyFactory);
    }

    public StubBuilder(ProxyFactory proxyFactory, Class<T> type, ObjectProvider<? extends T> provider)
    {
        this.proxyFactory = proxyFactory;
        this.type = type;
        this.target = proxyFactory.createDelegatorProxy(provider, type);
        this.interceptorBuilder = new StubInterceptorBuilder(proxyFactory);
    }

    public StubBuilder(ProxyFactory proxyFactory, Class<T> type, T target)
    {
        this.proxyFactory = proxyFactory;
        this.type = type;
        this.target = proxyFactory.createDelegatorProxy(new ConstantProvider<T>(target), type);
        this.interceptorBuilder = new StubInterceptorBuilder(proxyFactory);
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public T build()
    {
        return proxyFactory.createInterceptorProxy(target, interceptorBuilder.build(), type);
    }

    public StubBuilder<T> train(Trainer<T> trainer)
    {
        interceptorBuilder.trainFor(type, trainer);
        return this;
    }
}
