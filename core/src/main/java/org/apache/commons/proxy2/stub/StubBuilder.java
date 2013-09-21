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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.interceptor.SwitchInterceptor;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.provider.ConstantProvider;

public class StubBuilder<T> implements Builder<T>
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final ProxyFactory proxyFactory;
    private final T target;
    private final SwitchInterceptor switchInterceptor = new SwitchInterceptor();
    private final Set<Class<?>> proxyTypes = new HashSet<Class<?>>();

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public StubBuilder(ProxyFactory proxyFactory, Class<T> type)
    {
        this(proxyFactory, type, NullInvoker.INSTANCE);
    }

    public StubBuilder(ProxyFactory proxyFactory, Class<T> type, Invoker invoker)
    {
        this.proxyFactory = proxyFactory;
        this.target = proxyFactory.createInvokerProxy(invoker, type);
        this.proxyTypes.add(Validate.notNull(type));
    }
    
    public StubBuilder(ProxyFactory proxyFactory, Class<T> type, ObjectProvider<? extends T> provider)
    {
        this.proxyFactory = proxyFactory;
        this.target = proxyFactory.createDelegatorProxy(provider, type);
        this.proxyTypes.add(Validate.notNull(type));
    }

    public StubBuilder(ProxyFactory proxyFactory, Class<T> type, T target)
    {
        this.proxyFactory = proxyFactory;
        this.target = proxyFactory.createDelegatorProxy(new ConstantProvider<T>(target), type);
        this.proxyTypes.add(Validate.notNull(type));
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public T build()
    {
        return proxyFactory.createInterceptorProxy(target, switchInterceptor,
                proxyTypes.toArray(ArrayUtils.EMPTY_CLASS_ARRAY));
    }

    public <O> StubBuilder<T> train(BaseTrainer<?, O> trainer)
    {
        try
        {
            TrainingContext trainingContext = TrainingContext.set(proxyFactory);
            final O trainee = trainingContext.push(trainer.traineeType, switchInterceptor);
            trainer.train(trainee);
            proxyTypes.add(trainer.traineeType);
        }
        finally
        {
            TrainingContext.clear();
        }
        return this;
    }

    public StubBuilder<T> addProxyTypes(Class<?>... proxyTypes)
    {
        Collections.addAll(this.proxyTypes, Validate.noNullElements(proxyTypes));
        return this;
    }
}
