package org.apache.commons.proxy.cglib;

import org.apache.commons.proxy.AbstractSubclassingProxyFactoryTestCase;

public class TestCglibProxyFactory extends AbstractSubclassingProxyFactoryTestCase
{
//**********************************************************************************************************************
// Constructors
//**********************************************************************************************************************

    public TestCglibProxyFactory()
    {
        super(new CglibProxyFactory());
    }
}
