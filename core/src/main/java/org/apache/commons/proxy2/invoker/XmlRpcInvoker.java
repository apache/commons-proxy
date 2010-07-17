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

package org.apache.commons.proxy2.invoker;

import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.exception.InvokerException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Vector;

/**
 * Uses <a href="http://ws.apache.org/xmlrpc/">Apache XML-RPC</a> to invoke methods on an XML-RPC service.
 * <p/>
 * <p>
 * <b>Dependencies</b>:
 * <ul>
 * <li>Apache XML-RPC version 2.0 or greater</li>
 * </ul>
 * </p>
 *
 * @author James Carman
 * @since 1.0
 */
public class XmlRpcInvoker implements Invoker
{
    /** Serialization version */
    private static final long serialVersionUID = 1L;

//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private final XmlRpcHandler handler;
    private final String handlerName;

  //**********************************************************************************************************************
 // Constructors
 //**********************************************************************************************************************

    /**
     * Create a new XmlRpcInvoker instance.
     * @param handler
     * @param handlerName
     */
    public XmlRpcInvoker( XmlRpcHandler handler, String handlerName )
    {
        this.handler = handler;
        this.handlerName = handlerName;
    }

  //**********************************************************************************************************************
 // Invoker Implementation
 //**********************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        final Object returnValue = handler.execute(handlerName + "." + method.getName(), new Vector<Object>(Arrays.asList(args)));
        if( returnValue instanceof XmlRpcException )
        {
            throw new InvokerException("Unable to execute XML-RPC call.", ( XmlRpcException ) returnValue);
        }
        return returnValue;
    }

}
