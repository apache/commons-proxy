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

    protected abstract void train(T stub);

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
        trainingContext().addArgumentMatcher(matcher);
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
        trainingContext().setInterceptor(InterceptorUtils.throwing(e));
    }

    protected void thenThrow(ObjectProvider<? extends Exception> provider)
    {
        trainingContext().setInterceptor(InterceptorUtils.throwing(provider));
    }

    private TrainingContext trainingContext()
    {
        return TrainingContext.getTrainingContext();
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
        protected Trainer<T> thenThrow(Exception e)
        {
            trainingContext().setInterceptor(InterceptorUtils.throwing(e));
            return Trainer.this;
        }

        protected Trainer<T> thenThrow(ObjectProvider<? extends Exception> provider)
        {
            trainingContext().setInterceptor(InterceptorUtils.throwing(provider));
            return Trainer.this;
        }

        protected <R> Trainer<T> thenAnswer(ObjectProvider<? extends R> provider)
        {
            trainingContext().setInterceptor(InterceptorUtils.provider(provider));
            return Trainer.this;
        }
    }

    protected class WhenBooleanArray extends BaseWhen<boolean[]>
    {
        protected Trainer<T> thenReturn(boolean... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenByteArray extends BaseWhen<byte[]>
    {
        protected Trainer<T> thenReturn(byte... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenCharArray extends BaseWhen<char[]>
    {
        protected Trainer<T> thenReturn(char... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenDoubleArray extends BaseWhen<double[]>
    {
        protected Trainer<T> thenReturn(double... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenFloatArray extends BaseWhen<float[]>
    {
        protected Trainer<T> thenReturn(float... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenIntArray extends BaseWhen<int[]>
    {
        protected Trainer<T> thenReturn(int... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenLongArray extends BaseWhen<long[]>
    {
        protected Trainer<T> thenReturn(long... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenObject<R> extends BaseWhen
    {
        protected Trainer<T> thenReturn(R value)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(value));
            return Trainer.this;
        }
    }

    protected class WhenObjectArray<R> extends BaseWhen<R[]>
    {
        protected Trainer<T> thenReturn(R... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }

    protected class WhenShortArray extends BaseWhen<short[]>
    {
        protected Trainer<T> thenReturn(short... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Trainer.this;
        }
    }
}