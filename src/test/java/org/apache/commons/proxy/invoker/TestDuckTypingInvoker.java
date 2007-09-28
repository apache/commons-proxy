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

import junit.framework.TestCase;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.provider.ConstantProvider;

import java.io.Externalizable;
import java.io.IOException;
import java.util.Comparator;

/**
 *
 */
public class TestDuckTypingInvoker extends TestCase
{
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void testExactSignatureMatch()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new DuckComparator());
        final DuckTypingInvoker invoker = new DuckTypingInvoker( targetProvider );
        final Comparator c = ( Comparator )new ProxyFactory().createInvokerProxy( invoker, new Class[] { Comparator.class } );
        assertEquals( 12345, c.compare( null, null ) );
    }

    public void testNoMatchingMethod()
    {
        final ObjectProvider targetProvider = new ConstantProvider(new DuckComparator());
        final DuckTypingInvoker invoker = new DuckTypingInvoker( targetProvider );
        final Externalizable externalizable = ( Externalizable )new ProxyFactory().createInvokerProxy( invoker, new Class[] {
                Externalizable.class } );
        try
        {
            externalizable.writeExternal( null );
            fail("No matching method should be found.");
        }
        catch(UnsupportedOperationException e )
        {
            // Do nothing, expected behavior!
        }
        catch ( IOException e )
        {
            fail("No IOException should be thrown here.");
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    public static class DuckComparator
    {
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

        public int compare( final Object o1, final Object o2 )
        {
            return 12345;
        }
    }
}
