/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.proxy.interceptor.filter;

import junit.framework.TestCase;

import java.util.Date;

/**
 * @author James Carman
 * @version 1.0
 */
public class TestPatternMethodFilter extends TestCase
{
    public void testAccepts() throws Exception
    {
        final PatternMethodFilter filter = new PatternMethodFilter( "set\\w+|get\\w+" );
        assertTrue( filter.accepts( Date.class.getMethod( "getSeconds", new Class[] {} ) ) );
        assertTrue( filter.accepts( Date.class.getMethod( "getMinutes",  new Class[] {} ) ) );
        assertTrue( filter.accepts( Date.class.getMethod( "setSeconds",  new Class[] { Integer.TYPE } ) ) );
        assertTrue( filter.accepts( Date.class.getMethod( "setMinutes",  new Class[] { Integer.TYPE } ) ) );
        assertFalse( filter.accepts( Date.class.getMethod( "toString",  new Class[] {} ) ) );
        assertFalse( filter.accepts( Date.class.getMethod( "hashCode",  new Class[] {} ) ) );

    }
}
