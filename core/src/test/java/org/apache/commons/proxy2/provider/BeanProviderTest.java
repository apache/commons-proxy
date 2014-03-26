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

package org.apache.commons.proxy2.provider;

import org.apache.commons.proxy2.exception.ObjectProviderException;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.junit.Test;

public class BeanProviderTest extends AbstractTestCase
{
    //**********************************************************************************************************************
    // Other Methods
    //**********************************************************************************************************************

    @Test(expected = ObjectProviderException.class)
    public void testAbstractBeanClass()
    {
        final BeanProvider<Number> p = new BeanProvider<Number>(Number.class);
        p.getObject();
    }

    @Test(expected = ObjectProviderException.class)
    public void testNonAccessibleConstructor()
    {
        new BeanProvider<MyBean>(MyBean.class).getObject();
    }

    @Test
    public void testSerialization()
    {
        assertSerializable(new BeanProvider<MyBean>(MyBean.class));
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullBeanClass()
    {
        final BeanProvider<Object> p = new BeanProvider<Object>(null);
        p.getObject();
    }

    //**********************************************************************************************************************
    // Inner Classes
    //**********************************************************************************************************************

    public static class MyBean
    {
        private MyBean()
        {

        }
    }
}