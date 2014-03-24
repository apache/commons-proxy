package org.apache.commons.proxy2.stub;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.proxy2.ProxyFactory;
import org.apache.commons.proxy2.asm4.ASM4ProxyFactory;
import org.apache.commons.proxy2.cglib.CglibProxyFactory;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.javassist.JavassistProxyFactory;
import org.apache.commons.proxy2.jdk.JdkProxyFactory;
import org.apache.commons.proxy2.provider.ObjectProviderUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AbstractStubTestCase
{
    // ----------------------------------------------------------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------------------------------------------------------

    @Parameter
    public ProxyFactory proxyFactory;
    protected StubInterface target;

    // ----------------------------------------------------------------------------------------------------------------------
    // Abstract Methods
    // ----------------------------------------------------------------------------------------------------------------------

    protected abstract StubInterface createProxy(Trainer<StubInterface> trainer);

    // ----------------------------------------------------------------------------------------------------------------------
    // Other Methods
    // ----------------------------------------------------------------------------------------------------------------------

    @Parameters(name = "{0}")
    public static List<Object[]> createParameters() {
        final List<Object[]> result = new ArrayList<Object[]>();
        result.add(new Object[] { new JdkProxyFactory() });
        result.add(new Object[] { new CglibProxyFactory() });
        result.add(new Object[] { new JavassistProxyFactory() });
        result.add(new Object[] { new ASM4ProxyFactory() });
        return result;
    }

    @Before
    public final void setUpProxyFactory()
    {
        this.target = proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, StubInterface.class);
    }

    @Test
    public void testAnyMatcher()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one(any(String.class))).thenReturn("World");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals("World", proxy.one(null));
    }

    @Test(expected = IllegalStateException.class)
    public void testMixingArgumentMatchingStrategies()
    {
        createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.three(isInstance(String.class), "World"))
                        .thenAnswer(ObjectProviderUtils.constant("World"));
            }
        });
    }

    @Test
    public void testStubReturn()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.stub()).thenStub(new Trainer<StubInterface>()
                {
                    @Override
                    protected void train(StubInterface trainee)
                    {
                        when(trainee.one("Hello")).thenReturn("World");
                    }
                });
            }
        });
        assertNotNull(proxy.stub());
        assertEquals("World", proxy.stub().one("Hello"));
    }

    @Test
    public void testStubArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.stubs()).thenBuildArray().addElement(new Trainer<StubInterface>()
                {
                    @Override
                    protected void train(StubInterface trainee)
                    {
                        when(trainee.one("Whatever")).thenReturn("Zero");
                    }
                }).addElement(new Trainer<StubInterface>()
                {
                    @Override
                    protected void train(StubInterface trainee)
                    {
                        when(trainee.one("Whatever")).thenReturn("One");
                    }
                })
                .build();
            }
        });

        assertEquals("Zero", proxy.stubs()[0].one("Whatever"));
        assertEquals("One", proxy.stubs()[1].one("Whatever"));
    }

    @Test(expected = IllegalStateException.class)
    public void testThenBeforeWhen()
    {
        createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                thenThrow(new RuntimeException("Oops!"));
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithException()
    {
        StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                trainee.voidMethod("Hello");
                thenThrow(new IllegalArgumentException("Nope!"));
            }
        });
        proxy.voidMethod("Hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithProvidedException()
    {
        StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                trainee.voidMethod("Hello");
                thenThrow(ObjectProviderUtils.constant(new IllegalArgumentException("Nope!")));
            }
        });
        proxy.voidMethod("Hello");
    }

    @Test(expected = RuntimeException.class)
    public void testThrowingExceptionObject()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one("Hello")).thenThrow(new RuntimeException("No way, Jose!"));
            }
        });
        proxy.one("Hello");
    }

    @Test(expected = RuntimeException.class)
    public void testThrowingProvidedException()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one("Hello")).thenThrow(
                        ObjectProviderUtils.constant(new RuntimeException("No way, Jose!")));
            }
        });
        proxy.one("Hello");
    }

    @Test(expected = IllegalStateException.class)
    public void testUsingWrongStub()
    {
        createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(final StubInterface parent)
            {
                when(parent.stub()).thenStub(new Trainer<StubInterface>()
                {
                    @Override
                    protected void train(final StubInterface child)
                    {
                        when(parent.one("Hello")).thenReturn("World");
                    }
                });
            }
        });
    }

    @Test
    public void testWithArgumentMatchers()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one(isInstance(String.class))).thenAnswer(ObjectProviderUtils.constant("World"));
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals("World", proxy.one("Whatever"));
    }

    @Test
    public void testWithArrayParameter()
    {
        StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.arrayParameter("One", "Two", "Three")).thenReturn("Four");
            }
        });

        assertEquals("Four", proxy.arrayParameter("One", "Two", "Three"));
    }

    @Test
    public void testWithBooleanArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.booleanArray()).thenReturn(false, true, false);
            }
        });
        assertTrue(Arrays.equals(new boolean[] { false, true, false }, proxy.booleanArray()));
    }

    @Test
    public void testWithByteArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.byteArray()).thenReturn((byte) 1, (byte) 2);
            }
        });
        assertArrayEquals(new byte[] { 1, 2 }, proxy.byteArray());
    }

    @Test
    public void testWithCharArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.charArray()).thenReturn('a', 'b', 'c');
            }
        });
        assertArrayEquals(new char[] { 'a', 'b', 'c' }, proxy.charArray());
    }

    @Test
    public void testWithDoubleArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.doubleArray()).thenReturn(1.0, 2.0);
            }
        });
        assertArrayEquals(new double[] { 1.0, 2.0 }, proxy.doubleArray(), 0.0);
    }

    @Test
    public void testWithFloatArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.floatArray()).thenReturn(1f, 2f);
            }
        });
        assertArrayEquals(new float[] { 1f, 2f }, proxy.floatArray(), 0.0f);
    }

    @Test
    public void testWithIntArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.intArray()).thenReturn(1, 2);
            }
        });
        assertArrayEquals(new int[] { 1, 2 }, proxy.intArray());
    }

    @Test
    public void testWithLongArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.longArray()).thenReturn(1, 2);
            }
        });
        assertArrayEquals(new long[] { 1, 2 }, proxy.longArray());
    }

    @Test
    public void testWithMismatchedArgument()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one(eq("Hello"))).thenReturn("World");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals(null, proxy.one("Whatever"));
    }

    @Test
    public void testWithMultipleMethodsTrained()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one("Hello")).thenReturn("World");
                when(trainee.two("Foo")).thenReturn("Bar");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals("Bar", proxy.two("Foo"));
    }

    @Test
    public void testWithShortArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.shortArray()).thenReturn((short) 1, (short) 2);
            }
        });
        assertArrayEquals(new short[] { 1, 2 }, proxy.shortArray());
    }

    @Test
    public void testWithSingleMethodTrained()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.one("Hello")).thenReturn("World");
            }
        });
        assertEquals("World", proxy.one("Hello"));
        assertEquals(null, proxy.two("Whatever"));
        assertEquals(null, proxy.one("Mismatch!"));
    }

    @Test
    public void testWithStringArray()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.stringArray()).thenReturn("One", "Two");
            }
        });
        assertArrayEquals(new String[] { "One", "Two" }, proxy.stringArray());
    }

    /*
     * This test replicates #thenStub() functionality in a more "ignorant" (ergo versatile) manner.
     */
    @Test
    public void testInterruptResume()
    {
        final StubInterface proxy = createProxy(new Trainer<StubInterface>()
        {
            @Override
            protected void train(StubInterface trainee)
            {
                when(trainee.stub()).thenReturn(createProxy(new Trainer<StubInterface>()
                {
                    @Override
                    protected void train(StubInterface trainee)
                    {
                        when(trainee.one("Hello")).thenReturn("World");
                    }
                }));
            }
        });
        assertNotNull(proxy.stub());
        assertEquals("World", proxy.stub().one("Hello"));
    }
}
