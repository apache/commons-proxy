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
package org.apache.commons.proxy2.asm4;

import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.exception.ProxyFactoryException;
import org.apache.commons.proxy2.impl.AbstractProxyClassGenerator;
import org.apache.commons.proxy2.impl.AbstractSubclassingProxyFactory;
import org.apache.commons.proxy2.impl.ProxyClassCache;
import org.apache.xbean.asm4.ClassWriter;
import org.apache.xbean.asm4.Label;
import org.apache.xbean.asm4.MethodVisitor;
import org.apache.xbean.asm4.Opcodes;
import org.apache.xbean.asm4.Type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;

public class ASM4ProxyFactory extends AbstractSubclassingProxyFactory
{
	private static final ProxyClassCache PROXY_CLASS_CACHE = new ProxyClassCache(new ProxyGenerator());

    @Override
    public <T> T createDelegatorProxy(final ClassLoader classLoader, final ObjectProvider<?> delegateProvider, final Class<?>... proxyClasses)
    {
        return createProxy(classLoader, new DelegatorInvocationHandler(delegateProvider), proxyClasses);
    }

    @Override
    public <T> T createInterceptorProxy(final ClassLoader classLoader, final Object target, final Interceptor interceptor, final Class<?>... proxyClasses)
    {
        return createProxy(classLoader, new InterceptorInvocationHandler(target, interceptor), proxyClasses);
    }

    @Override
    public <T> T createInvokerProxy(final ClassLoader classLoader, final Invoker invoker, final Class<?>... proxyClasses)
    {
        return createProxy(classLoader, new InvokerInvocationHandler(invoker), proxyClasses);
    }

    private <T> T createProxy(final ClassLoader classLoader, InvocationHandler invocationHandler, final Class<?>... proxyClasses)
    {
    	final Class<?> proxyClass = PROXY_CLASS_CACHE.getProxyClass(classLoader, proxyClasses);
    	return ProxyGenerator.constructProxy(proxyClass, invocationHandler);
    }

    private static class ProxyGenerator extends AbstractProxyClassGenerator implements Opcodes
    {
        private static final AtomicInteger CLASS_NUMBER = new AtomicInteger(0);
        private static final String CLASSNAME_PREFIX = "CommonsProxyASM4_";
        private static final String HANDLER_NAME = "__handler";

        @Override
        public Class<?> generateProxyClass(final ClassLoader classLoader, final Class<?>... proxyClasses)
        {
        	final Class<?> superclass = getSuperclass(proxyClasses);
        	final String proxyName = CLASSNAME_PREFIX + CLASS_NUMBER.incrementAndGet();
			final Method[] implementationMethods = getImplementationMethods(proxyClasses);
			final Class<?>[] interfaces = toInterfaces(proxyClasses);
			final String classFileName = proxyName.replace('.', '/');

			try
			{
			    final byte[] proxyBytes = generateProxy(superclass, classFileName, implementationMethods, interfaces);
			    return Unsafe.defineClass(classLoader, superclass, proxyName, proxyBytes);
			}
			catch (final Exception e)
			{
			    throw new ProxyFactoryException(e);
			}
        }

        public static <T> T constructProxy(final Class<?> clazz, final java.lang.reflect.InvocationHandler handler) throws IllegalStateException
        {
            final Object instance = Unsafe.allocateInstance(clazz);
            Unsafe.setValue(getDeclaredField(clazz, HANDLER_NAME), instance, handler);
            @SuppressWarnings("unchecked")
			final T result = (T) instance;
			return result;
        }

        private static Field getDeclaredField(final Class<?> clazz, final String fieldName)
        {
            try
            {
                return clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
                final String message = String.format("Proxy class does not contain expected field \"%s\": %s", fieldName, clazz.getName());
                throw new IllegalStateException(message, e);
            }
        }

        public static byte[] generateProxy(final Class<?> classToProxy, final String proxyName, final Method[] methods, final Class<?>... interfaces) throws ProxyFactoryException
        {
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            final String proxyClassFileName = proxyName.replace('.', '/');
            final String classFileName = classToProxy.getName().replace('.', '/');

            // push class signature
            final String[] interfaceNames = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++)
            {
                final Class<?> anInterface = interfaces[i];
                interfaceNames[i] = anInterface.getName().replace('.', '/');
            }

            cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, proxyClassFileName, null, classFileName, interfaceNames);
            cw.visitSource(classFileName + ".java", null);

