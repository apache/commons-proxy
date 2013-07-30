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
        return new AnyMatcher();
    }

    public static ArgumentMatcher eq(final Object value)
    {
        return new EqualsMatcher(value);
    }

    public static ArgumentMatcher isA(final Class<?> type)
    {
        return new InstanceOfMatcher(type);
    }

    public static ArgumentMatcher isNull()
    {
        return new IsNullMatcher();
    }

    public static ArgumentMatcher notNull()
    {
        return new NotNullMatcher();
    }

    public static ArgumentMatcher same(final Object ref)
    {
        return new SameMatcher(ref);
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class AnyMatcher implements ArgumentMatcher
    {
        @Override
        public boolean matches(Object argument)
        {
            return true;
        }
    }

    private static class EqualsMatcher implements ArgumentMatcher
    {
        private final Object value;

        public EqualsMatcher(Object value)
        {
            this.value = value;
        }

        @Override
        public boolean matches(Object argument)
        {
            return ObjectUtils.equals(argument, value);
        }
    }

    private static class InstanceOfMatcher implements ArgumentMatcher
    {
        private final Class<?> type;

        public InstanceOfMatcher(Class<?> type)
        {
            this.type = type;
        }

        @Override
        public boolean matches(Object argument)
        {
            return type.isInstance(argument);
        }
    }

    private static class IsNullMatcher implements ArgumentMatcher
    {
        @Override
        public boolean matches(Object argument)
        {
            return argument == null;
        }
    }

    private static class NotNullMatcher implements ArgumentMatcher
    {
        @Override
        public boolean matches(Object argument)
        {
            return argument != null;
        }
    }

    private static class SameMatcher implements ArgumentMatcher
    {
        private final Object ref;

        public SameMatcher(Object ref)
        {
            this.ref = ref;
        }

        @Override
        public boolean matches(Object argument)
        {
            return argument == ref;
        }
    }
}
