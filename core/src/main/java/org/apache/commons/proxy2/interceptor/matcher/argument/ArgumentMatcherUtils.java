/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.proxy2.interceptor.matcher.argument;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;

public final class ArgumentMatcherUtils
{
//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    public static <T> ArgumentMatcher<T> any()
    {
        return new AnyMatcher<T>();
    }

    public static ArgumentMatcher<String> endsWith(String suffix)
    {
        return new EndsWithMatcher(Validate.notNull(suffix));
    }

    public static <T> ArgumentMatcher<T> eq(final T value)
    {
        return new EqualsMatcher<T>(value);
    }

    public static <C extends Comparable<?>> ArgumentMatcher<C> gt(C comparable)
    {
        return new GreaterThanMatcher<C>(comparable);
    }

    public static <C extends Comparable<?>> ArgumentMatcher<C> gte(C comparable)
    {
        return new GreaterThanOrEqualMatcher<C>(comparable);
    }

    public static <T> ArgumentMatcher<T> isA(final Class<?> type)
    {
        return new InstanceOfMatcher<T>(type);
    }

    public static <T> ArgumentMatcher<T> isNull()
    {
        return new IsNullMatcher<T>();
    }

    public static <C extends Comparable<?>> ArgumentMatcher<C> lt(C comparable)
    {
        return new LessThanMatcher<C>(comparable);
    }

    public static <C extends Comparable<?>> ArgumentMatcher<C> lte(C comparable)
    {
        return new LessThanOrEqualMatcher<C>(comparable);
    }

    public static ArgumentMatcher<String> matches(String regex)
    {
        return new RegexMatcher(Validate.notNull(regex));
    }

    public static <T> ArgumentMatcher<T> notNull()
    {
        return new NotNullMatcher<T>();
    }

    public static ArgumentMatcher<String> startsWith(String prefix)
    {
        return new StartsWithMatcher(Validate.notNull(prefix));
    }

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    private ArgumentMatcherUtils()
    {

    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static final class AnyMatcher<T> implements ArgumentMatcher<T>
    {
        @Override
        public boolean matches(T argument)
        {
            return true;
        }
    }

    private abstract static class ComparatorMatcher<C extends Comparable<?>> implements ArgumentMatcher<C>
    {
        private final C comparable;

        protected ComparatorMatcher(C comparable)
        {
            this.comparable = Validate.notNull(comparable);
        }

        protected abstract boolean evaluate(int comparison);

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(C argument)
        {
            if (argument == null)
            {
                return false;
            }
            @SuppressWarnings("rawtypes")
            final int comparison = ((Comparable) comparable).compareTo(argument);
            return evaluate(comparison);
        }
    }

    public static class EndsWithMatcher implements ArgumentMatcher<String>
    {
        private final String suffix;

        public EndsWithMatcher(String suffix)
        {
            this.suffix = suffix;
        }

        @Override
        public boolean matches(String argument)
        {
        	return StringUtils.endsWith(argument, suffix);
        }
    }

    private static final class EqualsMatcher<T> implements ArgumentMatcher<T>
    {
        private final T value;

        public EqualsMatcher(T value)
        {
            this.value = value;
        }

        @Override
        public boolean matches(T argument)
        {
            return ObjectUtils.equals(argument, value);
        }
    }

    private static final class GreaterThanMatcher<C extends Comparable<?>> extends ComparatorMatcher<C>
    {
        private GreaterThanMatcher(C comparable)
        {
            super(comparable);
        }

        @Override
        protected boolean evaluate(int comparison)
        {
            return comparison < 0;
        }
    }

    private static final class GreaterThanOrEqualMatcher<C extends Comparable<?>> extends ComparatorMatcher<C>
    {
        private GreaterThanOrEqualMatcher(C comparable)
        {
            super(comparable);
        }

        @Override
        protected boolean evaluate(int comparison)
        {
            return comparison <= 0;
        }
    }

    private static final class InstanceOfMatcher<T> implements ArgumentMatcher<T>
    {
        private final Class<?> type;

        public InstanceOfMatcher(Class<?> type)
        {
            this.type = Validate.notNull(type, "type");
        }

        @Override
        public boolean matches(T argument)
        {
            return type.isInstance(argument);
        }
    }

    private static final class IsNullMatcher<T> implements ArgumentMatcher<T>
    {
        @Override
        public boolean matches(T argument)
        {
            return argument == null;
        }
    }

    private static final class LessThanMatcher<C extends Comparable<?>> extends ComparatorMatcher<C>
    {
        private LessThanMatcher(C comparable)
        {
            super(comparable);
        }

        @Override
        protected boolean evaluate(int comparison)
        {
            return comparison > 0;
        }
    }

    private static final class LessThanOrEqualMatcher<C extends Comparable<?>> extends ComparatorMatcher<C>
    {
        private LessThanOrEqualMatcher(C comparable)
        {
            super(comparable);
        }

        @Override
        protected boolean evaluate(int comparison)
        {
            return comparison >= 0;
        }
    }

    private static final class NotNullMatcher<T> implements ArgumentMatcher<T>
    {
        @Override
        public boolean matches(T argument)
        {
            return argument != null;
        }
    }

    public static class RegexMatcher implements ArgumentMatcher<String>
    {
        private final String regex;

        public RegexMatcher(String regex)
        {
            this.regex = regex;
        }

        @Override
        public boolean matches(String argument)
        {
            return argument != null && argument.matches(regex);
        }
    }

    private static final class StartsWithMatcher implements ArgumentMatcher<String>
    {
        private final String prefix;

        private StartsWithMatcher(String prefix)
        {
            this.prefix = prefix;
        }

        @Override
        public boolean matches(String argument)
        {
        	return StringUtils.startsWith(argument, prefix);
        }
    }
}
