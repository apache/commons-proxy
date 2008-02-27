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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An interceptor which logs method invocations using <a href="http://www.slf4j.org/">SLF4J</a> using the
 * trace logging level.
 * 
 * @auothor James Carman
 * @since 1.1
 */
public class Slf4jInterceptor extends AbstractLoggingInterceptor
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private final String loggerName;

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public Slf4jInterceptor( Class clazz )
    {
        this(clazz.getName());
    }

    public Slf4jInterceptor( String loggerName )
    {
        this.loggerName = loggerName;
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    private Logger getLogger()
    {
        return LoggerFactory.getLogger(loggerName);
    }

    protected boolean isLoggingEnabled()
    {
        return getLogger().isTraceEnabled();
    }

    protected void logMessage( String message )
    {
        getLogger().debug(message);
    }

    protected void logMessage( String message, Throwable t )
    {
        getLogger().debug(message, t);
    }
}
