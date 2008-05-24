package org.apache.commons.proxy.invoker;

import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ProxyUtils;

import java.lang.reflect.Method;

/**
 * A chain invoker will invoke the method on each object in the chain until one of them
 * returns a non-default value
 *
 * @author James Carman
 * @since 1.1
 */
public class ChainInvoker implements Invoker
{
    private final Object[] targets;

    public ChainInvoker(Object[] targets)
    {
        this.targets = targets;
    }

    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable
    {
        for (int i = 0; i < targets.length; i++)
        {
            Object target = targets[i];
            Object value = method.invoke(target, arguments);
            if (value != null && !value.equals(ProxyUtils.getDefaultValue(method.getReturnType())))
            {
                return value;
            }
        }
        return ProxyUtils.getDefaultValue(method.getReturnType());
    }
}

