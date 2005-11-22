/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.proxy.factory.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.apache.commons.proxy.ProxyUtils;
import org.apache.commons.proxy.exception.ObjectProviderException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author James Carman
 * @version 1.0
 */
public class JavassistUtils
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------
    public static final String DEFAULT_BASE_NAME = "JavassistUtilsGenerated";
    private static int classNumber = 0;
    private static final ClassPool classPool = new ClassPool();

    static
    {
        classPool.appendClassPath( new LoaderClassPath( ClassLoader.getSystemClassLoader() ) );
    }

    private static final Set classLoaders = new HashSet();
//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    public static void addField( Class fieldType, String fieldName, CtClass enclosingClass )
            throws CannotCompileException
    {
        enclosingClass.addField( new CtField( resolve( fieldType ), fieldName, enclosingClass ) );
    }

    public static CtClass resolve( Class clazz )
    {
        synchronized( classLoaders )
        {
            try
            {
                final ClassLoader loader = clazz.getClassLoader();
                if( loader != null && !classLoaders.contains( loader ) )
                {
                    classLoaders.add( loader );
                    classPool.appendClassPath( new LoaderClassPath( loader ) );
                }
                return classPool.get( ProxyUtils.getJavaClassName( clazz ) );
            }
            catch( NotFoundException e )
            {
                throw new ObjectProviderException(
                        "Unable to find class " + clazz.getName() + " in default Javassist class pool.", e );
            }
        }
    }

    public static void addInterfaces( CtClass ctClass, Class[] proxyClasses )
    {
        for( int i = 0; i < proxyClasses.length; i++ )
        {
            Class proxyInterface = proxyClasses[i];
            ctClass.addInterface( resolve( proxyInterface ) );
        }
    }

    public static CtClass createClass( Class superclass )
    {
        return createClass( DEFAULT_BASE_NAME, superclass );
    }

    public synchronized static CtClass createClass( String baseName, Class superclass )
    {
        return classPool.makeClass( baseName + "_" + classNumber++, resolve( superclass ) );
    }

    public static CtClass[] resolve( Class[] classes )
    {
        final CtClass[] ctClasses = new CtClass[classes.length];
        for( int i = 0; i < ctClasses.length; ++i )
        {
            ctClasses[i] = resolve( classes[i] );
        }
        return ctClasses;
    }
}

