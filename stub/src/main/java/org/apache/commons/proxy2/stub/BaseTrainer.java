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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.interceptor.InterceptorUtils;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.apache.commons.proxy2.interceptor.matcher.argument.ArgumentMatcherUtils;

public abstract class BaseTrainer<S extends BaseTrainer<S, T>, T>
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------
    public final Class<T> traineeType;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Create a new {@link BaseTrainer} instance. This constructor should only be called
     * by classes that explicitly assign the T parameter in the class definition.
     * This should include basically any runtime-usable class.
     */
    protected BaseTrainer()
    {
        this(null);
    }

    protected BaseTrainer(Class<T> traineeType)
    {
        super();
        if (traineeType != null)
        {
            this.traineeType = traineeType;
            return;
        }
        @SuppressWarnings("unchecked")
        final Class<T> resolvedVariable =
            (Class<T>) TypeUtils.getRawType(BaseTrainer.class.getTypeParameters()[1], getClass());
        Validate.isTrue(resolvedVariable != null, "Trainee type was not specified and could not be calculated for %s",
            getClass());
        this.traineeType = resolvedVariable;
    }

//----------------------------------------------------------------------------------------------------------------------
// Abstract Methods
//----------------------------------------------------------------------------------------------------------------------

    protected abstract void train(T trainee);

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    protected <R> R any(Class<R> type)
    {
        record(ArgumentMatcherUtils.any());
        return null;
    }

    private void record(ArgumentMatcher<?> matcher)
    {
        trainingContext().record(matcher);
    }

    protected <R> R eq(R value)
    {
        record(ArgumentMatcherUtils.eq(value));
        return value;
    }

    protected <R> R isInstance(Class<R> type)
    {
        record(ArgumentMatcherUtils.isA(type));
        return ProxyUtils.nullValue(type);
    }

    protected void thenThrow(Exception e)
    {
        trainingContext().then(InterceptorUtils.throwing(e));
    }

    protected void thenThrow(ObjectProvider<? extends Exception> provider)
    {
        trainingContext().then(InterceptorUtils.throwing(provider));
    }

    protected TrainingContext trainingContext()
    {
        return TrainingContext.getCurrent();
    }

    protected <R> WhenObject<R> when(R expression)
    {
        return new WhenObject<R>();
    }

    protected WhenClass when(Class<?> expression)
    {
        return new WhenClass();
    }

    protected WhenByteArray when(byte[] expression)
    {
        return new WhenByteArray();
    }

    protected WhenBooleanArray when(boolean[] expression)
    {
        return new WhenBooleanArray();
    }

    protected WhenIntArray when(int[] expression)
    {
        return new WhenIntArray();
    }

    protected WhenShortArray when(short[] expresssion)
    {
        return new WhenShortArray();
    }

    protected WhenLongArray when(long[] expression)
    {
        return new WhenLongArray();
    }

    protected WhenFloatArray when(float[] expression)
    {
        return new WhenFloatArray();
    }

    protected WhenDoubleArray when(double[] expression)
    {
        return new WhenDoubleArray();
    }

    protected <R> WhenObjectArray<R> when(R[] expression)
    {
        return new WhenObjectArray<R>();
    }

    protected WhenCharArray when(char[] expression)
    {
        return new WhenCharArray();
    }

    @SuppressWarnings("unchecked")
    protected S self()
    {
        return (S) this;
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    protected abstract class BaseWhen<R>
    {
        protected S thenThrow(Exception e)
        {
            trainingContext().then(InterceptorUtils.throwing(e));
            return self();
        }

        protected S thenThrow(ObjectProvider<? extends Exception> provider)
        {
            trainingContext().then(InterceptorUtils.throwing(provider));
            return self();
        }

        protected S thenAnswer(ObjectProvider<? extends R> provider)
        {
            trainingContext().then(InterceptorUtils.provider(provider));
            return self();
        }
    }

    protected class WhenBooleanArray extends BaseWhen<boolean[]>
    {
        protected S thenReturn(boolean... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenByteArray extends BaseWhen<byte[]>
    {
        protected S thenReturn(byte... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenCharArray extends BaseWhen<char[]>
    {
        protected S thenReturn(char... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenDoubleArray extends BaseWhen<double[]>
    {
        protected S thenReturn(double... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenFloatArray extends BaseWhen<float[]>
    {
        protected S thenReturn(float... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenIntArray extends BaseWhen<int[]>
    {
        protected S thenReturn(int... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenLongArray extends BaseWhen<long[]>
    {
        protected S thenReturn(long... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenObject<R> extends BaseWhen<R>
    {
        protected S thenReturn(R value)
        {
            trainingContext().then(InterceptorUtils.constant(value));
            return self();
        }

        protected S thenStub(BaseTrainer<?, R> trainer)
        {
            final R trainee = trainingContext().push(trainer.traineeType);
            trainer.train(trainee);
            trainingContext().then(InterceptorUtils.constant(trainingContext().pop()));
            return self();
        }
    }

    protected class WhenClass extends BaseWhen<Class<?>>
    {
        protected S thenReturn(Class<?> value)
        {
            trainingContext().then(InterceptorUtils.constant(value));
            return self();
        }
    }

    protected class WhenObjectArray<R> extends BaseWhen<R[]>
    {
        protected S thenReturn(R... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenShortArray extends BaseWhen<short[]>
    {
        protected S thenReturn(short... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }
}
