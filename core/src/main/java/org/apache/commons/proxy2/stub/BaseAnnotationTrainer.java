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

public abstract class BaseAnnotationTrainer<S extends BaseAnnotationTrainer<S, A>, A extends Annotation> extends
        BaseTrainer<S, A>
{
    protected BaseAnnotationTrainer()
    {
        super();
    }

    protected BaseAnnotationTrainer(Class<A> traineeType)
    {
        super(traineeType);
    }

    protected class WhenAnnotation<R> extends WhenObject<R>
    {
        public S thenStub(Class<R> type)
        {
            trainingContext().push(type);
            trainingContext().then(InterceptorUtils.constant(trainingContext().pop(AnnotationInvoker.INSTANCE)));
            return self();
        }

        @Override
        public S thenStub(BaseTrainer<?, R> trainer)
        {
            final R trainee = trainingContext().push(trainer.traineeType);
            trainer.train(trainee);
            trainingContext().then(InterceptorUtils.constant(trainingContext().pop(AnnotationInvoker.INSTANCE)));
            return self();
        }
    }

    protected class WhenAnnotationArray<R> extends WhenObjectArray<R>
    {
        protected WhenAnnotationArray(Class<? extends R> componentType)
        {
            super(componentType);
        }

        @Override
        public StubAnnotationArrayBuilder<R> thenBuildArray()
        {
            return new StubAnnotationArrayBuilder<R>(componentType);
        }
    }

    protected class StubAnnotationArrayBuilder<R> extends StubArrayBuilder<R>
    {
        private final BaseTrainer<?, R> annotationTypeTrainer;

        private <N extends Annotation> StubAnnotationArrayBuilder(final Class<? extends R> componentType)
        {
            super(componentType);
            
            /*
             * We know the only type of array method that can be hosted on an annotation is an annotation array.
             * Therefore we declare a bogus annotation type parameter on this method which we use to create
             * our AnnotationTypeTrainer, whose type parameter requires an annotation type. N == R
             */
            @SuppressWarnings("unchecked") // we assume N == R
            final Class<N> annotationType = (Class<N>) componentType;
            @SuppressWarnings("unchecked") // and cast it back
            final BaseTrainer<?, R> trainer = (BaseTrainer<?, R>) new AnnotationTypeTrainer<N>(
                    annotationType);
            this.annotationTypeTrainer = trainer;
        }

        @Override
        public StubAnnotationArrayBuilder<R> addElement(BaseTrainer<?, R> trainer)
        {
            final R trainee = trainingContext().push(trainer.traineeType);

            annotationTypeTrainer.train(trainee);
            trainer.train(trainee);

            elements.add(trainingContext().<R> pop());
            return this;
        }
    }

    @Override
    public <R> WhenAnnotation<R> when(R expression)
    {
        return new WhenAnnotation<R>();
    }

    @Override
    public <R> WhenAnnotationArray<R> when(R[] expression)
    {
        @SuppressWarnings("unchecked") // we can reasonably say that the component type of an R[] is Class<? extends R>:
        final Class<? extends R> componentType = (Class<? extends R>) expression.getClass().getComponentType();
        return new WhenAnnotationArray<R>(componentType);
    }
}
