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

package org.apache.commons.proxy.invoker;

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.provider.ConstantProvider;
import org.apache.commons.proxy.util.AbstractTestCase;

import java.io.Serializable;

/**
 *
 */
public class TestDuckTypingInvoker extends AbstractTestCase
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    public void testExactSignatureMatch()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new LegacyDuck());
        final DuckTypingInvoker invoker = new DuckTypingInvoker(targetProvider);
        final Duck duck = ( Duck ) new ProxyFactory().createInvokerProxy(invoker, new Class[] {Duck.class});
        assertEquals("Quack!", duck.sayQuack());
    }

    public void testMismatchingParameterType()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new LegacyDuck());
        final DuckTypingInvoker invoker = new DuckTypingInvoker(targetProvider);
        final ParameterizedDuck parameterizedDuck = ( ParameterizedDuck ) new ProxyFactory()
                .createInvokerProxy(invoker, new Class[] {ParameterizedDuck.class});
        try
        {
            parameterizedDuck.sayQuack("Elmer");
            fail("No matching method should be found.");
        }
        catch( UnsupportedOperationException e )
        {
            // Do nothing, expected behavior!
        }
    }

    public void testMismatchingReturnType()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new LegacyDuck());
        final DuckTypingInvoker invoker = new DuckTypingInvoker(targetProvider);
        final VoidReturnDuck voidDuck = ( VoidReturnDuck ) new ProxyFactory().createInvokerProxy(invoker, new Class[] {
                VoidReturnDuck.class});
        try
        {
            voidDuck.sayQuack();
            fail("No matching method should be found.");
        }
        catch( UnsupportedOperationException e )
        {
            // Do nothing, expected behavior!
        }
    }

    public void testNoMatchingMethod()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new LegacyDuck());
        final DuckTypingInvoker invoker = new DuckTypingInvoker(targetProvider);
        final Goose goose = ( Goose ) new ProxyFactory().createInvokerProxy(invoker, new Class[] {Goose.class});
        try
        {
            goose.sayHonk();
            fail("No matching method should be found.");
        }
        catch( UnsupportedOperationException e )
        {
            // Do nothing, expected behavior!
        }
    }

    public void testSerialization()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new LegacyDuck());
        final DuckTypingInvoker invoker = new DuckTypingInvoker(targetProvider);
        assertSerializable(invoker);
    }

    public void testTargetHasCompatibleReturnType()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new LegacyDuck());
        final DuckTypingInvoker invoker = new DuckTypingInvoker(targetProvider);
        final SerializableDuck duck = ( SerializableDuck ) new ProxyFactory().createInvokerProxy(invoker, new Class[] {
                SerializableDuck.class});
        assertEquals("Quack!", duck.sayQuack());
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    public interface Duck
    {
        public String sayQuack();
    }

    public interface Goose
    {
        public void sayHonk();
    }

    public static class LegacyDuck implements Serializable
    {
        public String sayQuack()
        {
            return "Quack!";
        }
    }

    public interface ParameterizedDuck
    {
        public String sayQuack( String recipient );
    }

    public interface SerializableDuck
    {
        public Serializable sayQuack();
    }

    public interface VoidReturnDuck
    {
        public void sayQuack();
    }
}
