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
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.Assert.*;

public class StubInterceptorBuilderTest extends AbstractStubTestCase
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private StubInterceptorBuilder builder;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Before
    public void initialize()
    {
        builder = new StubInterceptorBuilder(proxyFactory);
    }

    @Override
    protected StubInterface createProxy(Trainer<StubInterface> trainer)
    {
        Interceptor interceptor = builder.train(trainer).build();
        return proxyFactory.createInterceptorProxy(
                proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, StubInterface.class),
                interceptor,
                StubInterface.class);
    }

    @Test
    public void testWithNestedAnnotations()
    {
        Interceptor interceptor = builder.train(new Trainer<RetentionWrapper>()
        {
            @Override
            protected void train(RetentionWrapper trainee)
            {

                when(trainee.value()).thenStub(new Trainer<Retention>()
                {
                    @Override
                    protected void train(Retention trainee)
                    {
                        when(trainee.value()).thenReturn(RetentionPolicy.RUNTIME);
                    }
                });
            }
        }).build();
        RetentionWrapper wrapper = proxyFactory.createInterceptorProxy(proxyFactory.createInvokerProxy(NullInvoker.INSTANCE), interceptor, RetentionWrapper.class);
        assertNotNull(wrapper.value());
        assertEquals(RetentionPolicy.RUNTIME, wrapper.value().value());
    }

    @Test
    public void testWithSimpleAnnotations()
    {
        Interceptor interceptor = builder.train(new Trainer<Retention>()
        {
            @Override
            protected void train(Retention trainee)
            {
                when(trainee.value()).thenReturn(RetentionPolicy.RUNTIME);
            }
        }).build();
        Retention wrapper = proxyFactory.createInterceptorProxy(proxyFactory.createInvokerProxy(NullInvoker.INSTANCE), interceptor, Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, wrapper.value());
    }

}
