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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.proxy2.provider.BeanProvider;
import org.junit.Test;

public class StubBuilderTest extends AbstractStubTestCase
{

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------


    @Override
    protected StubInterface createProxy(Trainer<StubInterface> trainer)
    {
        return new StubBuilder<StubInterface>(proxyFactory, StubInterface.class).train(trainer).build();
    }

    @Test
    public void testWithConcreteTarget()
    {
        StubBuilder<StubInterface> builder = new StubBuilder<StubInterface>(proxyFactory, StubInterface.class, new SimpleStub());
        builder.train(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one("Foo")).thenReturn("Bar");
            }
        });
        StubInterface stub = builder.build();
        assertEquals("Bar", stub.one("Foo"));
    }

    @Test
    public void testWithNoTargetAndNoInterceptors()
    {
        StubBuilder<StubInterface> builder = new StubBuilder<StubInterface>(proxyFactory, StubInterface.class);
        StubInterface stub = builder.build();
        assertNull(stub.one("Whatever"));
    }

    @Test
    public void testWithNoTargetWithInterceptor()
    {
        StubBuilder<StubInterface> builder = new StubBuilder<StubInterface>(proxyFactory, StubInterface.class);
        builder.train(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one("Foo")).thenReturn("Bar");
            }
        });
        StubInterface stub = builder.build();
        assertEquals("Bar", stub.one("Foo"));
    }

    @Test
    public void testWithObjectProviderTarget()
    {
        StubBuilder<StubInterface> builder = new StubBuilder<StubInterface>(proxyFactory, StubInterface.class, new BeanProvider<StubInterface>(SimpleStub.class));
        builder.train(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one("Foo")).thenReturn("Bar");
            }
        });
        StubInterface stub = builder.build();
        assertEquals("Bar", stub.one("Foo"));
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class SimpleStub implements StubInterface
    {
        @Override
        public String one(String value)
        {
            return value;
        }

        @Override
        public String three(String arg1, String arg2)
        {
            return arg1 + arg2;
        }

        @Override
        public String two(String value)
        {
            return StringUtils.repeat(value, 2);
        }

        @Override
        public byte[] byteArray()
        {
            return new byte[]{1, 2, 3};
        }

        @Override
        public char[] charArray()
        {
            return new char[]{'1', '2', '3'};
        }

        @Override
        public short[] shortArray()
        {
            return new short[]{1, 2, 3};
        }

        @Override
        public int[] intArray()
        {
            return new int[]{1, 2, 3};
        }

        @Override
        public long[] longArray()
        {
            return new long[]{1, 2, 3};
        }

        @Override
        public float[] floatArray()
        {
            return new float[]{1.0f, 2.0f, 3.0f};
        }

        @Override
        public double[] doubleArray()
        {
            return new double[]{1.0, 2.0, 3.0};
        }

        @Override
        public boolean[] booleanArray()
        {
            return new boolean[]{true, false, true};
        }

        @Override
        public String[] stringArray()
        {
            return new String[]{"One", "Two", "Three"};
        }

        @Override
        public String arrayParameter(String... strings)
        {
            return StringUtils.join(strings, ", ");
        }

        @Override
        public void voidMethod(String arg)
        {

        }

        @Override
        public StubInterface stub()
        {
            return null;
        }
    }
}
