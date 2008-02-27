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

package org.apache.commons.proxy.interceptor.logging;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An interceptor which logs method invocations using a {@link Logger JDK logger} using the "finer" logging level.
 * 
 * @auothor James Carman
 * @since 1.1
 */
public class JdkLoggingInterceptor extends AbstractLoggingInterceptor
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private final String loggerName;

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public JdkLoggingInterceptor( Class clazz )
    {
        this(clazz.getName());
    }

    public JdkLoggingInterceptor( String loggerName )
    {
        this.loggerName = loggerName;
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private Logger getLogger()
    {
        return Logger.getLogger(loggerName);
    }

    protected void entering( Method method, Object[] args )
    {
        getLogger().entering(method.getDeclaringClass().getName(), method.getName(), args);
    }

    protected void exiting( Method method, Object result )
    {
        getLogger().exiting(method.getDeclaringClass().getName(), method.getName(), result);
    }

    protected boolean isLoggingEnabled()
    {
        return getLogger().isLoggable(Level.FINER);
    }

    protected void logMessage( String message )
    {
        // Do nothing!
    }

    protected void logMessage( String message, Throwable t )
    {
        // Do nothing!
    }

    protected void throwing( Method method, Throwable t )
    {
        getLogger().throwing(method.getDeclaringClass().getName(), method.getName(), t);
    }
}
