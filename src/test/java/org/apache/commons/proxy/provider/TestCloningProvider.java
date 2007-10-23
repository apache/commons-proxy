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

package org.apache.commons.proxy.provider;

import junit.framework.TestCase;
import org.apache.commons.proxy.exception.ObjectProviderException;

import java.util.Date;

public class TestCloningProvider extends TestCase
{
    public void testValidCloneable()
    {
        final Date now = new Date();
        final CloningProvider provider = new CloningProvider( now );
        final Date clone1 = ( Date ) provider.getObject();
        assertEquals( now, clone1 );
        assertNotSame( now, clone1 );
        final Date clone2 = ( Date )provider.getObject();
        assertEquals( now, clone2 );
        assertNotSame( now, clone2 );
        assertNotSame( clone2, clone1 );
    }

    public void testWithPrivateCloneMethod()
    {
        final CloningProvider provider = new CloningProvider( new PrivateCloneable() );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }
    
    public void testWithInvalidCloneable()
    {
        final CloningProvider provider = new CloningProvider( new InvalidCloneable() );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public void testWithExceptionThrown()
    {
        final CloningProvider provider = new CloningProvider( new ExceptionCloneable() );
        try
        {
            provider.getObject();
            fail();
        }
        catch( ObjectProviderException e )
        {
        }
    }

    public static class InvalidCloneable implements Cloneable
    {
    }

    public static class PrivateCloneable implements Cloneable
    {
        protected Object clone()
        {
            return this;
        }
    }
    
    public static class ExceptionCloneable implements Cloneable
    {
        public Object clone()
        {
            throw new RuntimeException( "No clone for you!" );
        }
    }
}