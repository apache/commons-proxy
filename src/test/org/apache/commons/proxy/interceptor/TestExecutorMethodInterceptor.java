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
package org.apache.commons.proxy.interceptor;

import junit.framework.TestCase;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.EchoImpl;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.CountDown;

public class TestExecutorMethodInterceptor extends TestCase
{
    public void testVoidMethod() throws Exception
    {
        final ExecutedEcho impl = new ExecutedEcho();
        final OneShotExecutor executor = new OneShotExecutor();
        final Echo proxy = ( Echo ) new CglibProxyFactory()
                .createInterceptorProxy( impl, new ExecutorMethodInterceptor( executor ), new Class[] { Echo.class } );
        proxy.echo();
        executor.getLatch().acquire();
        assertEquals( executor.getThread(), impl.getExecutionThread() );
    }

    public void testNonVoidMethod() throws Exception
    {
        final ExecutedEcho impl = new ExecutedEcho();
        final OneShotExecutor executor = new OneShotExecutor();
        final Echo proxy = ( Echo ) new CglibProxyFactory()
                .createInterceptorProxy( impl, new ExecutorMethodInterceptor( executor ), new Class[] { Echo.class } );
        try
        {
            proxy.echoBack( "hello" );
            fail();
        }
        catch( IllegalArgumentException e )
        {
        }
    }

    public static class ExecutedEcho extends EchoImpl
    {
        private Thread executionThread;

        public void echo()
        {
            executionThread = Thread.currentThread();
        }

        public Thread getExecutionThread()
        {
            return executionThread;
        }
    }

    private static class OneShotExecutor implements Executor
    {
        private Thread thread;
        private CountDown latch = new CountDown( 1 );

        public void execute( final Runnable command )
        {
            thread = new Thread( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        command.run();
                    }
                    finally
                    {
                        latch.release();
                    }
                }
            } );
            thread.start();
        }

        public Thread getThread()
        {
            return thread;
        }

        public CountDown getLatch()
        {
            return latch;
        }
    }
}