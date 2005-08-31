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
package org.apache.commons.proxy.factory.reflect;

import org.apache.commons.proxy.ObjectProvider;

/**
 * An invocation handler which delegates to an object supplied by an {@link ObjectProvider}.
 *
 * @author James Carman
 * @version 1.0
 */
public class DelegateProviderInvocationHandler extends DelegatingInvocationHandler
{
    private final ObjectProvider delegateProvider;

    public DelegateProviderInvocationHandler( ObjectProvider delegateProvider )
    {
        this.delegateProvider = delegateProvider;
    }

    protected Object getDelegate()
    {
        return delegateProvider.getDelegate();
    }
}
