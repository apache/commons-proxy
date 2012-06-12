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

package org.apache.commons.proxy2.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.exception.ObjectProviderException;

import java.util.HashSet;
import java.util.Set;

/**
 * Some utility methods for dealing with Javassist.  This class is not part of the public API!
 *
 * @author James Carman
 * @since 1.0
 */
final class JavassistUtils
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    public static final String DEFAULT_BASE_NAME = "JavassistUtilsGenerated";
    private static int classNumber = 0;
    private static final ClassPool classPool = new ClassPool();

    private static final Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();

//**********************************************************************************************************************
// Static Methods
//**********************************************************************************************************************

    static
    {
        classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
    }

    /**
     * Adds a field to a class.
     *
     * @param fieldType      the field's type
     * @param fieldName      the field name
     * @param enclosingClass the class receiving the new field
     * @throws CannotCompileException if a compilation problem occurs
     */
    public static void addField( Class<?> fieldType, String fieldName, CtClass enclosingClass )
            throws CannotCompileException
    {
        enclosingClass.addField(new CtField(resolve(fieldType), fieldName, enclosingClass));
    }

    /**
     * Adds interfaces to a {@link CtClass}
     *
     * @param ctClass      the {@link CtClass}
     * @param proxyClasses the interfaces
     */
    public static void addInterfaces( CtClass ctClass, Class<?>[] proxyClasses )
    {
        for( int i = 0; i < proxyClasses.length; i++ )
        {
            Class<?> proxyInterface = proxyClasses[i];
            ctClass.addInterface(resolve(proxyInterface));
        }
    }

    /**
     * Creates a new {@link CtClass} derived from the Java {@link Class} using the default base name.
     *
     * @param superclass the superclass
     * @return the new derived {@link CtClass}
     */
    public static CtClass createClass( Class<?> superclass )
    {
        return createClass(DEFAULT_BASE_NAME, superclass);
    }

    /**
     * Creates a new {@link CtClass} derived from the Java {@link Class} using the supplied base name.
     *
     * @param baseName   the base name
     * @param superclass the superclass
     * @return the new derived {@link CtClass}
     */
    public synchronized static CtClass createClass( String baseName, Class<?> superclass )
    {
        return classPool.makeClass(baseName + "_" + classNumber++, resolve(superclass));
    }

    /**
     * Finds the {@link CtClass} corresponding to the Java {@link Class} passed in.
     *
     * @param clazz the Java {@link Class}
     * @return the {@link CtClass}
     */
    public static CtClass resolve( Class<?> clazz )
    {
        synchronized( classLoaders )
        {
            try
            {
                final ClassLoader loader = clazz.getClassLoader();
                if( loader != null && !classLoaders.contains(loader) )
                {
                    classLoaders.add(loader);
                    classPool.appendClassPath(new LoaderClassPath(loader));
                }
                return classPool.get(ProxyUtils.getJavaClassName(clazz));
            }
            catch( NotFoundException e )
            {
                throw new ObjectProviderException(
                        "Unable to find class " + clazz.getName() + " in default Javassist class pool.", e);
            }
        }
    }

    /**
     * Resolves an array of Java {@link Class}es to an array of their corresponding {@link CtClass}es.
     *
     * @param classes the Java {@link Class}es
     * @return the corresponding {@link CtClass}es
     */
    public static CtClass[] resolve( Class<?>[] classes )
    {
        final CtClass[] ctClasses = new CtClass[classes.length];
        for( int i = 0; i < ctClasses.length; ++i )
        {
            ctClasses[i] = resolve(classes[i]);
        }
        return ctClasses;
    }

    private JavassistUtils() {
        // Hiding constructor in utility class!
    }
}
