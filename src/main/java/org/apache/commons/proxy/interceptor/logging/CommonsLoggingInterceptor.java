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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @auothor James Carman
 * @since 1.1
 */
public class CommonsLoggingInterceptor extends AbstractLoggingInterceptor
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private final String logName;

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public CommonsLoggingInterceptor( String logName )
    {
        this.logName = logName;
    }

    public CommonsLoggingInterceptor(Class clazz)
    {
        this(clazz.getName());
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private Log getLog()
    {
        return LogFactory.getLog(logName);
    }

    protected boolean isLoggingEnabled()
    {
        return getLog().isTraceEnabled();
    }

    protected void logMessage( String message )
    {
        getLog().trace(message);
    }

    protected void logMessage( String message, Throwable t )
    {
        getLog().trace(message, t);
    }
}
