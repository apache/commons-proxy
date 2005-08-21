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
package org.apache.commons.proxy.provider;

import org.apache.commons.proxy.ObjectProvider;

/**
 * @author James Carman
 * @version 1.0
 */
public class ProviderUtils
{
    public static <T> ObjectProvider<T> constantProvider( T value )
    {
        return new ConstantProvider<T>( value );
    }

    public static <T> ObjectProvider<T> beanProvider( Class<T> beanClass )
    {
        return new BeanProvider<T>( beanClass );
    }

    public static <T> ObjectProvider<T> singletonProvider( ObjectProvider<T> inner )
    {
        return new SingletonProvider<T>( inner );
    }

    public static <T> ObjectProvider<T> synchronizedProvider( ObjectProvider<T> inner )
    {
        return new SynchronizedProvider<T>( inner );
    }
}