            // push InvocationHandler fields
            cw.visitField(ACC_FINAL + ACC_PRIVATE, HANDLER_NAME, "Ljava/lang/reflect/InvocationHandler;", null, null).visitEnd();

            for (final Method method : methods)
            {
                processMethod(cw, method, proxyClassFileName, HANDLER_NAME);
            }

            return cw.toByteArray();
        }

        private static void processMethod(final ClassWriter cw, final Method method, final String proxyName, final String handlerName) throws ProxyFactoryException {
            final Class<?> returnType = method.getReturnType();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final Class<?>[] exceptionTypes = method.getExceptionTypes();
            final int modifiers = method.getModifiers();

            // push the method definition
            int modifier = 0;
            if (Modifier.isPublic(modifiers))
            {
                modifier = ACC_PUBLIC;
            }
            else if (Modifier.isProtected(modifiers))
            {
                modifier = ACC_PROTECTED;
            }

            final MethodVisitor mv = cw.visitMethod(modifier, method.getName(), getMethodSignatureAsString(returnType, parameterTypes), null, null);
            mv.visitCode();

            // push try/catch block, to catch declared exceptions, and to catch java.lang.Throwable
            final Label l0 = new Label();
            final Label l1 = new Label();
            final Label l2 = new Label();

            if (exceptionTypes.length > 0)
            {
                mv.visitTryCatchBlock(l0, l1, l2, "java/lang/reflect/InvocationTargetException");
            }

            // push try code
            mv.visitLabel(l0);
            final String classNameToOverride = method.getDeclaringClass().getName().replace('.', '/');
            mv.visitLdcInsn(Type.getType("L" + classNameToOverride + ";"));

            // the following code generates the bytecode for this line of Java:
            // Method method = <proxy>.class.getMethod("add", new Class[] { <array of function argument classes> });

            // get the method name to invoke, and push to stack
            mv.visitLdcInsn(method.getName());

            // create the Class[]
            createArrayDefinition(mv, parameterTypes.length, Class.class);

            int length = 1;

            // push parameters into array
            for (int i = 0; i < parameterTypes.length; i++)
            {
                // keep copy of array on stack
                mv.visitInsn(DUP);

                final Class<?> parameterType = parameterTypes[i];

                // push number onto stack
                pushIntOntoStack(mv, i);

                if (parameterType.isPrimitive())
                {
                    final String wrapperType = getWrapperType(parameterType);
                    mv.visitFieldInsn(GETSTATIC, wrapperType, "TYPE", "Ljava/lang/Class;");
                } else {
                    mv.visitLdcInsn(Type.getType(getAsmTypeAsString(parameterType, true)));
                }

                mv.visitInsn(AASTORE);

                if (Long.TYPE.equals(parameterType) || Double.TYPE.equals(parameterType))
                {
                    length += 2;
                } else {
                    length++;
                }
            }

            // invoke getMethod() with the method name and the array of types
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");

            // store the returned method for later
            mv.visitVarInsn(ASTORE, length);

            // the following code generates bytecode equivalent to:
            // return ((<returntype>) invocationHandler.invoke(this, method, new Object[] { <function arguments }))[.<primitive>Value()];

            final Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitVarInsn(ALOAD, 0);

            // get the invocationHandler field from this class
            mv.visitFieldInsn(GETFIELD, proxyName, handlerName, "Ljava/lang/reflect/InvocationHandler;");

            // we want to pass "this" in as the first parameter
            mv.visitVarInsn(ALOAD, 0);

            // and the method we fetched earlier
            mv.visitVarInsn(ALOAD, length);

            // need to construct the array of objects passed in

            // create the Object[]
            createArrayDefinition(mv, parameterTypes.length, Object.class);

            int index = 1;
            // push parameters into array
            for (int i = 0; i < parameterTypes.length; i++)
            {
                // keep copy of array on stack
                mv.visitInsn(DUP);

                final Class<?> parameterType = parameterTypes[i];

                // push number onto stack
                pushIntOntoStack(mv, i);

                if (parameterType.isPrimitive())
                {
                    final String wrapperType = getWrapperType(parameterType);
                    mv.visitVarInsn(getVarInsn(parameterType), index);

                    mv.visitMethodInsn(INVOKESTATIC, wrapperType, "valueOf", "(" + getPrimitiveLetter(parameterType) + ")L" + wrapperType + ";");
                    mv.visitInsn(AASTORE);

                    if (Long.TYPE.equals(parameterType) || Double.TYPE.equals(parameterType))
                    {
                        index += 2;
                    }
                    else
                    {
                        index++;
                    }
                }
                else
                {
                    mv.visitVarInsn(ALOAD, index);
                    mv.visitInsn(AASTORE);
                    index++;
                }
            }

            // invoke the invocationHandler
            mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/reflect/InvocationHandler", "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");

            // cast the result
            mv.visitTypeInsn(CHECKCAST, getCastType(returnType));

            if (returnType.isPrimitive() && (!Void.TYPE.equals(returnType)))
            {
                // get the primitive value
                mv.visitMethodInsn(INVOKEVIRTUAL, getWrapperType(returnType), getPrimitiveMethod(returnType), "()" + getPrimitiveLetter(returnType));
            }

            // push return
            mv.visitLabel(l1);
            if (!Void.TYPE.equals(returnType))
            {
                mv.visitInsn(getReturnInsn(returnType));
            }
            else
            {
                mv.visitInsn(POP);
                mv.visitInsn(RETURN);
            }

            // catch InvocationTargetException
            if (exceptionTypes.length > 0)
            {
                mv.visitLabel(l2);
                mv.visitVarInsn(ASTORE, length);

                final Label l5 = new Label();
                mv.visitLabel(l5);

                for (int i = 0; i < exceptionTypes.length; i++)
                {
                    final Class<?> exceptionType = exceptionTypes[i];

                    mv.visitLdcInsn(Type.getType("L" + exceptionType.getName().replace('.', '/') + ";"));
                    mv.visitVarInsn(ALOAD, length);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/InvocationTargetException", "getCause", "()Ljava/lang/Throwable;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z");

                    final Label l6 = new Label();
                    mv.visitJumpInsn(IFEQ, l6);

                    final Label l7 = new Label();
                    mv.visitLabel(l7);

                    mv.visitVarInsn(ALOAD, length);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/InvocationTargetException", "getCause", "()Ljava/lang/Throwable;");
                    mv.visitTypeInsn(CHECKCAST, exceptionType.getName().replace('.', '/'));
                    mv.visitInsn(ATHROW);
                    mv.visitLabel(l6);

                    if (i == (exceptionTypes.length - 1))
                    {
                        mv.visitTypeInsn(NEW, "java/lang/reflect/UndeclaredThrowableException");
                        mv.visitInsn(DUP);
                        mv.visitVarInsn(ALOAD, length);
                        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V");
                        mv.visitInsn(ATHROW);
                    }
                }
            }

            // finish this method
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private static int getReturnInsn(final Class<?> type)
        {
            if (type.isPrimitive())
            {
                if (Integer.TYPE.equals(type))
                {
                    return IRETURN;
                }
                if (Boolean.TYPE.equals(type))
                {
                    return IRETURN;
                }
                if (Character.TYPE.equals(type))
                {
                    return IRETURN;
                }
                if (Byte.TYPE.equals(type))
                {
                    return IRETURN;
                }
                if (Short.TYPE.equals(type))
                {
                    return IRETURN;
                }
                if (Float.TYPE.equals(type))
                {
                    return FRETURN;
                }
                if (Long.TYPE.equals(type))
                {
                    return LRETURN;
                }
                if (Double.TYPE.equals(type))
                {
                    return DRETURN;
                }
            }

            return ARETURN;
        }

        private static int getVarInsn(final Class<?> type)
        {
            if (type.isPrimitive())
            {
                if (Integer.TYPE.equals(type))
                {
                    return ILOAD;
                }
            	if (Boolean.TYPE.equals(type))
            	{
                    return ILOAD;
                }
            	if (Character.TYPE.equals(type))
            	{
                    return ILOAD;
                }
            	if (Byte.TYPE.equals(type))
            	{
                    return ILOAD;
                }
            	if (Short.TYPE.equals(type))
            	{
                    return ILOAD;
                }
            	if (Float.TYPE.equals(type))
            	{
                    return FLOAD;
                }
            	if (Long.TYPE.equals(type))
            	{
                    return LLOAD;
                }
            	if (Double.TYPE.equals(type))
            	{
                    return DLOAD;
                }
            }

            throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
        }

        private static String getPrimitiveMethod(final Class<?> type)
        {
            if (Integer.TYPE.equals(type))
            {
                return "intValue";
            }
            if (Boolean.TYPE.equals(type))
            {
                return "booleanValue";
            }
            if (Character.TYPE.equals(type))
            {
                return "charValue";
            }
            if (Byte.TYPE.equals(type))
            {
                return "byteValue";
            }
            if (Short.TYPE.equals(type))
            {
                return "shortValue";
            }
            if (Float.TYPE.equals(type))
            {
                return "floatValue";
            }
            if (Long.TYPE.equals(type))
            {
                return "longValue";
            }
            if (Double.TYPE.equals(type))
            {
                return "doubleValue";
            }

            throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
        }

        static String getCastType(final Class<?> returnType)
        {
            if (returnType.isPrimitive())
            {
                return getWrapperType(returnType);
            }
            return getAsmTypeAsString(returnType, false);
        }

        private static String getWrapperType(final Class<?> type)
        {
            if (Integer.TYPE.equals(type))
            {
                return Integer.class.getCanonicalName().replace('.', '/');
            }
            if (Boolean.TYPE.equals(type))
            {
                return Boolean.class.getCanonicalName().replace('.', '/');
            }
            if (Character.TYPE.equals(type))
            {
                return Character.class.getCanonicalName().replace('.', '/');
            }
            if (Byte.TYPE.equals(type))
            {
                return Byte.class.getCanonicalName().replace('.', '/');
            }
            if (Short.TYPE.equals(type))
            {
                return Short.class.getCanonicalName().replace('.', '/');
            }
            if (Float.TYPE.equals(type))
            {
                return Float.class.getCanonicalName().replace('.', '/');
            }
            if (Long.TYPE.equals(type))
            {
                return Long.class.getCanonicalName().replace('.', '/');
            }
            if (Double.TYPE.equals(type))
            {
                return Double.class.getCanonicalName().replace('.', '/');
            }
            if (Void.TYPE.equals(type))
            {
                return Void.class.getCanonicalName().replace('.', '/');
            }

            throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
        }

        private static void pushIntOntoStack(final MethodVisitor mv, final int i)
        {
            if (i == 0)
            {
                mv.visitInsn(ICONST_0);
            }
            else if (i == 1)
            {
                mv.visitInsn(ICONST_1);
            }
            else if (i == 2)
            {
                mv.visitInsn(ICONST_2);
            }
            else if (i == 3)
            {
                mv.visitInsn(ICONST_3);
            }
            else if (i == 4)
            {
                mv.visitInsn(ICONST_4);
            }
            else if (i == 5)
            {
                mv.visitInsn(ICONST_5);
            }
            else if (i > 5 && i <= 255)
            {
                mv.visitIntInsn(BIPUSH, i);
            }
            else
            {
                mv.visitIntInsn(SIPUSH, i);
            }
        }

        private static void createArrayDefinition(final MethodVisitor mv, final int size, final Class<?> type) throws ProxyFactoryException
        {
            // create a new array of java.lang.class (2)
            if (size < 0)
            {
                throw new ProxyFactoryException("Array size cannot be less than zero");
            }

            pushIntOntoStack(mv, size);
            mv.visitTypeInsn(ANEWARRAY, type.getCanonicalName().replace('.', '/'));
        }

        static String getMethodSignatureAsString(final Class<?> returnType, final Class<?>[] parameterTypes)
        {
            final StringBuilder builder = new StringBuilder("(");
            for (final Class<?> parameterType : parameterTypes) {
                builder.append(getAsmTypeAsString(parameterType, true));
            }

            builder.append(")");
            builder.append(getAsmTypeAsString(returnType, true));

            return builder.toString();
        }

        /**
         * Returns the single letter that matches the given primitive in bytecode instructions
         */
        private static String getPrimitiveLetter(final Class<?> type)
        {
            if (Integer.TYPE.equals(type))
            {
                return "I";
            }
            if (Void.TYPE.equals(type))
            {
                return "V";
            }
            if (Boolean.TYPE.equals(type))
            {
                return "Z";
            }
            if (Character.TYPE.equals(type))
            {
                return "C";
            }
            if (Byte.TYPE.equals(type))
            {
                return "B";
            }
            if (Short.TYPE.equals(type))
            {
                return "S";
            }
        	if (Float.TYPE.equals(type))
        	{
                return "F";
            }
        	if (Long.TYPE.equals(type))
        	{
                return "J";
            }
        	if (Double.TYPE.equals(type))
        	{
                return "D";
            }

            throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
        }

        public static String getAsmTypeAsString(final Class<?> parameterType, final boolean wrap)
        {
            if (parameterType.isArray())
            {
                if (parameterType.getComponentType().isPrimitive())
                {
                    final Class<?> componentType = parameterType.getComponentType();
                    return "[" + getPrimitiveLetter(componentType);
                }
                return "[" + getAsmTypeAsString(parameterType.getComponentType(), true);
            }
            if (parameterType.isPrimitive()) {
                return getPrimitiveLetter(parameterType);
            }
            String className = parameterType.getCanonicalName();

            if (parameterType.isMemberClass()) {
                final int lastDot = className.lastIndexOf(".");
                className = className.substring(0, lastDot) + "$" + className.substring(lastDot + 1);
            }

            if (wrap)
            {
                return "L" + className.replace('.', '/') + ";";
            }
            return className.replace('.', '/');
        }

        private static class Unsafe
        {
            // sun.misc.Unsafe
            private static final Object unsafe;
            private static final Method defineClass;
            private static final Method allocateInstance;
            private static final Method putObject;
            private static final Method objectFieldOffset;

            static
            {
                final Class<?> unsafeClass;
                try {
                    unsafeClass = AccessController.doPrivileged(new PrivilegedAction<Class<?>>()
            		{
                        @Override
                        public Class<?> run()
                        {
                            try
                            {
                                return Thread.currentThread().getContextClassLoader().loadClass("sun.misc.Unsafe");
                            }
                            catch (Exception e)
                            {
                                try
                                {
                                    return ClassLoader.getSystemClassLoader().loadClass("sun.misc.Unsafe");
                                }
                                catch (ClassNotFoundException e1)
                                {
                                    throw new IllegalStateException("Cannot get sun.misc.Unsafe", e);
                                }
                            }
                        }
                    });
                }
                catch (Exception e)
                {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe class", e);
                }

                unsafe = AccessController.doPrivileged(new PrivilegedAction<Object>()
        		{
                    @Override
                    public Object run()
                    {
                        try
                        {
                            final Field field = unsafeClass.getDeclaredField("theUnsafe");
                            field.setAccessible(true);
                            return field.get(null);
                        }
                        catch (Exception e)
                        {
                            throw new IllegalStateException("Cannot get sun.misc.Unsafe", e);
                        }
                    }
                });
                allocateInstance = AccessController.doPrivileged(new PrivilegedAction<Method>()
        		{
                    @Override
                    public Method run()
                    {
                        try
                        {
                            final Method mtd = unsafeClass.getDeclaredMethod("allocateInstance", Class.class);
                            mtd.setAccessible(true);
                            return mtd;
                        }
                        catch (Exception e)
                        {
                            throw new IllegalStateException("Cannot get sun.misc.Unsafe.allocateInstance", e);
                        }
                    }
                });
                objectFieldOffset = AccessController.doPrivileged(new PrivilegedAction<Method>()
        		{
                    @Override
                    public Method run()
                    {
                        try
                        {
                            final Method mtd = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
                            mtd.setAccessible(true);
                            return mtd;
                        }
                        catch (Exception e)
                        {
                            throw new IllegalStateException("Cannot get sun.misc.Unsafe.objectFieldOffset", e);
                        }
                    }
                });
                putObject = AccessController.doPrivileged(new PrivilegedAction<Method>()
        		{
                    @Override
                    public Method run()
                    {
                        try
                        {
                            final Method mtd = unsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
                            mtd.setAccessible(true);
                            return mtd;
                        }
                        catch (Exception e)
                        {
                            throw new IllegalStateException("Cannot get sun.misc.Unsafe.putObject", e);
                        }
                    }
                });
                defineClass = AccessController.doPrivileged(new PrivilegedAction<Method>()
        		{
                    @Override
                    public Method run()
                    {
                        try
                        {
                            final Method mtd = unsafeClass.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                            mtd.setAccessible(true);
                            return mtd;
                        }
                        catch (Exception e)
                        {
                            throw new IllegalStateException("Cannot get sun.misc.Unsafe.defineClass", e);
                        }
                    }
                });
            }

            private static Object allocateInstance(final Class<?> clazz)
            {
                try
                {
                    return allocateInstance.invoke(unsafe, clazz);
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException("Failed to allocateInstance of Proxy class " + clazz.getName(), e);
                }
                catch (InvocationTargetException e)
                {
                    final Throwable throwable = e.getTargetException() != null ? e.getTargetException() : e;
                    throw new IllegalStateException("Failed to allocateInstance of Proxy class " + clazz.getName(), throwable);
                }
            }

            private static void setValue(final Field field, final Object object, final Object value)
            {
                final long offset;
                try
                {
                    offset = (Long) objectFieldOffset.invoke(unsafe, field);
                }
                catch (Exception e)
                {
                    throw new IllegalStateException("Failed getting offset for: field=" + field.getName() + "  class=" + field.getDeclaringClass().getName(), e);
                }

                try
                {
                    putObject.invoke(unsafe, object, offset, value);
                }
                catch (Exception e)
                {
                    throw new IllegalStateException("Failed putting field=" + field.getName() + "  class=" + field.getDeclaringClass().getName(), e);
                }
            }

            private static Class<?> defineClass(final ClassLoader loader, final Class<?> clsToProxy, final String proxyName, final byte[] proxyBytes) throws IllegalAccessException, InvocationTargetException
            {
                return (Class<?>) defineClass.invoke(unsafe, proxyName, proxyBytes, 0, proxyBytes.length, loader, clsToProxy.getProtectionDomain());
            }
        }
    }

    //////////////// these classes should be protected in ProxyFactory
    @SuppressWarnings("serial")
	private static class DelegatorInvocationHandler extends AbstractInvocationHandler
	{
        private final ObjectProvider<?> delegateProvider;

        protected DelegatorInvocationHandler(ObjectProvider<?> delegateProvider) 
        {
            this.delegateProvider = delegateProvider;
        }

        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            try
            {
                return method.invoke(delegateProvider.getObject(), args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }
    }

    @SuppressWarnings("serial")
	private static class InterceptorInvocationHandler extends AbstractInvocationHandler
	{
        private final Object target;
        private final Interceptor methodInterceptor;

        public InterceptorInvocationHandler(Object target, Interceptor methodInterceptor)
        {
            this.target = target;
            this.methodInterceptor = methodInterceptor;
        }

        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            final ReflectionInvocation invocation = new ReflectionInvocation(target, proxy, method, args);
            return methodInterceptor.intercept(invocation);
        }
    }

    @SuppressWarnings("serial")
	private abstract static class AbstractInvocationHandler implements InvocationHandler, Serializable
	{
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (isHashCode(method))
            {
                return System.identityHashCode(proxy);
            }
            if (isEqualsMethod(method))
            {
                return proxy == args[0];
            }
            return invokeImpl(proxy, method, args);
        }

        protected abstract Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable;
    }

    @SuppressWarnings("serial")
	private static class InvokerInvocationHandler extends AbstractInvocationHandler
	{
        private final Invoker invoker;

        public InvokerInvocationHandler(Invoker invoker)
        {
            this.invoker = invoker;
        }

        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            return invoker.invoke(proxy, method, args);
        }
    }

    protected static boolean isHashCode(Method method)
    {
        return "hashCode".equals(method.getName()) &&
            Integer.TYPE.equals(method.getReturnType()) &&
            method.getParameterTypes().length == 0;
    }

    protected static boolean isEqualsMethod(Method method)
    {
        return "equals".equals(method.getName()) &&
            Boolean.TYPE.equals(method.getReturnType()) &&
            method.getParameterTypes().length == 1 &&
            Object.class.equals(method.getParameterTypes()[0]);
    }

    @SuppressWarnings("serial")
	private static class ReflectionInvocation implements Invocation, Serializable
	{
        private final Method method;
        private final Object[] arguments;
        private final Object proxy;
        private final Object target;

        public ReflectionInvocation(final Object target, final Object proxy, final Method method, final Object[] arguments)
        {
            this.method = method;
            this.arguments = (arguments == null ? ProxyUtils.EMPTY_ARGUMENTS : arguments);
            this.proxy = proxy;
            this.target = target;
        }

        public Object[] getArguments()
        {
            return arguments;
        }

        public Method getMethod()
        {
            return method;
        }

        public Object getProxy()
        {
            return proxy;
        }

        public Object proceed() throws Throwable
        {
            try
            {
                return method.invoke(target, arguments);
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }
    }
}