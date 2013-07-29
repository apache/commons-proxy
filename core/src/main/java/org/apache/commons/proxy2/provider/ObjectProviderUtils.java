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

package org.apache.commons.proxy2.provider;

import org.apache.commons.proxy2.ObjectProvider;

public final class ObjectProviderUtils
{
//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    public static <T> ObjectProvider<T> bean(Class<T> beanClass)
    {
        return new BeanProvider<T>(beanClass);
    }

    public static <T extends Cloneable> ObjectProvider<T> cloning(T prototype)
    {
        return new CloningProvider<T>(prototype);
    }

    public static <T> ObjectProvider<T> constant(T value)
    {
        return new ConstantProvider<T>(value);
    }

    public static <T> ObjectProvider<T> nullValue()
    {
        return new NullProvider<T>();
    }

    public static <T> ObjectProvider<T> singleton(ObjectProvider<T> inner)
    {
        return new SingletonProvider<T>(inner);
    }

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    private ObjectProviderUtils()
    {
        
    }
}
