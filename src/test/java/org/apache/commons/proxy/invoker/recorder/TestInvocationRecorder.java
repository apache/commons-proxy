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

import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.apache.commons.proxy.util.AbstractTestCase;
import org.apache.commons.proxy.ProxyUtils;

import java.util.List;

/**
 * @auothor James Carman
 */
public class TestInvocationRecorder extends AbstractTestCase
{
    public void testNestedMethodRecording() throws Exception
    {
        InvocationRecorder recorder = new InvocationRecorder(new CglibProxyFactory());
        Person personProxy = recorder.proxy(Person.class);

        assertEquals(null, personProxy.getAddress().getCity());
        List<RecordedInvocation> recordedInvocations = recorder.getRecordedInvocations();
        final RecordedInvocation getAddressInvocation = recordedInvocations.get(0);
        assertEquals(Person.class.getMethod("getAddress"), getAddressInvocation.getInvokedMethod());
        assertEquals(0, getAddressInvocation.getArguments().length);
    }

    public void testNestedGenericMethodRecording() throws Exception
    {
        InvocationRecorder recorder = new InvocationRecorder(new CglibProxyFactory());
        Person personProxy = recorder.proxy(Person.class);
        assertEquals(null, personProxy.getNicknames().get(0));
        List<RecordedInvocation> recordedInvocations = recorder.getRecordedInvocations();

        assertEquals(2, recordedInvocations.size());
        
        RecordedInvocation invocation = recordedInvocations.get(0);
        assertEquals(Person.class.getMethod("getNicknames"), invocation.getInvokedMethod());
        assertEquals(0, invocation.getArguments().length);

        invocation = recordedInvocations.get(1);
        assertEquals(List.class.getMethod("get", int.class), invocation.getInvokedMethod());
        assertEquals(1, invocation.getArguments().length);
        assertEquals(0, invocation.getArguments()[0] );
    }

    public void testProxyNonProxyableType()
    {
        InvocationRecorder recorder = new InvocationRecorder(new CglibProxyFactory());
        assertProxyIsNullValue(recorder, String.class);
        assertProxyIsNullValue(recorder, Long.TYPE);
        assertProxyIsNullValue(recorder, Integer.TYPE);
        assertProxyIsNullValue(recorder, Short.TYPE);
        assertProxyIsNullValue(recorder, Byte.TYPE);

        assertProxyIsNullValue(recorder, Double.TYPE);
        assertProxyIsNullValue(recorder, Float.TYPE);

        assertProxyIsNullValue(recorder, Boolean.TYPE);

        assertProxyIsNullValue(recorder, Character.TYPE);
    }

    private <T> void assertProxyIsNullValue( InvocationRecorder recorder, Class<T> type )
    {
        assertNullValue(recorder.proxy(type), type);
    }

    public <T> void assertNullValue( T value, Class<T> type )
    {
        assertEquals(value, ProxyUtils.nullValue(type));
    }

    public static interface Person
    {
        public Address getAddress();

        public List<String> getNicknames();
    }

    public static interface Address
    {
        public String getCity();
    }

}
