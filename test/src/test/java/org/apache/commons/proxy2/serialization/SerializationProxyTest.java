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
package org.apache.commons.proxy2.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.interceptor.InterceptorUtils;
import org.apache.commons.proxy2.interceptor.SwitchInterceptor;
import org.apache.commons.proxy2.interceptor.matcher.invocation.DeclaredByMatcher;
import org.apache.commons.proxy2.stub.AbstractProxyFactoryAgnosticTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SerializationProxyTest extends AbstractProxyFactoryAgnosticTest
{
    public static class NonSerializableStringWrapper
    {
        private final String value;

        NonSerializableStringWrapper(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    public interface Provider
    {
        NonSerializableStringWrapper getObject();
    }

    private static final ThreadLocal<ProxyFactory> PROXY_FACTORY = new ThreadLocal<ProxyFactory>();

    private static SwitchInterceptor implementProvider(String value)
    {
        return new SwitchInterceptor().when(new DeclaredByMatcher(Provider.class)).then(
                InterceptorUtils.constant(new NonSerializableStringWrapper(value)));
    }

    private static Provider serializableProvider(final String value)
    {
        return PROXY_FACTORY.get().createInterceptorProxy(
                null,
                implementProvider(value).when(new DeclaredByMatcher(WriteReplace.class)).then(
                        InterceptorUtils.constant(new ReadResolve()
                        {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public Object readResolve()
                            {
                                return serializableProvider(value);
                            }
                        })), Provider.class, WriteReplace.class);
    }

    @Before
    public void captureProxyFactory()
    {
        PROXY_FACTORY.set(proxyFactory);
    }

    @After
    public void clearProxyFactory()
    {
        PROXY_FACTORY.remove();
    }

    @Test(expected = SerializationException.class)
    public void testNaive()
    {
        final Provider proxy = proxyFactory.createInterceptorProxy(null, implementProvider("foo"), Provider.class,
                Serializable.class);
        assertEquals("foo", proxy.getObject().getValue());
        assertTrue(Serializable.class.isInstance(proxy));
        SerializationUtils.roundtrip((Serializable) proxy);
    }

    @Test
    public void testSerializationProxy()
    {
        final Provider proxy = serializableProvider("foo");
        assertEquals("foo", proxy.getObject().getValue());
        assertTrue(Serializable.class.isInstance(proxy));
        final Provider proxy2 = (Provider) SerializationUtils.roundtrip((Serializable) proxy);
        assertEquals("foo", proxy2.getObject().getValue());
    }
}
