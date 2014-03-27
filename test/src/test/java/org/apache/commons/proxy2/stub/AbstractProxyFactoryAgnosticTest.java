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
package org.apache.commons.proxy2.stub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.asm.ASMProxyFactory;
import org.apache.commons.proxy2.cglib.CglibProxyFactory;
import org.apache.commons.proxy2.javassist.JavassistProxyFactory;
import org.apache.commons.proxy2.jdk.JdkProxyFactory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Conveniently defines the setup for a unit test class that runs with all known {@link ProxyFactory} implementations.
 */
@RunWith(Parameterized.class)
public abstract class AbstractProxyFactoryAgnosticTest
{

    @Parameters(name = "{0}")
    public static List<Object[]> createParameters()
    {
        final List<Object[]> result = new ArrayList<Object[]>();
        result.add(new Object[] { new JdkProxyFactory() });
        result.add(new Object[] { new CglibProxyFactory() });
        result.add(new Object[] { new JavassistProxyFactory() });
        result.add(new Object[] { new ASMProxyFactory() });
        return result;
    }

    @Parameter
    public ProxyFactory proxyFactory;
}
