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
package org.apache.commons.proxy.provider;

import junit.framework.TestCase;
import org.apache.commons.proxy.exception.ObjectProviderException;
import org.apache.commons.proxy.util.Echo;

public class TestHessianProvider extends TestCase
{
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void testWithMalformedUrl()
    {
        try
        {
            final HessianProvider p = new HessianProvider( Echo.class, "a malformed URL" );
            p.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }
}