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
package org.apache.commons.proxy.factory.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author James Carman
 * @version 1.0
 */
public class MethodSignature
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final String name;
    private final List<Class> parameterTypes;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public MethodSignature( Method method )
    {
        this.name = method.getName();
        this.parameterTypes = Arrays.<Class>asList( method.getParameterTypes() );
    }

//----------------------------------------------------------------------------------------------------------------------
// Canonical Methods
//----------------------------------------------------------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        final MethodSignature that = ( MethodSignature ) o;
        if( !name.equals( that.name ) )
        {
            return false;
        }
        if( !parameterTypes.equals( that.parameterTypes ) )
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result;
        result = name.hashCode();
        result = 29 * result + parameterTypes.hashCode();
        return result;
    }
}

