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

import java.lang.annotation.Annotation;

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
        assertNotNull(customAnnotation);
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
        assertNotNull(customAnnotation);
        assertEquals(CustomAnnotation.class, customAnnotation.annotationType());
        assertEquals("hey", customAnnotation.annString());
        assertArrayEquals(new FiniteValues[] { FiniteValues.ONE, FiniteValues.THREE }, customAnnotation.finiteValues());
        assertEquals(Object.class, customAnnotation.someType());
    }

    @Test
    public void testStubbedAnnotationWithDefaultChild() {
        NestingAnnotation nestingAnnotation =
            annotationFactory.create(new AnnotationConfigurer<NestingAnnotation>() {
                @Override
                protected void configure(NestingAnnotation stub) {
                    when(stub.child()).thenReturn(child(CustomAnnotation.class))
                        .when(stub.somethingElse()).thenReturn("somethingElse");
                }
            });
        assertNotNull(nestingAnnotation);
        assertNotNull(nestingAnnotation.child());
        assertEquals("", nestingAnnotation.child().annString());
        assertEquals(0, nestingAnnotation.child().finiteValues().length);
        assertEquals(null, nestingAnnotation.child().someType());
        assertEquals("somethingElse", nestingAnnotation.somethingElse());
    }

    @Test
    public void testStubbedAnnotationWithConfiguredChild() {
        NestingAnnotation nestingAnnotation = annotationFactory.create(new AnnotationConfigurer<NestingAnnotation>() {
            @Override
            protected void configure(NestingAnnotation stub) {
                when(stub.child()).thenReturn(child(new StubConfigurer<CustomAnnotation>() {

                    @Override
                    protected void configure(CustomAnnotation stub) {
                        when(stub.annString()).thenReturn("wow").when(stub.finiteValues())
                            .thenReturn(FiniteValues.values()).when(stub.someType()).thenReturn(Class.class);
                    }
                })).when(stub.somethingElse()).thenReturn("somethingElse");
            }
        });
        assertNotNull(nestingAnnotation);
        assertNotNull(nestingAnnotation.child());
        assertEquals("wow", nestingAnnotation.child().annString());
        assertEquals(3, nestingAnnotation.child().finiteValues().length);
        assertEquals(Class.class, nestingAnnotation.child().someType());
        assertEquals("somethingElse", nestingAnnotation.somethingElse());
    }

    @Test
    public void testStubbedAnnotationWithDoubleNesting() {
        OuterContainer outerContainer = annotationFactory.create(new AnnotationConfigurer<OuterContainer>() {

            @Override
            protected void configure(OuterContainer stub) {
                when(stub.nest()).thenReturn(child(new AnnotationConfigurer<NestingAnnotation>() {

                    @Override
                    protected void configure(NestingAnnotation stub) {
                        when(stub.child()).thenReturn(child(new AnnotationConfigurer<CustomAnnotation>() {

                            @Override
                            protected void configure(CustomAnnotation stub) {
                                when(stub.annString()).thenReturn("wow").when(stub.finiteValues())
                                .thenReturn(FiniteValues.values()).when(stub.someType()).thenReturn(Class.class);
                            }

                        })).when(stub.somethingElse()).thenReturn("somethingElse");
                    }
                }));
            }
        });
        assertNotNull(outerContainer);
        NestingAnnotation nestingAnnotation = outerContainer.nest();
        assertNotNull(nestingAnnotation);
        assertNotNull(nestingAnnotation.child());
        assertEquals("wow", nestingAnnotation.child().annString());
        assertEquals(3, nestingAnnotation.child().finiteValues().length);
        assertEquals(Class.class, nestingAnnotation.child().someType());
        assertEquals("somethingElse", nestingAnnotation.somethingElse());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCannotConfigureOwnChild() {
        annotationFactory.create(new AnnotationConfigurer<NestingAnnotation>() {

            @Override
            protected void configure(NestingAnnotation stub) {
                child(this);
            }
        });
    }

    @Test(expected=IllegalStateException.class)
    public void testChildRequiresOngoingStubbing() {
        new AnnotationConfigurer<Annotation>() {
            {
                child(CustomAnnotation.class);
            }

            @Override
            protected void configure(Annotation stub) {
            }
        };
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

    public @interface OuterContainer {
        NestingAnnotation nest();
    }

    public enum FiniteValues {
        ONE, TWO, THREE;
    }

}
