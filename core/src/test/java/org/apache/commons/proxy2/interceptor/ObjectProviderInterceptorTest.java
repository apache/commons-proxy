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

package org.apache.commons.proxy2.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.proxy2.provider.ObjectProviderUtils;
import org.apache.commons.proxy2.util.AbstractTestCase;
import org.junit.Test;

public class ObjectProviderInterceptorTest extends AbstractTestCase
{

    //----------------------------------------------------------------------------------------------------------------------
    // Other Methods
    //----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testIntercept() throws Throwable
    {
        ObjectProviderInterceptor interceptor = new ObjectProviderInterceptor(ObjectProviderUtils.constant("Hello!"));
        assertEquals("Hello!", interceptor.intercept(null));
    }

    @Test
    public void testWithNullProvider()
    {
        assertThrows(NullPointerException.class, () -> new ObjectProviderInterceptor(null));
    }

}
