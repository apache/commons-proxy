/* $Id:XmlRpcInvoker.java 325897 2005-10-17 10:11:52 -0400 (Mon, 17 Oct 2005) jcarman $
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
package org.apache.commons.proxy.invoker;

import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.exception.InvokerException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;

import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Uses <a href="http://ws.apache.org/xmlrpc/">Apache XML-RPC</a> to invoke methods on an XML-RPC service.
 *
 * <p>
 * <b>Dependencies</b>:
 * <ul>
 *   <li>Apache XML-RPC version 2.0 or greater</li>
 * </ul>
 * </p>
 * @author James Carman
 * @since 1.0
 */
public class XmlRpcInvoker implements Invoker
{
    private final XmlRpcHandler handler;
    private final String handlerName;

    public XmlRpcInvoker( XmlRpcHandler handler, String handlerName )
    {
        this.handler = handler;
        this.handlerName = handlerName;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        final Object returnValue = handler.execute( handlerName + "." + method.getName(), toArgumentVector( args ) );
        if( returnValue instanceof XmlRpcException )
        {
            throw new InvokerException( "Unable to execute XML-RPC call.", ( XmlRpcException )returnValue );

        }
        return returnValue;
    }

    private Vector toArgumentVector( Object[] args )
    {
        final Vector v = new Vector();
        for( int i = 0; i < args.length; i++ )
        {
            Object arg = args[i];
            v.addElement( arg );
        }
        return v;
    }
}
