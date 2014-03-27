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

package org.apache.commons.proxy2.impl;

/**
 * A proxy class generator generates Class instances for a given combination of interfaces in a given classloader.
 * Typically, in the context of a {@link org.apache.commons.proxy2.ProxyFactory} implementation, an instance will
 * generate proxy class instances for a specific type of proxies (interceptor, invoker, etc.) and will be associated
 * with a corresponding {@link ProxyClassCache}.
 * 
 * @since 1.0
 */
public interface ProxyClassGenerator
{
    //******************************************************************************************************************
    // Other Methods
    //******************************************************************************************************************

    /**
     * Generates a proxy class for the supplied {@link ClassLoader} and proxy classes.
     * 
     * @param classLoader
     *            the classloader
     * @param proxyClasses
     *            the proxy classes
     * @return the dynamically generated proxy class
     */
    Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses);
}
