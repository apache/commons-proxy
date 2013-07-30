package org.apache.commons.proxy2.interceptor.matcher.argument;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;

public class ArgumentMatcherUtils
{
//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    public static ArgumentMatcher any()
    {
        return new ArgumentMatcher()
        {
            @Override
            public boolean matches(Object argument)
            {
                return true;
            }
        };
    }

    public static ArgumentMatcher eq(final Object value)
    {
        return new ArgumentMatcher()
        {
            @Override
            public boolean matches(Object argument)
            {
                return ObjectUtils.equals(argument, value);
            }
        };
    }

    public static ArgumentMatcher isInstance(final Class<?> type)
    {
        return new ArgumentMatcher()
        {
            @Override
            public boolean matches(Object argument)
            {
                return type.isInstance(argument);
            }
        };
    }
}
