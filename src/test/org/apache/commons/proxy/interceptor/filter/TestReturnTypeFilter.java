/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.proxy.interceptor.filter;
import junit.framework.*;
import org.apache.commons.proxy.interceptor.filter.ReturnTypeFilter;

public class TestReturnTypeFilter extends TestCase
{
    public void testAcceptsMethod() throws Exception
    {
        final ReturnTypeFilter filter = new ReturnTypeFilter( String.class, Integer.TYPE );
        assertTrue( filter.accepts( Object.class.getMethod( "toString" ) ) );
        assertTrue( filter.accepts( Object.class.getMethod( "hashCode" ) ) );
        assertFalse( filter.accepts( Object.class.getMethod( "equals", Object.class ) ) );
    }


}