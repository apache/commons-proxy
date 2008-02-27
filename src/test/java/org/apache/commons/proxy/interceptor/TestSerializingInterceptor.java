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

package org.apache.commons.proxy.interceptor;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.util.AbstractTestCase;

import java.io.ByteArrayOutputStream;

public class TestSerializingInterceptor extends AbstractTestCase
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    public void testSerialization()
    {
        assertSerializable(new SerializingInterceptor());
    }

    public void testWithInvalidParameterType()
    {
        try
        {
            final ObjectEchoImpl target = new ObjectEchoImpl();
            ObjectEcho echo =
                    ( ObjectEcho ) new ProxyFactory().createInterceptorProxy(target,
                            new SerializingInterceptor(),
                            new Class[] {ObjectEcho.class});
            final Object originalParameter = new ByteArrayOutputStream();
            echo.echoBack(originalParameter);
            fail("Should not be able to call method with non-serializable parameter type.");
        }
        catch( RuntimeException e )
        {
        }
    }

    public void testWithSerializableParametersAndReturn()
    {
        final ObjectEchoImpl target = new ObjectEchoImpl();
        ObjectEcho echo =
                ( ObjectEcho ) new ProxyFactory().createInterceptorProxy(target,
                        new SerializingInterceptor(),
                        new Class[] {ObjectEcho.class});
        final Object originalParameter = "Hello, World!";
        final Object returnValue = echo.echoBack(originalParameter);
        assertNotSame(originalParameter, target.parameter);
        assertNotSame(originalParameter, returnValue);
        assertNotSame(returnValue, target.parameter);
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    public static interface ObjectEcho
    {
        public Object echoBack( Object object );
    }

    public static class ObjectEchoImpl implements ObjectEcho
    {
        private Object parameter;

        public Object echoBack( Object object )
        {
            this.parameter = object;
            return object;
        }
    }
}
