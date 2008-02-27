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

import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * An interceptor which makes a serialized copy of all parameters and return values.  This
 * is useful when testing remote services to ensure that all parameter/return types
 * are in fact serializable/deserializable.
 *
 * @since 1.0
 */
public class SerializingInterceptor implements Interceptor, Serializable
{
//**********************************************************************************************************************
// Interceptor Implementation
//**********************************************************************************************************************

    public Object intercept( Invocation invocation ) throws Throwable
    {
        Object[] arguments = invocation.getArguments();
        for( int i = 0; i < arguments.length; i++ )
        {
            arguments[i] = serializedCopy(arguments[i]);
        }
        return serializedCopy(invocation.proceed());
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private Object serializedCopy( Object original )
    {
        try
        {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(original);
            oout.close();
            bout.close();
            final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            final ObjectInputStream oin = new ObjectInputStream(bin);
            final Object copy = oin.readObject();
            oin.close();
            bin.close();
            return copy;
        }
        catch( IOException e )
        {
            throw new RuntimeException("Unable to make serialized copy of " +
                    original.getClass().getName() + " object.", e);
        }
        catch( ClassNotFoundException e )
        {
            throw new RuntimeException("Unable to make serialized copy of " +
                    original.getClass().getName() + " object.", e);
        }
    }
}
