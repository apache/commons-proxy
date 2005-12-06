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
package org.apache.commons.proxy.exception;

import junit.framework.TestCase;

/**
 * @author James Carman
 * @since 1.0
 */
public abstract class AbstractExceptionClassTestCase extends TestCase
{
    private final Class exceptionClass;

    public AbstractExceptionClassTestCase( Class exceptionClass )
    {
        this.exceptionClass = exceptionClass;
    }

    public void testNoArgConstructor() throws Exception
    {
        Exception e = ( Exception )exceptionClass.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
        assertNull( e.getMessage() );
        assertNull( e.getCause() );
    }

    public void testMessageOnlyConstructor() throws Exception
    {
        final String message = "message";
        Exception e = ( Exception )exceptionClass.getConstructor( new Class[] { String.class } ).newInstance( new Object[] { message } );
        assertEquals( message, e.getMessage() );
        assertNull( e.getCause() );
    }

    public void testCauseOnlyConstructor() throws Exception
    {
        final Exception cause = new Exception();
        Exception e = ( Exception )exceptionClass.getConstructor( new Class[] { Throwable.class } ).newInstance( new Object[] { cause } );
        assertEquals( cause.toString(), e.getMessage() );
        assertEquals( cause, e.getCause() );
    }

    public void testMessageAndCauseConstructor() throws Exception
    {
        final Exception cause = new Exception();
        final String message = "message";
        Exception e = ( Exception )exceptionClass.getConstructor( new Class[] { String.class, Throwable.class } ).newInstance( new Object[] { message, cause } );
        assertEquals( message, e.getMessage() );
        assertEquals( cause, e.getCause() );
    }
}
