/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.proxy.exception;

/**
 * A runtime exception type to be used by {@link org.apache.commons.proxy.ProxyFactory proxy factories} when a
 * problem occurs.
 * 
 * @author James Carman
 * @version 1.0
 */
public class ProxyFactoryException extends RuntimeException
{
    public ProxyFactoryException()
    {
    }

    public ProxyFactoryException( String message )
    {
        super( message );
    }

    public ProxyFactoryException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ProxyFactoryException( Throwable cause )
    {
        super( cause );
    }
}
