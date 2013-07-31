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

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.interceptor.SwitchInterceptor;

import java.lang.reflect.Method;

public class StubInterceptorBuilder
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final ProxyFactory proxyFactory;
    private final SwitchInterceptor interceptor = new SwitchInterceptor();

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public StubInterceptorBuilder(ProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public Interceptor build()
    {
        return interceptor;
    }

    public <T> StubInterceptorBuilder trainFor(Class<T> type, Trainer<T> trainer)
    {
        try
        {
            TrainingContext.set(interceptor);
            T stub = proxyFactory.createInvokerProxy(new TrainingContextInvoker(), type);
            trainer.train(stub);
        }
        finally
        {
            TrainingContext.clear();
        }
        return this;
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static final class TrainingContextInvoker implements Invoker
    {
        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable
        {
            TrainingContext.getTrainingContext().stubMethodInvoked(method, arguments);
            return ProxyUtils.nullValue(method.getReturnType());
        }
    }
}
