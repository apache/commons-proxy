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

import java.lang.annotation.Annotation;

import org.apache.commons.proxy2.interceptor.InterceptorUtils;

public abstract class BaseAnnotationTrainer<S extends BaseAnnotationTrainer<S, A>, A extends Annotation> extends BaseTrainer<S, A>
{
    protected BaseAnnotationTrainer() {
        super();
    }

    protected BaseAnnotationTrainer(Class<A> traineeType) {
        super(traineeType);
    }

    protected class WhenAnnotation<R> extends WhenObject<R>
    {
        protected S thenStub(Class<R> type) {
            trainingContext().push(type);
            trainingContext().then(InterceptorUtils.constant(trainingContext().pop(AnnotationInvoker.INSTANCE)));
            return self();
        }

        @Override
        protected S thenStub(BaseTrainer<?, R> trainer) {
            final R trainee = trainingContext().push(trainer.traineeType);
            trainer.train(trainee);
            trainingContext().then(InterceptorUtils.constant(trainingContext().pop(AnnotationInvoker.INSTANCE)));
            return self();
        }
    }

    @Override
    protected <R> WhenAnnotation<R> when(R expression) {
        return new WhenAnnotation<R>();
    }

}
