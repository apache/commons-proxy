package org.apache.commons.proxy2.util;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.ProxyUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * @author James Carman
 * @since 1.1
 */
public abstract class AbstractTestCase
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    protected void assertSerializable( Object o )
    {
        assertTrue(o instanceof Serializable);
        SerializationUtils.clone(( Serializable ) o);
    }

    protected MockInvocationBuilder mockInvocation(Class<?> type, String name, Class<?>... argumentTypes)
    {
        try
        {
            return new MockInvocationBuilder(Validate.notNull(type).getMethod(name, argumentTypes));
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Method not found.", e);
        }
    }

    protected static final class MockInvocationBuilder implements Builder<Invocation>
    {
        private final Method method;
        private Object[] arguments = ProxyUtils.EMPTY_ARGUMENTS;
        private Object returnValue = null;

        public MockInvocationBuilder(Method method)
        {
            this.method = method;
        }

        public MockInvocationBuilder withArguments(Object... arguments)
        {
            this.arguments = arguments;
            return this;
        }

        public MockInvocationBuilder returning(Object value)
        {
            this.returnValue = value;
            return this;
        }

        @Override
        public Invocation build()
        {
            return new MockInvocation(method, returnValue, arguments);
        }
    }
}
