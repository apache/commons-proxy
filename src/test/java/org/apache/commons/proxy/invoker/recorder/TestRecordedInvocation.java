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

import org.apache.commons.proxy.util.AbstractTestCase;

/**
 * @auothor James Carman
 */
public class TestRecordedInvocation extends AbstractTestCase
{
    public void testToString() throws Exception
    {
        RecordedInvocation invocation = new RecordedInvocation(String.class.getMethod("toString"), new Object[0] );
        assertEquals("java.lang.String.toString()", invocation.toString());

        invocation = new RecordedInvocation(String.class.getMethod("substring", Integer.TYPE), new Object[] { 1 } );
        assertEquals("java.lang.String.substring(1)", invocation.toString());

        invocation = new RecordedInvocation(String.class.getMethod("substring", Integer.TYPE, Integer.TYPE), new Object[] { 1, 2 } );
        assertEquals("java.lang.String.substring(1, 2)", invocation.toString());

        invocation = new RecordedInvocation(String.class.getMethod("equals", Object.class), new Object[] { null } );
        assertEquals("java.lang.String.equals(<null>)", invocation.toString());
    }
}
