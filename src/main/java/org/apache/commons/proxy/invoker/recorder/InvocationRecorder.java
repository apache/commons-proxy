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

package org.apache.commons.proxy.invoker.recorder;

import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.ProxyUtils;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @auothor James Carman
 */
public class InvocationRecorder
{
    private final ProxyFactory proxyFactory;
    private List<RecordedInvocation> recordedInvocations = new LinkedList<RecordedInvocation>();

    public InvocationRecorder( ProxyFactory proxyFactory )
    {
        this.proxyFactory = proxyFactory;
    }

    public List<RecordedInvocation> getRecordedInvocations()
    {
        return recordedInvocations;
    }

    public <T> T proxy( Class<T> type )
    {
        if(proxyFactory.canProxy(type))
        {
            return proxyFactory.createInvokerProxy(new InvocationRecorderInvoker(), type);
        }
        return ProxyUtils.nullValue(type);
    }

    private class InvocationRecorderInvoker implements Invoker
    {
        public Object invoke( Object o, Method method, Object[] args ) throws Throwable
        {
            recordedInvocations.add(new RecordedInvocation(method, args));
            return proxy(method.getReturnType());
        }
    }

    public void reset()
    {
        recordedInvocations.clear();
    }
}
