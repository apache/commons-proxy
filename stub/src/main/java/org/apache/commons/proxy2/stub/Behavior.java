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
import org.apache.commons.proxy2.interceptor.InterceptorUtils;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.apache.commons.proxy2.interceptor.matcher.argument.ArgumentMatcherUtils;

public abstract class Behavior<T>
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private TrainingContext trainingContext;

//----------------------------------------------------------------------------------------------------------------------
// Abstract Methods
//----------------------------------------------------------------------------------------------------------------------

    protected abstract void train(T stub);

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    protected <R> R any()
    {
        recordArgumentMatcher(ArgumentMatcherUtils.any());
        return null;
    }

    private void recordArgumentMatcher(ArgumentMatcher matcher)
    {
        trainingContext.addArgumentMatcher(matcher);
    }

    protected <R> R eq(R value)
    {
        recordArgumentMatcher(ArgumentMatcherUtils.eq(value));
        return null;
    }

    protected <R> R isInstance(Class<R> type)
    {
        recordArgumentMatcher(ArgumentMatcherUtils.isInstance(type));
        return null;
    }

    void train(TrainingContext context, T stub)
    {
        this.trainingContext = context;
        train(stub);
    }

    protected <R> When<R> when(R expression)
    {
        return new When();
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    protected class When<R>
    {
        protected Behavior<T> thenReturn(R value)
        {
            trainingContext.setInterceptor(InterceptorUtils.constant(value));
            return Behavior.this;
        }

        protected Behavior<T> thenReturn(ObjectProvider<R> provider)
        {
            trainingContext.setInterceptor(InterceptorUtils.provider(provider));
            return Behavior.this;
        }
    }
}
