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
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.interceptor.InterceptorUtils;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.apache.commons.proxy2.interceptor.matcher.argument.ArgumentMatcherUtils;

public abstract class Trainer<T>
{
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

    private void record(ArgumentMatcher matcher)
    {
        trainingContext().record(matcher);
    }

    protected <R> R eq(R value)
    {
        record(ArgumentMatcherUtils.eq(value));
        return value;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getTraineeType()
    {
        return (Class<T>) TypeUtils.getRawType(Trainer.class.getTypeParameters()[0], getClass());
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

    private TrainingContext trainingContext()
    {
        return TrainingContext.getCurrent();
    }

    protected <R> WhenObject<R> when(R expression)
    {
        return new WhenObject<R>();
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

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    protected abstract class BaseWhen<R>
    {
        protected Trainer<T> thenStub(Trainer<R> trainer)
        {
            R trainee = trainingContext().push(trainer.getTraineeType());
            trainer.train(trainee);
            trainingContext().then(InterceptorUtils.constant(trainingContext().pop()));
            return Trainer.this;
        }

        protected Trainer<T> thenThrow(Exception e)
        {
            trainingContext().then(InterceptorUtils.throwing(e));
            return Trainer.this;
        }

        protected Trainer<T> thenThrow(ObjectProvider<? extends Exception> provider)
        {
            trainingContext().then(InterceptorUtils.throwing(provider));
            return Trainer.this;
        }

        protected <R> Trainer<T> thenAnswer(ObjectProvider<? extends R> provider)
        {
            trainingContext().then(InterceptorUtils.provider(provider));
            return Trainer.this;
        }
    }

    protected class WhenBooleanArray extends BaseWhen<boolean[]>
    {
        protected Trainer<T> thenReturn(boolean... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenByteArray extends BaseWhen<byte[]>
    {
        protected Trainer<T> thenReturn(byte... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenCharArray extends BaseWhen<char[]>
    {
        protected Trainer<T> thenReturn(char... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenDoubleArray extends BaseWhen<double[]>
    {
        protected Trainer<T> thenReturn(double... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenFloatArray extends BaseWhen<float[]>
    {
        protected Trainer<T> thenReturn(float... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenIntArray extends BaseWhen<int[]>
    {
        protected Trainer<T> thenReturn(int... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenLongArray extends BaseWhen<long[]>
    {
        protected Trainer<T> thenReturn(long... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenObject<R> extends BaseWhen
    {
        protected Trainer<T> thenReturn(R value)
        {
            trainingContext().then(InterceptorUtils.constant(value));
            return Trainer.this;
        }
    }

    protected class WhenObjectArray<R> extends BaseWhen<R[]>
    {
        protected Trainer<T> thenReturn(R... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenShortArray extends BaseWhen<short[]>
    {
        protected Trainer<T> thenReturn(short... values)
        {
            trainingContext().then(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }
}
