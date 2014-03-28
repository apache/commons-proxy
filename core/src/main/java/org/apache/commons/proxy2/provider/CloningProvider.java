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

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.exception.ObjectProviderException;

/**
 * Merely calls <code>clone()</code> (reflectively) on the given {@link Cloneable} object.
 * 
 * @since 1.0
 */
public class CloningProvider<T extends Cloneable> implements ObjectProvider<T>, Serializable
{
    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    private final T cloneable;

    //******************************************************************************************************************
    // Constructors
    //******************************************************************************************************************

    /**
     * Constructs a provider which returns clone copies of the specified {@link Cloneable} object.
     * 
     * @param cloneable
     *            the object to clone
     */
    public CloningProvider(T cloneable)
    {
        Validate.notNull(cloneable, "Cloneable object cannot be null.");
        Validate.isTrue(MethodUtils.getAccessibleMethod(cloneable.getClass(), "clone") != null,
                String.format("Class %s does not override clone() method as public.", cloneable.getClass().getName()));
        this.cloneable = cloneable;
    }

    //******************************************************************************************************************
    // ObjectProvider Implementation
    //******************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
    public T getObject()
    {
        try
        {
            return ObjectUtils.clone(cloneable);
        }
        catch (CloneFailedException e) {
            throw new ObjectProviderException(e.getMessage(), e.getCause());
        }
    }

}
