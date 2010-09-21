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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link AnnotationFactory}.
 */
public class AnnotationFactoryTest {
    private AnnotationFactory annotationFactory;

    @Before
    public void setUp() {
        annotationFactory = new AnnotationFactory();
    }

    @Test
    public void testDefaultAnnotation() {
        CustomAnnotation customAnnotation = annotationFactory.create(CustomAnnotation.class);
        assertEquals(CustomAnnotation.class, customAnnotation.annotationType());
        assertEquals("", customAnnotation.annString());
        assertEquals(0, customAnnotation.finiteValues().length);
        assertNull(customAnnotation.someType());

    }

    @Test
    public void testStubbedAnnotation() {
        CustomAnnotation customAnnotation = annotationFactory.create(new StubConfigurer<CustomAnnotation>() {

            @Override
            protected void configure(CustomAnnotation stub) {
                when(stub.someType()).thenReturn(Object.class).when(stub.finiteValues())
                    .thenReturn(FiniteValues.ONE, FiniteValues.THREE).when(stub.annString()).thenReturn("hey");
            }

        });
        assertEquals(CustomAnnotation.class, customAnnotation.annotationType());
        assertEquals("hey", customAnnotation.annString());
        assertArrayEquals(new FiniteValues[] { FiniteValues.ONE, FiniteValues.THREE }, customAnnotation.finiteValues());
        assertEquals(Object.class, customAnnotation.someType());
    }

    @Test
    public void testNestedStubbedAnnotation() {
        NestingAnnotation nestingAnnotation =
            annotationFactory.create(new StubConfigurer<NestingAnnotation>() {
                @Override
                protected void configure(NestingAnnotation stub) {
                    when(stub.child()).thenReturn(annotationFactory.create(CustomAnnotation.class))
                        .when(stub.somethingElse()).thenReturn("somethingElse");
                }
            });
        assertEquals("", nestingAnnotation.child().annString());
        assertEquals(0, nestingAnnotation.child().finiteValues().length);
        assertEquals(null, nestingAnnotation.child().someType());
        assertEquals("somethingElse", nestingAnnotation.somethingElse());
    }

    public @interface NestingAnnotation {
        CustomAnnotation child();

        String somethingElse();
    }

    public @interface CustomAnnotation {
        String annString() default "";

        FiniteValues[] finiteValues() default {};

        Class<?> someType();
    }

    public enum FiniteValues {
        ONE, TWO, THREE;
    }

}
