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

import org.apache.commons.proxy2.ObjectProvider;

/**
 * Fluent stub configuration interface inspired by Mockito stubbing mechanisms.
 * This interface declares all the methods necessary to, in particular, allow
 * varargs to be used to specify return values for any array type.
 */
public interface StubConfiguration {

    /**
     * Permits the setup of thrown {@link Throwable}s when stubbing.
     */
    interface MayThrow {
        /**
         * Throw something.
         * @param t
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenThrow(Throwable t);

        /**
         * Throw something, deferring construction.
         * @param throwableProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenThrow(
                ObjectProvider<? extends Throwable> throwableProvider);
    }

    /**
     * Intermediate result of a generic when(...) call
     *
     * @param <R>
     */
    interface When<R> extends MayThrow {
        /**
         * Declare return value.
         * @param result
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(R result);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(
                ObjectProvider<? extends R> objectProvider);

    }

    /**
     * "when(...)"
     * @param <RT>
     * @param call
     * @return {@link When}
     */
    <RT> When<RT> when(RT call);

    /**
     * Intermediate result of a when(boolean[]) call
     */
    interface WhenBooleanArray extends MayThrow {
        /**
         * Declare return value.
         * @param b
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(boolean... b);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<boolean[]> objectProvider);
    }

    /**
     * "when(boolean[])"
     * @param call
     * @return {@link WhenBooleanArray}
     */
    WhenBooleanArray when(boolean[] call);

    /**
     * Intermediate result of a when(byte[]) call
     */
    interface WhenByteArray extends MayThrow {
        /**
         * Declare return value.
         * @param b
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(byte... b);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<byte[]> objectProvider);
    }

    /**
     * "when(byte[])"
     * @param call
     * @return {@link WhenByteArray}
     */
    WhenByteArray when(byte[] call);

    /**
     * Intermediate result of a when(short[]) call
     */
    interface WhenShortArray extends MayThrow {
        /**
         * Declare return value
         * @param s
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(short... s);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<short[]> objectProvider);
    }

    /**
     * "when(short[])"
     * @param call
     * @return {@link WhenShortArray}
     */
    WhenShortArray when(short[] call);

    /**
     * Intermediate result of a when(int[]) call
     *
     * @param
     */
    interface WhenIntArray extends MayThrow {
        /**
         * Declare return value
         * @param i
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(int... i);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<int[]> objectProvider);
    }

    /**
     * "when(int[])"
     * @param call
     * @return {@link WhenIntArray}
     */
    WhenIntArray when(int[] call);

    /**
     * Intermediate result of a when(char[]) call
     */
    interface WhenCharArray extends MayThrow {
        /**
         * Declare return value
         * @param c
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(char... c);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<char[]> objectProvider);
    }

    /**
     * "when(char[])"
     * @param call
     * @return {@link WhenCharArray}
     */
    WhenCharArray when(char[] call);

    /**
     * Intermediate result of a when(long[]) call
     */
    interface WhenLongArray extends MayThrow {
        /**
         * Declare return value
         * @param l
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(long... l);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<long[]> objectProvider);
    }

    /**
     * "when(long[])"
     * @param call
     * @return {@link WhenLongArray}
     */
    WhenLongArray when(long[] call);

    /**
     * Intermediate result of a when(float[]) call
     */
    interface WhenFloatArray extends MayThrow {
        /**
         * Declare return value
         * @param f
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(float... f);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<float[]> objectProvider);
    }

    /**
     * "when(float[])"
     * @param call
     * @return {@link WhenFloatArray}
     */
    WhenFloatArray when(float[] call);

    /**
     * Intermediate result of a when(double[]) call
     */
    interface WhenDoubleArray extends MayThrow {
        /**
         * Declare return value
         * @param d
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(double... d);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<double[]> objectProvider);
    }

    /**
     * "when(double[])"
     * @param call
     * @return {@link WhenDoubleArray}
     */
    WhenDoubleArray when(double[] call);

    /**
     * Intermediate result of a when(Object[]) call
     *
     * @param <C>
     */
    interface WhenObjectArray<C> extends MayThrow {
        /**
         * Declare return value
         * @param c
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(C... c);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<C[]> objectProvider);
    }

    /**
     * "when(Object[])"
     * @param call
     * @return {@link WhenObjectArray}
     */
    <C> WhenObjectArray<C> when(C[] call);

    /**
     * Intermediate result of a when(Class) call.
     * Provided because it is such a common case to have a mismatch between a
     * declared Class<?> return type and the bound parameter of a class literal.
     */
    interface WhenClass extends MayThrow {
        /**
         * Declare return value
         * @param c
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenReturn(Class<?> c);

        /**
         * Defer return value.
         * @param objectProvider
         * @return the original {@link StubConfiguration} for chaining
         */
        StubConfiguration thenAnswer(ObjectProvider<Class<?>> objectProvider);
    }

    /**
     * "when(Class<?>)" (because it is such a common case to have a mismatch between a
     * declared Class<?> return type and the bound parameter of a class literal)
     * @param call
     * @return {@link WhenClass}
     */
    WhenClass when(Class<?> call);

}
