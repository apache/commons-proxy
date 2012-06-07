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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link AnnotationFactory}.
 */
public class AnnotationFactoryTest {
    @CustomAnnotation(annString = "FOO", finiteValues = { FiniteValues.ONE, FiniteValues.TWO, FiniteValues.THREE }, someType = Object.class)
    private AnnotationFactory annotationFactory;

    @Before
    public void setUp() {
        annotationFactory = new AnnotationFactory();
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultAnnotation() {
        annotationFactory.create(CustomAnnotation.class);
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
        assertEquals(NestingAnnotation.class, nestingAnnotation.annotationType());
        assertNotNull(nestingAnnotation.child());
        assertEquals(CustomAnnotation.class, nestingAnnotation.child().annotationType());
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
        assertEquals(NestingAnnotation.class, nestingAnnotation.annotationType());
        assertNotNull(nestingAnnotation.child());
        assertEquals(CustomAnnotation.class, nestingAnnotation.child().annotationType());
        assertEquals("wow", nestingAnnotation.child().annString());
        assertEquals(3, nestingAnnotation.child().finiteValues().length);
        assertEquals(Class.class, nestingAnnotation.child().someType());
        assertEquals("somethingElse", nestingAnnotation.somethingElse());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotConfigureOwnChild() {
        annotationFactory.create(new AnnotationConfigurer<NestingAnnotation>() {

            @Override
            protected void configure(NestingAnnotation stub) {
                child(this);
            }
        });
    }

    @Test(expected = IllegalStateException.class)
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

    @Test
    public void testAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("annString", "foo");
        attributes.put("finiteValues", FiniteValues.values());
        attributes.put("someType", Object.class);
        CustomAnnotation customAnnotation = annotationFactory.create(CustomAnnotation.class, attributes);
        assertNotNull(customAnnotation);
        assertEquals(CustomAnnotation.class, customAnnotation.annotationType());
        assertEquals("foo", customAnnotation.annString());
        assertEquals(3, customAnnotation.finiteValues().length);
        assertEquals(Object.class, customAnnotation.someType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("annString", 100);
        annotationFactory.create(CustomAnnotation.class, attributes);
    }

    @Test
    public void testDelegator() {
        final boolean forceAccess = true;
        final CustomAnnotation sourceAnnotation =
            FieldUtils.getDeclaredField(AnnotationFactoryTest.class, "annotationFactory", forceAccess).getAnnotation(
                CustomAnnotation.class);
        assertNotNull(sourceAnnotation);
        CustomAnnotation stub =
            annotationFactory.createDelegator(sourceAnnotation, new AnnotationConfigurer<CustomAnnotation>() {

                @Override
                protected void configure(CustomAnnotation stub) {
                    when(stub.finiteValues()).thenReturn(FiniteValues.ONE);
                }
            });
        assertEquals(CustomAnnotation.class, stub.annotationType());
        assertEquals(Object.class, stub.someType());
        assertEquals("FOO", stub.annString());
        assertArrayEquals(new FiniteValues[] { FiniteValues.ONE }, stub.finiteValues());
    }

    @Test(expected = NullPointerException.class)
    public void testDelegatorMissingTarget() {
        annotationFactory.createDelegator(null, new StubConfigurer<CustomAnnotation>() {

            @Override
            protected void configure(CustomAnnotation stub) {
            }
        });
    }

    @Test
    public void testDelegatorWithAttributes() {
        final boolean forceAccess = true;
        final CustomAnnotation sourceAnnotation =
            FieldUtils.getDeclaredField(AnnotationFactoryTest.class, "annotationFactory", forceAccess).getAnnotation(
                CustomAnnotation.class);
        assertNotNull(sourceAnnotation);
        Map<String, Object> attributes =
            Collections.<String, Object> singletonMap("finiteValues", new FiniteValues[] { FiniteValues.ONE });
        CustomAnnotation stub = annotationFactory.createDelegator(sourceAnnotation, attributes);
        assertEquals(CustomAnnotation.class, stub.annotationType());
        assertEquals(Object.class, stub.someType());
        assertEquals("FOO", stub.annString());
        assertArrayEquals(new FiniteValues[] { FiniteValues.ONE }, stub.finiteValues());
    }

    @Test(expected = NullPointerException.class)
    public void testDelegatorWithAttributesMissingTarget() {
        annotationFactory.createDelegator(null, Collections.<String, Object> emptyMap());
    }

    public @interface NestingAnnotation {
        CustomAnnotation child();

        String somethingElse();
    }

    @Retention(RetentionPolicy.RUNTIME)
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
