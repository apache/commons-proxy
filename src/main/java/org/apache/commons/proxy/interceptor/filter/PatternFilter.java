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

package org.apache.commons.proxy.interceptor.filter;

import org.apache.commons.proxy.interceptor.MethodFilter;

import java.lang.reflect.Method;

/**
 * A method filter implementation that returns true if the method's name matches a supplied regular expression (JDK
 * regex) pattern string.
 *
 * @author James Carman
 * @since 1.0
 */
public class PatternFilter implements MethodFilter
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    public static String GETTER_SETTER_PATTERN = "get\\w+|set\\w+";
    private final String pattern;

//**********************************************************************************************************************
// Static Methods
//**********************************************************************************************************************

    /**
     * Returns a {@link MethodFilter} which accepts only "getters" and "setters."
     *
     * @return a {@link MethodFilter} which accepts only "getters" and "setters."
     */
    public static MethodFilter getterSetterFilter()
    {
        return new PatternFilter(GETTER_SETTER_PATTERN);
    }

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public PatternFilter( String pattern )
    {
        this.pattern = pattern;
    }

//**********************************************************************************************************************
// MethodFilter Implementation
//**********************************************************************************************************************

    public boolean accepts( Method method )
    {
        return method.getName().matches(pattern);
    }
}

