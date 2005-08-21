/*
 * Copyright (c) 2005 Carman Consulting, Inc. All Rights Reserved.
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
package org.apache.commons.proxy.util;

import java.util.Comparator;

/**
 * @author James Carman
 * @version 1.0
 */
public class EchoImpl implements Echo
{
    private boolean initialized = false;
    private String stringDependency;
    private Integer integerDependency;
    private Comparator comparator;

    public EchoImpl()
    {
    }

    public EchoImpl( String stringDependency, Integer integerDependency, Comparator comparator )
    {
        this.stringDependency = stringDependency;
        this.integerDependency = integerDependency;
        this.comparator = comparator;
    }

    public EchoImpl( String stringDependency, Integer integerDependency )
    {
        this.stringDependency = stringDependency;
        this.integerDependency = integerDependency;
    }

    public EchoImpl( Comparator comparator )
    {
        this.comparator = comparator;
    }

    public String getStringDependency()
    {
        return stringDependency;
    }

    public void setComparator( Comparator comparator )
    {
        this.comparator = comparator;
    }

    public Comparator getComparator()
    {
        return comparator;
    }

    public void setStringDependency( String stringDependency )
    {
        this.stringDependency = stringDependency;
    }

    public Integer getIntegerDependency()
    {
        return integerDependency;
    }

    public void setIntegerDependency( Integer integerDependency )
    {
        this.integerDependency = integerDependency;
    }

    public String echoBack( String message )
    {
        return message;
    }

    public void init()
    {
        this.initialized = true;
    }

    public boolean isInitialized()
    {
        return initialized;
    }
}
