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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;

/**
 * Special {@link StubConfigurer} subclass that makes creating nested annotations (somewhat more) convenient.
 *
 * @param <A>
 */
public abstract class AnnotationConfigurer<A extends Annotation> extends StubConfigurer<A> {
    /**
     * Create a new {@link AnnotationConfigurer} instance.
     */
    protected AnnotationConfigurer() {
        super();
    }

    /**
     * Create a new {@link AnnotationConfigurer} instance.
     * @param type
     */
    protected AnnotationConfigurer(Class<A> type) {
        super(type);
    }

    /**
     * Create a child annotation of the specified type using a StubConfigurer.
     * @param <T>
     * @param configurer, should not be <code>this</code>
     * @return T
     * @throws IllegalStateException if called other than when an {@link AnnotationFactory} is executing {@link #configure(Object)}
     * @throws IllegalArgumentException if <code>configurer == this</code>
     */
    protected final <T extends Annotation> T child(StubConfigurer<T> configurer) {
        if (configurer == this) {
            throw new IllegalArgumentException("An AnnotationConfigurer cannot configure its own child annotation");
        }
        ImmutablePair<AnnotationFactory, ClassLoader> context = requireContext();
        return context.left.create(context.right, configurer);
    }

    /**
     * Create a child annotation of the specified type with default behavior.
     * @param <T>
     * @param annotationType
     * @return T
     * @throws IllegalStateException if called other than when an {@link AnnotationFactory} is executing {@link #configure(Object)}
     */
    protected final <T extends Annotation> T child(Class<T> annotationType) {
        ImmutablePair<AnnotationFactory, ClassLoader> context = requireContext();
        return context.left.create(context.right, annotationType);
    }

    /**
     * Get the registered {@link AnnotationFactory}/{@link ClassLoader}.
     * @return a {@link Pair}
     * @throws IllegalStateException if no ongoing annotation stubbing could be detected
     */
    synchronized ImmutablePair<AnnotationFactory, ClassLoader> requireContext() throws IllegalStateException {
        ImmutablePair<AnnotationFactory, ClassLoader> result = AnnotationFactory.CONTEXT.get();
        if (result == null) {
            throw new IllegalStateException("Could not detect ongoing annotation stubbing");
        }
        return result;
    }

}