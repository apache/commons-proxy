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
import org.apache.commons.proxy2.exception.ObjectProviderException;

import java.io.Serializable;

/**
 * Uses <code>Class.newInstance()</code> to instantiate an object.
 *
 * @author James Carman
 * @since 1.0
 */
public class BeanProvider<T> implements ObjectProvider<T>, Serializable
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private Class<T> beanClass;

//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public BeanProvider()
    {
    }

    /**
     * Constructs a provider which instantiates objects of the specified bean class.
     *
     * @param beanClass the bean class
     */
    public BeanProvider( Class<T> beanClass )
    {
        this.beanClass = beanClass;
    }

//**********************************************************************************************************************
// ObjectProvider Implementation
//**********************************************************************************************************************

    public T getObject()
    {
        try
        {
            if( beanClass == null )
            {
                throw new ObjectProviderException("No bean class provided.");
            }
            return beanClass.newInstance();
        }
        catch( InstantiationException e )
        {
            throw new ObjectProviderException("Class " + beanClass.getName() + " is not concrete.", e);
        }
        catch( IllegalAccessException e )
        {
            throw new ObjectProviderException("Constructor for class " + beanClass.getName() + " is not accessible.",
                    e);
        }
    }

//**********************************************************************************************************************
// Getter/Setter Methods
//**********************************************************************************************************************

    public void setBeanClass( Class<T> beanClass )
    {
        this.beanClass = beanClass;
    }
}
