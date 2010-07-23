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

package org.apache.commons.proxy2.stub;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.provider.ConstantProvider;

/**
 * Configuration mechanism for a stub.  Implements {@link StubConfiguration} for maximum fluency.
 * A {@link StubConfigurer} needs to know the type of stub object to which it applies.
 * Any useful runtime subclass should have the type variable non-generically
 * declared (else its {@link #configure(Object)} implementation wouldn't be worth much),
 * so usually the no-arg constructor can be used.  Example:
 * <code><pre>
 * new StubConfigurer&lt;java.util.Iterable&lt;String&gt;&gt;() {
 *     protected void configure(Iterable&lt;String&gt; stub) {
 *         when(stub.iterator()).thenReturn(Arrays.asList("foo", "bar", "baz").iterator());
 *     }
 * }
 * </pre></code>
 *
 * @param <T>
 * @author Matt Benson
 */
//TODO add argument matcher capturing
public abstract class StubConfigurer<T> implements StubConfiguration {
    private Class<T> stubType;

    /** Stateful reference to the StubInterceptor currently being configured */
    private StubInterceptor stubInterceptor;

    /**
     * Create a new StubConfigurer instance.  This constructor should only be called
     * by classes that explicitly assign the T type parameter in the class definition.
     * This should include basically any runtime-usable subclass.
     */
    protected StubConfigurer() {
        this(null);
    }

    /**
     * Create a new StubConfigurer instance.
     * @param stubType
     */
    protected StubConfigurer(Class<T> stubType) {
        super();
        if (stubType != null) {
            this.stubType = stubType;
            return;
        }
        //TODO replace with lang3 TypeUtils calls
        @SuppressWarnings("unchecked")
        final Class<T> resolvedVariable = (Class<T>) getRawType(getClass(),
                StubConfigurer.class.getTypeParameters()[0]);
        if (resolvedVariable == null) {
            throw new IllegalArgumentException(
                    "stubType was not specified and could not be calculated for "
                            + getClass());
        }
        this.stubType = resolvedVariable;
    }

    /**
     * Get the stubType.
     * @return Class<T>
     */
    public Class<? extends T> getStubType() {
        return stubType;
    }

