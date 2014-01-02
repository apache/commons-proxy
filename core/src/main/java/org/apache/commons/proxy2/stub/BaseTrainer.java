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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.interceptor.InterceptorUtils;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.apache.commons.proxy2.interceptor.matcher.argument.ArgumentMatcherUtils;

public abstract class BaseTrainer<S extends BaseTrainer<S, T>, T>
{
    // ----------------------------------------------------------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------------------------------------------------------
    public final Class<T> traineeType;

    // ----------------------------------------------------------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------------------------------------------------------

    /**
     * Create a new {@link BaseTrainer} instance. This constructor should only
     * be called by classes that explicitly assign the T parameter in the class
     * definition. This should include basically any runtime-usable class.
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
        final Class<T> resolvedVariable = (Class<T>) TypeUtils.getRawType(BaseTrainer.class.getTypeParameters()[1],
                getClass());
        Validate.isTrue(resolvedVariable != null, "Trainee type was not specified and could not be calculated for %s",
                getClass());
        this.traineeType = resolvedVariable;
    }

    // ----------------------------------------------------------------------------------------------------------------------
    // Abstract Methods
    // ----------------------------------------------------------------------------------------------------------------------

    protected abstract void train(T trainee);

    // ----------------------------------------------------------------------------------------------------------------------
    // Other Methods
    // ----------------------------------------------------------------------------------------------------------------------

    protected <R> R any(Class<R> type)
    {
        return argThat(ArgumentMatcherUtils.<R> any());
    }

    protected <R> R eq(R value)
    {
        return argThat(ArgumentMatcherUtils.eq(value));
    }

    protected <R> R isInstance(Class<R> type)
    {
        return argThat(ArgumentMatcherUtils.<R> isA(type));
    }

    protected <R> R argThat(ArgumentMatcher<R> matcher)
    {
        trainingContext().record(matcher);
        return null;
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

    public <R> WhenObject<R> when(R expression)
    {
        return new WhenObject<R>();
    }

    public WhenClass when(Class<?> expression)
    {
        return new WhenClass();
    }

    public WhenByteArray when(byte[] expression)
    {
        return new WhenByteArray();
    }

    public WhenBooleanArray when(boolean[] expression)
    {
        return new WhenBooleanArray();
    }

    public WhenIntArray when(int[] expression)
    {
        return new WhenIntArray();
    }

    public WhenShortArray when(short[] expresssion)
    {
        return new WhenShortArray();
    }

    public WhenLongArray when(long[] expression)
    {
        return new WhenLongArray();
    }

    public WhenFloatArray when(float[] expression)
    {
        return new WhenFloatArray();
    }

    public WhenDoubleArray when(double[] expression)
    {
        return new WhenDoubleArray();
    }

    public <R> WhenObjectArray<R> when(R[] expression)
    {
        @SuppressWarnings("unchecked")
        final Class<? extends R> componentType = (Class<? extends R>) expression.getClass().getComponentType();
        return new WhenObjectArray<R>(componentType);
    }

    public WhenCharArray when(char[] expression)
    {
        return new WhenCharArray();
    }

    @SuppressWarnings("unchecked")
    protected S self()
    {
        return (S) this;
    }

    // ----------------------------------------------------------------------------------------------------------------------
    // Inner Classes
    // ----------------------------------------------------------------------------------------------------------------------

    protected abstract class BaseWhen<R>
    {
        public S thenThrow(Exception e)
        {
            return then(InterceptorUtils.throwing(e));
        }

        public S thenThrow(ObjectProvider<? extends Exception> provider)
        {
            return then(InterceptorUtils.throwing(provider));
        }

        public S thenAnswer(ObjectProvider<? extends R> provider)
        {
            return then(InterceptorUtils.provider(provider));
        }

        public S then(Interceptor interceptor)
        {
            trainingContext().then(interceptor);
            return self();
        }
    }

    protected class WhenBooleanArray extends BaseWhen<boolean[]>
    {
        public S thenReturn(boolean... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenByteArray extends BaseWhen<byte[]>
    {
        public S thenReturn(byte... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenCharArray extends BaseWhen<char[]>
    {
        public S thenReturn(char... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenDoubleArray extends BaseWhen<double[]>
    {
        public S thenReturn(double... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenFloatArray extends BaseWhen<float[]>
    {
        public S thenReturn(float... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenIntArray extends BaseWhen<int[]>
    {
        public S thenReturn(int... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenLongArray extends BaseWhen<long[]>
    {
        public S thenReturn(long... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }

    protected class WhenObject<R> extends BaseWhen<R>
    {
        public S thenReturn(R value)
        {
            trainingContext().then(InterceptorUtils.constant(value));
            return self();
        }

        public S thenStub(BaseTrainer<?, R> trainer)
        {
            final R trainee = trainingContext().push(trainer.traineeType);
            trainer.train(trainee);
            trainingContext().then(InterceptorUtils.constant(trainingContext().pop()));
            return self();
        }
    }

    /**
     * Intermediate result of a when(Class) call. Provided because it is such a
     * common case to have a mismatch between a declared Class<?> return type
     * and the bound parameter of a class literal.
     */
    protected class WhenClass extends BaseWhen<Class<?>>
    {
        public S thenReturn(Class<?> value)
        {
            trainingContext().then(InterceptorUtils.constant(value));
            return self();
        }
    }

    protected class WhenObjectArray<R> extends BaseWhen<R[]>
    {
        protected final Class<? extends R> componentType;

        protected WhenObjectArray(Class<? extends R> componentType)
        {
            this.componentType = componentType;
        }

        public S thenReturn(R... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }

        public StubArrayBuilder<R> thenBuildArray()
        {
            return new StubArrayBuilder<R>(componentType);
        }
    }

    protected class StubArrayBuilder<R>
    {
        protected final List<R> elements = new ArrayList<R>();
        protected final Class<? extends R> componentType;

        protected StubArrayBuilder(Class<? extends R> componentType)
        {
            this.componentType = componentType;
        }

        public StubArrayBuilder<R> addElement(BaseTrainer<?, R> trainer)
        {
            final R trainee = trainingContext().push(trainer.traineeType);
            trainer.train(trainee);
            elements.add(trainingContext().<R> pop());
            return this;
        }

        public S build()
        {
            @SuppressWarnings("unchecked")
            final R[] array = elements.toArray((R[]) Array.newInstance(componentType, elements.size()));
            trainingContext().then(InterceptorUtils.constant(array));
            return self();
        }
    }

    protected class WhenShortArray extends BaseWhen<short[]>
    {
        public S thenReturn(short... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return self();
        }
    }
}
