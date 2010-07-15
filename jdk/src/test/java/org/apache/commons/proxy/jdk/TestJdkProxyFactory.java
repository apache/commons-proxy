package org.apache.commons.proxy.jdk;

import org.apache.commons.proxy.AbstractProxyFactoryTestCase;
import org.apache.commons.proxy.AbstractSubclassingProxyFactoryTestCase;

public class TestJdkProxyFactory extends AbstractProxyFactoryTestCase
{
    public TestJdkProxyFactory()
    {
        super(new JdkProxyFactory());
    }
}
