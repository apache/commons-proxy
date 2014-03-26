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

package org.apache.commons.proxy2;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * An invoker is responsible for handling a method invocation.
 * 
 * @author James Carman
 * @since 1.0
 */
public interface Invoker extends Serializable
{
    //******************************************************************************************************************
    // Other Methods
    //******************************************************************************************************************

    /**
     * "Invokes" the method. Implementation should throw a {@link org.apache.commons.proxy2.exception.InvokerException}
     * if problems arise while trying to invoke the method.
     * 
     * @param proxy
     *            the proxy2 object
     * @param method
     *            the method being invoked
     * @param arguments
     *            the arguments
     * @return the return value
     * @throws Throwable
     *             thrown by the implementation
     */
    Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable;
}
