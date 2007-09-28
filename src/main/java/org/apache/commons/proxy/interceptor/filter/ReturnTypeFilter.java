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
package org.apache.commons.proxy.interceptor.filter;

import org.apache.commons.proxy.interceptor.MethodFilter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Filters methods based on their return type.
 * @author James Carman
 * @since 1.0
 */
public class ReturnTypeFilter implements MethodFilter
{
    private final Set validReturnTypes = new HashSet();

    public ReturnTypeFilter( Class[] validReturnTypes )
    {
        this( Arrays.asList( validReturnTypes ) );
    }

    public ReturnTypeFilter( Collection validReturnTypes )
    {
        this.validReturnTypes.addAll( validReturnTypes );
    }

    public boolean accepts( Method method )
    {
        return validReturnTypes.contains( method.getReturnType() );
    }
}
