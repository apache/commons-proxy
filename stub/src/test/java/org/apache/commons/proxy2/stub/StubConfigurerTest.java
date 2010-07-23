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

import org.apache.commons.proxy2.stub.StubConfigurer;
import org.junit.Test;

/**
 * Test StubConfigurer stubType calculation.
 */
public class StubConfigurerTest {
    public interface Foo {
    }

    public class FooConfigurer extends StubConfigurer<Foo> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void configure(Foo stub) {
        }
    }

    public class GenericConfigurer<T> extends StubConfigurer<T> {
        public GenericConfigurer() {
        }

        public GenericConfigurer(Class<T> stubType) {
            super(stubType);
        }

        protected void configure(T stub) {
        }
    }

    public class GenericFooConfigurer extends GenericConfigurer<Foo> {
    }

    @Test
    public void testAnonymousInner() {
        assertEquals(Foo.class, new StubConfigurer<Foo>() {

            @Override
            protected void configure(Foo stub) {
            }
        }.getStubType());
    }

    @Test
    public void testStubTypeCalculation() {
        assertEquals(Foo.class, new FooConfigurer().getStubType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenericFailure() {
        new GenericConfigurer<Foo>();
    }

    @Test
    public void testGenericWithStubType() {
        assertEquals(Foo.class, new GenericConfigurer<Foo>(Foo.class).getStubType());
    }

    @Test
    public void testDownHierarchy() {
        assertEquals(Foo.class, new GenericFooConfigurer().getStubType());
    }

    @Test
    public void testGenericSubclass() {
        assertEquals(Foo.class, new GenericConfigurer<Foo>() {}.getStubType());
    }
}
