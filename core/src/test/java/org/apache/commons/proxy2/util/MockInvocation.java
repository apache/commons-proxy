package org.apache.commons.proxy2.util;

import org.apache.commons.proxy2.Invocation;

import java.lang.reflect.Method;

public class MockInvocation implements Invocation
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Method method;
    private final Object[] arguments;
    private final Object returnValue;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public MockInvocation(Method method, Object returnValue, Object... arguments)
    {
        this.returnValue = returnValue;
        this.arguments = arguments;
        this.method = method;
    }

//----------------------------------------------------------------------------------------------------------------------
// Invocation Implementation
//----------------------------------------------------------------------------------------------------------------------


    @Override
    public Object[] getArguments()
    {
        return arguments;
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    @Override
    public Object getProxy()
    {
        throw new UnsupportedOperationException("Proxy objects aren't supported.");
    }

    @Override
    public Object proceed() throws Throwable
    {
        return returnValue;
    }
}
