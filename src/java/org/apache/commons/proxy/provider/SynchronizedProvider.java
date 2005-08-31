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
package org.apache.commons.proxy.provider;

import org.apache.commons.proxy.ObjectProvider;

/**
 * Wraps another object provider, making it synchronized.
 *
 * @author James Carman
 * @version 1.0
 */
public class SynchronizedProvider extends ProviderDecorator
{
    private final Object monitor;

    public SynchronizedProvider( ObjectProvider inner, Object monitor )
    {
        super( inner );
        this.monitor = monitor;
    }

    public SynchronizedProvider( ObjectProvider inner )
    {
        super( inner );
        monitor = this;
    }

    public Object getDelegate()
    {
        synchronized( monitor )
        {
            return super.getDelegate();
        }
    }
}
