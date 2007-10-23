package org.apache.commons.proxy.interceptor;

import junit.framework.TestCase;
import org.apache.commons.proxy.ProxyFactory;

import java.io.ByteArrayOutputStream;

public class TestSerializingInterceptor extends TestCase
{
    public void testWithSerializableParametersAndReturn()
    {
        final ObjectEchoImpl target = new ObjectEchoImpl();
        ObjectEcho echo =
                (ObjectEcho) new ProxyFactory().createInterceptorProxy(target,
                        new SerializingInterceptor(),
                        new Class[]{ObjectEcho.class});
        final Object originalParameter = "Hello, World!";
        final Object returnValue = echo.echoBack(originalParameter);
        assertNotSame(originalParameter, target.parameter);
        assertNotSame(originalParameter, returnValue);
        assertNotSame(returnValue, target.parameter);
    }

    public void testWithInvalidParameterType()
    {
        try
        {
            final ObjectEchoImpl target = new ObjectEchoImpl();
            ObjectEcho echo =
                    (ObjectEcho) new ProxyFactory().createInterceptorProxy(target,
                            new SerializingInterceptor(),
                            new Class[]{ObjectEcho.class});
            final Object originalParameter = new ByteArrayOutputStream();
            echo.echoBack(originalParameter);
            fail("Should not be able to call method with non-serializable parameter type.");
        }
        catch (RuntimeException e)
        {

        }

    }

    public static interface ObjectEcho
    {
        public Object echoBack(Object object);
    }

    public static class ObjectEchoImpl implements ObjectEcho
    {
        private Object parameter;

        public Object echoBack(Object object)
        {
            this.parameter = object;
            return object;
        }
    }
}
