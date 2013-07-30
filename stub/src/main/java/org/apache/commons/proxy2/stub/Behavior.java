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

public abstract class Behavior<T>
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
        protected Behavior<T> thenThrow(Exception e)
        {
            trainingContext().setInterceptor(InterceptorUtils.throwing(e));
            return Behavior.this;
        }

        protected Behavior<T> thenThrow(ObjectProvider<? extends Exception> provider)
        {
            trainingContext().setInterceptor(InterceptorUtils.throwing(provider));
            return Behavior.this;
        }

        protected <R> Behavior<T> thenAnswer(ObjectProvider<? extends R> provider)
        {
            trainingContext().setInterceptor(InterceptorUtils.provider(provider));
            return Behavior.this;
        }
    }

    private TrainingContext trainingContext()
    {
        return TrainingContext.getTrainingContext();
    }

    protected class WhenBooleanArray extends BaseWhen<boolean[]>
    {
        protected Behavior<T> thenReturn(boolean... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenByteArray extends BaseWhen<byte[]>
    {
        protected Behavior<T> thenReturn(byte... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenCharArray extends BaseWhen<char[]>
    {
        protected Behavior<T> thenReturn(char... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenDoubleArray extends BaseWhen<double[]>
    {
        protected Behavior<T> thenReturn(double... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenFloatArray extends BaseWhen<float[]>
    {
        protected Behavior<T> thenReturn(float... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenIntArray extends BaseWhen<int[]>
    {
        protected Behavior<T> thenReturn(int... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenLongArray extends BaseWhen<long[]>
    {
        protected Behavior<T> thenReturn(long... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenObject<R> extends BaseWhen
    {
        protected Behavior<T> thenReturn(R value)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(value));
            return Behavior.this;
        }
    }

    protected class WhenObjectArray<R> extends BaseWhen<R[]>
    {
        protected Behavior<T> thenReturn(R... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }

    protected class WhenShortArray extends BaseWhen<short[]>
    {
        protected Behavior<T> thenReturn(short... values)
        {
            trainingContext().setInterceptor(InterceptorUtils.constant(ArrayUtils.clone(values)));
            return Behavior.this;
        }
    }
}
