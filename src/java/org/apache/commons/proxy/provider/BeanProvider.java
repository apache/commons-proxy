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

import org.apache.commons.proxy.exception.ObjectProviderException;

/**
 * Uses <code>Class.newInstance()</code> to instantiate an object.
 *
 * @author James Carman
 * @version $Rev: 57 $
 */
public class BeanProvider extends AbstractObjectProvider
{
    private final Class beanClass;

    public BeanProvider( Class beanClass )
    {
        this.beanClass = beanClass;
    }

    public Object getDelegate()
    {
        try
        {
            return beanClass.newInstance();
        }
        catch( InstantiationException e )
        {
            throw new ObjectProviderException( "Class " + beanClass.getName() + " is not concrete.", e );
        }
        catch( IllegalAccessException e )
        {
            throw new ObjectProviderException( "Constructor for class " + beanClass.getName() + " is not accessible.",
                                               e );
        }
    }
}
