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

package org.apache.commons.proxy2.exception;

/**
 * To be used by an {@link org.apache.commons.proxy2.Invoker} when they encounter an error.
 *
 * @author James Carman
 * @since 1.0
 */
public class InvokerException extends RuntimeException
{
    /** Serialization version */
    private static final long serialVersionUID = -1L;

  //**********************************************************************************************************************
 // Constructors
 //**********************************************************************************************************************

    /**
     * Create a new InvokerException instance.
     */
    public InvokerException()
    {
    }

    /**
     * Create a new InvokerException instance.
     * @param message
     */
    public InvokerException( String message )
    {
        super(message);
    }

    /**
     * Create a new InvokerException instance.
     * @param cause
     */
    public InvokerException( Throwable cause )
    {
        super(cause);
    }

    /**
     * Create a new InvokerException instance.
     * @param message
     * @param cause
     */
    public InvokerException( String message, Throwable cause )
    {
        super(message, cause);
    }
}
