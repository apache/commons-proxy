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

/**
 * {@link StubConfigurer} for annotation types:  handles <code>{@link Annotation#annotationType()}</code>.
 */
public class AnnotationStubConfigurer<T extends Annotation> extends
        StubConfigurer<T> {

    /**
     * Create a new AnnotationStubConfigurer instance.
     */
    public AnnotationStubConfigurer() {
        super();
    }

    /**
     * Create a new AnnotationStubConfigurer instance.
     */
    public AnnotationStubConfigurer(Class<T> stubType) {
        super(stubType);
    }

    /**
     * {@inheritDoc}
     */
    protected final void configure(T stub) {
        when(stub.annotationType()).thenReturn(getStubType());
        configureAnnotation(stub);
    };

    /**
     * Configure stub as usual. Default implementation is a noop.
     * @param stub
     */
    protected void configureAnnotation(T stub) {
    }
}
