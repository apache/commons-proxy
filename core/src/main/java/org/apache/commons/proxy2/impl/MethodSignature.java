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

package org.apache.commons.proxy2.impl;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * A class for capturing the signature of a method (its name and parameter types).
 * 
 * @author James Carman
 * @since 1.0
 */
public class MethodSignature implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Map<Class<?>, Character> PRIMITIVE_ABBREVIATIONS;
    private static final Map<Character, Class<?>> REVERSE_ABBREVIATIONS;
    static
    {
        final Map<Class<?>, Character> primitiveAbbreviations = new HashMap<Class<?>, Character>();
        primitiveAbbreviations.put(Boolean.TYPE, Character.valueOf('Z'));
        primitiveAbbreviations.put(Byte.TYPE, Character.valueOf('B'));
        primitiveAbbreviations.put(Short.TYPE, Character.valueOf('S'));
        primitiveAbbreviations.put(Integer.TYPE, Character.valueOf('I'));
        primitiveAbbreviations.put(Character.TYPE, Character.valueOf('C'));
        primitiveAbbreviations.put(Long.TYPE, Character.valueOf('J'));
        primitiveAbbreviations.put(Float.TYPE, Character.valueOf('F'));
        primitiveAbbreviations.put(Double.TYPE, Character.valueOf('D'));
        primitiveAbbreviations.put(Void.TYPE, Character.valueOf('V'));
        final Map<Character, Class<?>> reverseAbbreviations = new HashMap<Character, Class<?>>();
        for (Map.Entry<Class<?>, Character> e : primitiveAbbreviations.entrySet())
        {
            reverseAbbreviations.put(e.getValue(), e.getKey());
        }
        PRIMITIVE_ABBREVIATIONS = Collections.unmodifiableMap(primitiveAbbreviations);
        REVERSE_ABBREVIATIONS = Collections.unmodifiableMap(reverseAbbreviations);
    }

    private static void appendTo(StringBuilder buf, Class<?> type)
    {
        if (type.isPrimitive())
        {
            buf.append(PRIMITIVE_ABBREVIATIONS.get(type));
        }
        else if (type.isArray())
        {
            buf.append('[');
            appendTo(buf, type.getComponentType());
        }
        else
        {
            buf.append('L').append(type.getName().replace('.', '/')).append(';');
        }
    }

    private static class SignaturePosition extends ParsePosition
    {
        SignaturePosition()
        {
            super(0);
        }

        SignaturePosition next()
        {
            return plus(1);
        }

        SignaturePosition plus(int addend)
        {
            setIndex(getIndex() + addend);
            return this;
        }
    }

    private static Pair<String, Class<?>[]> parse(String internal)
    {
        Validate.notBlank(internal, "Cannot parse blank method signature");
        final SignaturePosition pos = new SignaturePosition();
        int lparen = internal.indexOf('(', pos.getIndex());
        Validate.isTrue(lparen > 0, "Method signature \"%s\" requires parentheses", internal);
        final String name = internal.substring(0, lparen).trim();
        Validate.notBlank(name, "Method signature \"%s\" has blank name", internal);

        pos.setIndex(lparen + 1);

        boolean complete = false;
        final List<Class<?>> params = new ArrayList<Class<?>>();
        while (pos.getIndex() < internal.length())
        {
            final char c = internal.charAt(pos.getIndex());
            if (Character.isWhitespace(c))
            {
                pos.next();
                continue;
            }
            final Character k = Character.valueOf(c);
            if (REVERSE_ABBREVIATIONS.containsKey(k))
            {
                params.add(REVERSE_ABBREVIATIONS.get(k));
                pos.next();
                continue;
            }
            if (')' == c)
            {
                complete = true;
                pos.next();
                break;
            }
            try
            {
                params.add(parseType(internal, pos));
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalArgumentException(String.format("Method signature \"%s\" references unknown type",
                        internal), e);
            }
        }
        Validate.isTrue(complete, "Method signature \"%s\" is incomplete", internal);
        Validate.isTrue(StringUtils.isBlank(internal.substring(pos.getIndex())),
                "Method signature \"%s\" includes unrecognized content beyond end", internal);

        return Pair.of(name, params.toArray(ArrayUtils.EMPTY_CLASS_ARRAY));
    }

    private static Class<?> parseType(String internal, SignaturePosition pos) throws ClassNotFoundException
    {
        final int here = pos.getIndex();
        final char c = internal.charAt(here);

        switch (c)
        {
        case '[':
            pos.next();
            final Class<?> componentType = parseType(internal, pos);
            return Array.newInstance(componentType, 0).getClass();
        case 'L':
            pos.next();
            final int type = pos.getIndex();
            final int semi = internal.indexOf(';', type);
            Validate.isTrue(semi > 0, "Type at index %d of method signature \"%s\" not terminated by semicolon",
                    Integer.valueOf(here), internal);
            final String className = internal.substring(type, semi).replace('/', '.');
            Validate.notBlank(className, "Invalid classname at position %d of method signature \"%s\"",
                    Integer.valueOf(type), internal);
            pos.setIndex(semi + 1);
            return Class.forName(className);
        default:
            throw new IllegalArgumentException(String.format(
                    "Unexpected character at index %d of method signature \"%s\"",
                    Integer.valueOf(here), internal));
        }
    }

    //******************************************************************************************************************
    // Fields
    //******************************************************************************************************************

    /**
     * Stored as a Java method descriptor minus return type.
     */
    private final String internal;

    //******************************************************************************************************************
    // Constructors
    //******************************************************************************************************************

    /**
     * Create a new MethodSignature instance.
     * 
     * @param method
     */
    public MethodSignature(Method method)
    {
        final StringBuilder buf = new StringBuilder(method.getName()).append('(');
        for (Class<?> p : method.getParameterTypes())
        {
            appendTo(buf, p);
        }
        buf.append(')');
        this.internal = buf.toString();
    }

    //******************************************************************************************************************
    // Methods
    //******************************************************************************************************************

    /**
     * Get the corresponding {@link Method} instance from the specified {@link Class}.
     * 
     * @param type
     * @return Method
     */
    public Method toMethod(Class<?> type)
    {
        final Pair<String, Class<?>[]> info = parse(internal);
        return MethodUtils.getAccessibleMethod(type, info.getLeft(), info.getRight());
    }

    //******************************************************************************************************************
    // Canonical Methods
    //******************************************************************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        if (o.getClass() != getClass())
        {
            return false;
        }
        MethodSignature other = (MethodSignature) o;
        return other.internal.equals(internal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(internal).build().intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return internal;
    }
}
