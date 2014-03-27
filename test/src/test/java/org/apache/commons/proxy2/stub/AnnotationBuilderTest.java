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
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test {@link AnnotationBuilder}.
 */
public class AnnotationBuilderTest
{
    @Test
    public void testDefaultAnnotation()
    {
        final CustomAnnotation customAnnotation = AnnotationBuilder.buildDefault(CustomAnnotation.class);
        assertEquals(CustomAnnotation.class, customAnnotation.annotationType());
        assertEquals("", customAnnotation.annString());
        assertEquals(0, customAnnotation.finiteValues().length);
        assertNull(customAnnotation.someType());
    }

    @Test
    public void testStubbedAnnotation()
    {
        final CustomAnnotation customAnnotation = AnnotationBuilder.of(CustomAnnotation.class)
                .train(new AnnotationTrainer<CustomAnnotation>()
                {
                    @Override
                    protected void train(CustomAnnotation trainee)
                    {
                        when(trainee.someType()).thenReturn(Object.class).when(trainee.finiteValues())
                                .thenReturn(FiniteValues.ONE, FiniteValues.THREE).when(trainee.annString())
                                .thenReturn("hey");
                    }
                }).build();

        assertEquals(CustomAnnotation.class, customAnnotation.annotationType());
        assertEquals("hey", customAnnotation.annString());
        assertArrayEquals(new FiniteValues[] { FiniteValues.ONE, FiniteValues.THREE }, customAnnotation.finiteValues());
        assertEquals(Object.class, customAnnotation.someType());
    }

    @Test
    public void testNestedStubbedAnnotation()
    {
        final NestingAnnotation nestingAnnotation = AnnotationBuilder.of(NestingAnnotation.class)
                .train(new AnnotationTrainer<NestingAnnotation>()
                {
                    @Override
                    protected void train(NestingAnnotation trainee)
                    {
                        when(trainee.child()).thenStub(CustomAnnotation.class).when(trainee.somethingElse())
                                .thenReturn("somethingElse");
                    }
                }).build();

        assertEquals("", nestingAnnotation.child().annString());
        assertEquals(0, nestingAnnotation.child().finiteValues().length);
        assertEquals(null, nestingAnnotation.child().someType());
        assertEquals("somethingElse", nestingAnnotation.somethingElse());
    }

    @Test
    public void testMemberMap()
    {
        final Map<String, Object> members = new HashMap<String, Object>();
        members.put("annString", "foo");
        members.put("finiteValues", FiniteValues.values());
        members.put("someType", Object.class);

        final CustomAnnotation customAnnotation = AnnotationBuilder.of(CustomAnnotation.class).withMembers(members)
                .build();

        assertNotNull(customAnnotation);
        assertEquals(CustomAnnotation.class, customAnnotation.annotationType());
        assertEquals("foo", customAnnotation.annString());
        assertEquals(3, customAnnotation.finiteValues().length);
        assertEquals(Object.class, customAnnotation.someType());
    }

    @Test
    public void testNestedStubbedAnnotationArray()
    {
        final NestingAnnotation nestingAnnotation = AnnotationBuilder.of(NestingAnnotation.class)
                .train(new AnnotationTrainer<NestingAnnotation>()
                {

                    @Override
                    protected void train(NestingAnnotation trainee)
                    {
                        when(trainee.children()).thenBuildArray().addElement(new AnnotationTrainer<CustomAnnotation>()
                        {
                            @Override
                            protected void train(CustomAnnotation trainee)
                            {
                                when(trainee.finiteValues()).thenReturn(FiniteValues.ONE, FiniteValues.THREE);
                            }
                        }).addElement(new AnnotationTrainer<CustomAnnotation>()
                        {
                            @Override
                            protected void train(CustomAnnotation trainee)
                            {
                                when(trainee.finiteValues()).thenReturn(FiniteValues.TWO);
                            }
                        }).build();
                    }
                }).build();

        assertNull(nestingAnnotation.child());
        assertEquals(2, nestingAnnotation.children().length);
        assertArrayEquals(new FiniteValues[] { FiniteValues.ONE, FiniteValues.THREE },
                nestingAnnotation.children()[0].finiteValues());
        assertArrayEquals(new FiniteValues[] { FiniteValues.TWO }, nestingAnnotation.children()[1].finiteValues());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadMemberMap()
    {
        AnnotationBuilder.of(CustomAnnotation.class).withMembers(
                Collections.singletonMap("annString", Integer.valueOf(100)));
    }

    public @interface NestingAnnotation
    {
        CustomAnnotation child();

        String somethingElse();

        CustomAnnotation[] children() default {};
    }

    public @interface CustomAnnotation
    {
        String annString() default "";

        FiniteValues[] finiteValues() default {};

        Class<?> someType();
    }

    public enum FiniteValues
    {
        ONE, TWO, THREE;
    }

}