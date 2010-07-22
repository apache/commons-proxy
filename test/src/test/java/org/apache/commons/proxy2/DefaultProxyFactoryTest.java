package org.apache.commons.proxy2;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;

import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the default ProxyFactory provided by {@link ProxyUtils}.
 */
public class DefaultProxyFactoryTest {
    private ProxyFactory proxyFactory;

    @Before
    public void setUp() {
        proxyFactory = ProxyUtils.proxyFactory();
    }

    @Test
    public void testBasic() {
        Foo foo = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE,
                Foo.class);
        assertNotNull(foo);
        assertTrue(foo instanceof Proxy);
    }

    @Test
    public void testSubclassing() {
        Bar bar = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE,
                Bar.class);
        assertNotNull(bar);
    }

    @Test
    public void testCombined() {
        Bar bar = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE,
                Bar.class, Foo.class);
        assertNotNull(bar);
        assertTrue(bar instanceof Foo);
    }

    public interface Foo {
    }

    public static class Bar {
    }
}