    /**
     * {@inheritDoc}
     */
    public <RT> org.apache.commons.proxy2.stub.StubConfiguration.When<RT> when(
            RT call) {
        return new When<RT>() {

            public StubConfiguration thenReturn(RT result) {
                requireStubInterceptor().addAnswer(result);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<? extends RT> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenBooleanArray when(
            boolean[] call) {
        return new WhenBooleanArray() {

            public StubConfiguration thenReturn(boolean... b) {
                requireStubInterceptor().addAnswer(b);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<boolean[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenByteArray when(
            byte[] call) {
        return new WhenByteArray() {

            public StubConfiguration thenReturn(byte... b) {
                requireStubInterceptor().addAnswer(b);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<byte[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenShortArray when(
            short[] call) {
        return new WhenShortArray() {

            public StubConfiguration thenReturn(short... s) {
                requireStubInterceptor().addAnswer(s);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<short[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenIntArray when(
            int[] call) {
        return new WhenIntArray() {

            public StubConfiguration thenReturn(int... i) {
                requireStubInterceptor().addAnswer(i);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<int[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenCharArray when(
            char[] call) {
        return new WhenCharArray() {

            public StubConfiguration thenReturn(char... c) {
                requireStubInterceptor().addAnswer(c);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<char[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenLongArray when(
            long[] call) {
        return new WhenLongArray() {

            public StubConfiguration thenReturn(long... l) {
                requireStubInterceptor().addAnswer(l);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<long[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenFloatArray when(
            float[] call) {
        return new WhenFloatArray() {

            public StubConfiguration thenReturn(float... f) {
                requireStubInterceptor().addAnswer(f);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<float[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenDoubleArray when(
            double[] call) {
        return new WhenDoubleArray() {

            public StubConfiguration thenReturn(double... d) {
                requireStubInterceptor().addAnswer(d);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<double[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public <C> org.apache.commons.proxy2.stub.StubConfiguration.WhenObjectArray<C> when(
            C[] call) {
        return new WhenObjectArray<C>() {

            public StubConfiguration thenReturn(C... c) {
                requireStubInterceptor().addAnswer(c);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<Object[]> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public org.apache.commons.proxy2.stub.StubConfiguration.WhenClass when(
            Class<?> call) {
        return new WhenClass() {

            public StubConfiguration thenReturn(Class<?> c) {
                requireStubInterceptor().addAnswer(c);
                return StubConfigurer.this;
            }

            public StubConfiguration thenAnswer(
                    ObjectProvider<Class<?>> objectProvider) {
                requireStubInterceptor().addAnswer(objectProvider);
                return StubConfigurer.this;
            }

            public StubConfiguration thenThrow(Throwable t) {
                return thenThrow(new ConstantProvider<Throwable>(t));
            }

            public StubConfiguration thenThrow(
                    ObjectProvider<? extends Throwable> throwableProvider) {
                requireStubInterceptor().addThrow(throwableProvider);
                return StubConfigurer.this;
            }
        };
    }

    /**
     * Apply thyself against the specified stub interceptor.
     * @param stubInterceptor
     */
    final void configure(StubInterceptor stubInterceptor, T stub) {
        if (stubInterceptor == null) {
            throw new IllegalArgumentException(
                    "Cannot configure null StubInterceptor");
        }
        synchronized (this) {
            this.stubInterceptor = stubInterceptor;
            try {
                configure(stub);
            } finally {
                this.stubInterceptor = null;
            }
        }
    }

    /**
     * Specify the behavior of <code>stub</code> via the {@link StubConfiguration} interface.
     * @param stub
     */
    protected abstract void configure(T stub);

    synchronized StubInterceptor requireStubInterceptor() {
        if (stubInterceptor == null) {
            throw new IllegalStateException(
                    "no StubInterceptor currently in use");
        }
        return stubInterceptor;
    }

    /**
     * Get the raw type of a Java type, given its context. Primarily for use
     * with {@link TypeVariable}s and {@link GenericArrayType}s, or when you do
     * not know the runtime type of <code>type</code>: if you know you have a
     * {@link Class} instance, it is already raw; if you know you have a
     * {@link ParameterizedType}, its raw type is only a method call away.
     * @param enclosingType context
     * @param type to read
     * @return Class<?>
     */
    // adapted from unreleased commons-lang 3.0 trunk r924407
    private static Class<?> getRawType(Type enclosingType, Type type) {
        if (type instanceof Class<?>) {
            // it is raw, no problem
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            // simple enough to get the raw type of a ParameterizedType
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (enclosingType != null) {
            if (type instanceof TypeVariable<?>) {
                // resolve the variable against the enclosing type, hope for the best (casting)
                Type resolved = resolveVariable(enclosingType,
                        (TypeVariable<?>) type);
                //this is the only change from commons-lang r924407
                return getRawType(enclosingType, resolved);
            }
            if (type instanceof GenericArrayType) {
                validateNotNull(enclosingType,
                        "Cannot get raw type of GenericArrayType without enclosing type");
                // not included in original code, but not too difficult:  just have to get raw component type...
                Class<?> rawComponentType = getRawType(enclosingType,
                        ((GenericArrayType) type).getGenericComponentType());
                // ...and know how to reflectively create array types, uncommon but not unheard of:
                return Array.newInstance(rawComponentType, 0).getClass();
            }
        }
        return null;
    }

    /**
     * Handle recursion and falling back up the graph.
     * @param enclosingType
     * @param typeVar
     * @return Type resolved
     */
    // adapted from unreleased commons-lang 3.0 trunk r924407
    private static Type resolveVariable(Type enclosingType,
            TypeVariable<?> typeVar) {
        if (enclosingType instanceof ParameterizedType) {
            ParameterizedType parameterizedEnclosingType = (ParameterizedType) enclosingType;
            TypeVariable<?>[] typeVariables = getRawType(null,
                    parameterizedEnclosingType.getRawType())
                    .getTypeParameters();
            //look for the matching variable:
            for (int i = 0; i < typeVariables.length; i++) {
                if (typeVariables[i].equals(typeVar)) {
                    return parameterizedEnclosingType.getActualTypeArguments()[i];
                }
            }
            //otherwise recurse to try against raw class
            Type result = resolveVariable(parameterizedEnclosingType
                    .getRawType(), typeVar);
            //unroll variable if returned
            if (result instanceof TypeVariable<?>) {
                return resolveVariable(enclosingType, (TypeVariable<?>) result);
            }
            return result;
        }
        if (enclosingType instanceof Class<?>) {
            Class<?> enclosingClass = (Class<?>) enclosingType;
            Type result = null;
            Type genericSuperclass = enclosingClass.getGenericSuperclass();
            if (genericSuperclass != null
                    && !Object.class.equals(genericSuperclass)) {
                result = resolveVariable(genericSuperclass, typeVar);
            }
            if (result == null) {
                for (Type genericInterface : enclosingClass
                        .getGenericInterfaces()) {
                    result = resolveVariable(genericInterface, typeVar);
                    if (result != null) {
                        break;
                    }
                }
            }
            return result;
        }
        return null;
    }

    private static void validateNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

}
