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
package org.apache.commons.proxy.factory.javassist;

import org.apache.commons.proxy.factory.AbstractProxyFactoryTestCase;
import org.apache.commons.proxy.util.Echo;
import org.apache.commons.proxy.util.DuplicateEcho;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * @author James Carman
 * @version 1.0
 */
public class TestJavassistProxyFactory extends AbstractProxyFactoryTestCase
{
    public TestJavassistProxyFactory()
    {
        super( new JavassistProxyFactory() );
    }
}
