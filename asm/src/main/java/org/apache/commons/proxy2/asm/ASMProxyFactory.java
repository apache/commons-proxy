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
package org.apache.commons.proxy2.asm;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.proxy2.Interceptor;
import org.apache.commons.proxy2.Invocation;
import org.apache.commons.proxy2.Invoker;
import org.apache.commons.proxy2.ObjectProvider;
import org.apache.commons.proxy2.ProxyUtils;
import org.apache.commons.proxy2.exception.ProxyFactoryException;
import org.apache.commons.proxy2.impl.AbstractProxyClassGenerator;
import org.apache.commons.proxy2.impl.AbstractSubclassingProxyFactory;
import org.apache.commons.proxy2.impl.ProxyClassCache;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class ASMProxyFactory extends AbstractSubclassingProxyFactory
{
    private static final ProxyClassCache PROXY_CLASS_CACHE = new ProxyClassCache(new ProxyGenerator());

    @Override
    public <T> T createDelegatorProxy(final ClassLoader classLoader, final ObjectProvider<?> delegateProvider,
            final Class<?>... proxyClasses)
    {
        return createProxy(classLoader, new DelegatorInvoker(delegateProvider), proxyClasses);
    }

    @Override
    public <T> T createInterceptorProxy(final ClassLoader classLoader, final Object target,
            final Interceptor interceptor, final Class<?>... proxyClasses)
    {
        return createProxy(classLoader, new InterceptorInvoker(target, interceptor), proxyClasses);
    }

    @Override
    public <T> T createInvokerProxy(final ClassLoader classLoader, final Invoker invoker,
            final Class<?>... proxyClasses)
    {
        return createProxy(classLoader, new InvokerInvoker(invoker), proxyClasses);
    }

    private <T> T createProxy(final ClassLoader classLoader, final AbstractInvoker invoker,
            final Class<?>... proxyClasses)
    {
        final Class<?> proxyClass = PROXY_CLASS_CACHE.getProxyClass(classLoader, proxyClasses);
        try
        {
            @SuppressWarnings("unchecked")
            final T result = (T) proxyClass.getConstructor(Invoker.class).newInstance(invoker);
            return result;
        }
        catch (Exception e)
        {
            throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
        }
    }

    private static class ProxyGenerator extends AbstractProxyClassGenerator implements Opcodes
    {
        private static final AtomicInteger CLASS_NUMBER = new AtomicInteger(0);
        private static final String CLASSNAME_PREFIX = "CommonsProxyASM_";
        private static final String HANDLER_NAME = "__handler";
        private static final Type INVOKER_TYPE = Type.getType(Invoker.class);

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
                return loadClass(classLoader, proxyName, proxyBytes);
            }
            catch (final Exception e)
            {
                throw new ProxyFactoryException(e);
            }
        }

        private static byte[] generateProxy(final Class<?> classToProxy, final String proxyName,
                final Method[] methods, final Class<?>... interfaces) throws ProxyFactoryException
        {
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            final Type proxyType = Type.getObjectType(proxyName);

            // push class signature
            final String[] interfaceNames = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++)
            {
                interfaceNames[i] = Type.getType(interfaces[i]).getInternalName();
            }

            final Type superType = Type.getType(classToProxy);
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, proxyType.getInternalName(), null, superType.getInternalName(),
                    interfaceNames);

            // create Invoker field
            cw.visitField(ACC_FINAL + ACC_PRIVATE, HANDLER_NAME, INVOKER_TYPE.getDescriptor(), null, null).visitEnd();

            init(cw, proxyType, superType);

            for (final Method method : methods)
            {
                processMethod(cw, method, proxyType, HANDLER_NAME);
            }

            return cw.toByteArray();
        }

        private static void init(final ClassWriter cw, final Type proxyType, Type superType)
        {
            final GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, new org.objectweb.asm.commons.Method("<init>",
                    Type.VOID_TYPE, new Type[] { INVOKER_TYPE }), null, null, cw);
            // invoke super constructor:
            mg.loadThis();
            mg.invokeConstructor(superType, org.objectweb.asm.commons.Method.getMethod("void <init> ()"));

            // assign handler:
            mg.loadThis();
            mg.loadArg(0);
            mg.putField(proxyType, HANDLER_NAME, INVOKER_TYPE);
            mg.returnValue();
            mg.endMethod();
        }

        private static void processMethod(final ClassWriter cw, final Method method, final Type proxyType,
                final String handlerName) throws ProxyFactoryException
        {
            final Type sig = Type.getType(method);
            final Type[] exceptionTypes = getTypes(method.getExceptionTypes());

            // push the method definition
            final int access = (ACC_PUBLIC | ACC_PROTECTED) & method.getModifiers();
            final org.objectweb.asm.commons.Method m = org.objectweb.asm.commons.Method.getMethod(method);
            final GeneratorAdapter mg = new GeneratorAdapter(access, m, null, getTypes(method.getExceptionTypes()), cw);

            final Label tryBlock = exceptionTypes.length > 0 ? mg.mark() : null;

            mg.push(Type.getType(method.getDeclaringClass()));

            // the following code generates the bytecode for this line of Java:
            // Method method = <proxy>.class.getMethod("add", new Class[] {
            // <array of function argument classes> });

            // get the method name to invoke, and push to stack

            mg.push(method.getName());

            // create the Class[]
            mg.push(sig.getArgumentTypes().length);
            final Type classType = Type.getType(Class.class);
            mg.newArray(classType);

            // push parameters into array
            for (int i = 0; i < sig.getArgumentTypes().length; i++)
            {
                // keep copy of array on stack
                mg.dup();

                // push index onto stack
                mg.push(i);
                mg.push(sig.getArgumentTypes()[i]);
                mg.arrayStore(classType);
            }

            // invoke getMethod() with the method name and the array of types
            mg.invokeVirtual(classType, org.objectweb.asm.commons.Method
                    .getMethod("java.lang.reflect.Method getDeclaredMethod(String, Class[])"));
            // store the returned method for later

            // the following code generates bytecode equivalent to:
            // return ((<returntype>) invoker.invoke(this, method, new Object[]
            // { <function arguments }))[.<primitive>Value()];

            mg.loadThis();

            mg.getField(proxyType, handlerName, INVOKER_TYPE);
            // put below method:
            mg.swap();

            // we want to pass "this" in as the first parameter
            mg.loadThis();
            // put below method:
            mg.swap();

            // need to construct the array of objects passed in

            // create the Object[]
            mg.push(sig.getArgumentTypes().length);
            final Type objectType = Type.getType(Object.class);
            mg.newArray(objectType);

            // push parameters into array
            for (int i = 0; i < sig.getArgumentTypes().length; i++)
            {
                // keep copy of array on stack
                mg.dup();

                // push index onto stack
                mg.push(i);

                mg.loadArg(i);
                mg.valueOf(sig.getArgumentTypes()[i]);
                mg.arrayStore(objectType);
            }

            // invoke the invoker
            mg.invokeInterface(INVOKER_TYPE, org.objectweb.asm.commons.Method
                    .getMethod("Object invoke(Object, java.lang.reflect.Method, Object[])"));

            // cast the result
            mg.unbox(sig.getReturnType());

            // push return
            mg.returnValue();

            // catch InvocationTargetException
            if (exceptionTypes.length > 0)
            {
                final Type caughtExceptionType = Type.getType(InvocationTargetException.class);
                mg.catchException(tryBlock, mg.mark(), caughtExceptionType);

                final Label throwCause = new Label();

                mg.invokeVirtual(caughtExceptionType,
                        org.objectweb.asm.commons.Method.getMethod("Throwable getCause()"));

                for (int i = 0; i < exceptionTypes.length; i++)
                {
                    mg.dup();
                    mg.push(exceptionTypes[i]);
                    mg.swap();
                    mg.invokeVirtual(classType,
                            org.objectweb.asm.commons.Method.getMethod("boolean isInstance(Object)"));
                    // if true, throw cause:
                    mg.ifZCmp(GeneratorAdapter.NE, throwCause);
                }
                // no exception types matched; throw
                // UndeclaredThrowableException:
                final int cause = mg.newLocal(Type.getType(Exception.class));
                mg.storeLocal(cause);
                final Type undeclaredType = Type.getType(UndeclaredThrowableException.class);
                mg.newInstance(undeclaredType);
                mg.dup();
                mg.loadLocal(cause);
                mg.invokeConstructor(undeclaredType, new org.objectweb.asm.commons.Method("<init>", Type.VOID_TYPE,
                        new Type[] { Type.getType(Throwable.class) }));
                mg.throwException();

                mg.mark(throwCause);
                mg.throwException();
            }

            // finish this method
            mg.endMethod();
        }

        private static Type[] getTypes(Class<?>... src)
        {
            final Type[] result = new Type[src.length];
            for (int i = 0; i < result.length; i++)
            {
                result[i] = Type.getType(src[i]);
            }
            return result;
        }

        /**
         * Adapted from http://asm.ow2.org/doc/faq.html#Q5
         * 
         * @param b
         * @return Class<?>
         */
        private static Class<?> loadClass(final ClassLoader loader, String className, byte[] b)
        {
            // override classDefine (as it is protected) and define the class.
            try
            {
                final Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class,
                        int.class, int.class);

                // protected method invocation
                final boolean accessible = method.isAccessible();
                if (!accessible)
                {
                    method.setAccessible(true);
                }
                try
                {
                    return (Class<?>) method
                            .invoke(loader, className, b, Integer.valueOf(0), Integer.valueOf(b.length));
                }
                finally
                {
                    if (!accessible)
                    {
                        method.setAccessible(false);
                    }
                }
            }
            catch (Exception e)
            {
                throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("serial")
    private static class DelegatorInvoker extends AbstractInvoker
    {
        private final ObjectProvider<?> delegateProvider;

        protected DelegatorInvoker(ObjectProvider<?> delegateProvider)
        {
            this.delegateProvider = delegateProvider;
        }

        @Override
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
    private static class InterceptorInvoker extends AbstractInvoker
    {
        private final Object target;
        private final Interceptor methodInterceptor;

        public InterceptorInvoker(Object target, Interceptor methodInterceptor)
        {
            this.target = target;
            this.methodInterceptor = methodInterceptor;
        }

        @Override
        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            final ReflectionInvocation invocation = new ReflectionInvocation(target, proxy, method, args);
            return methodInterceptor.intercept(invocation);
        }
    }

    @SuppressWarnings("serial")
    private abstract static class AbstractInvoker implements Invoker, Serializable
    {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (isHashCode(method))
            {
                return Integer.valueOf(System.identityHashCode(proxy));
            }
            if (isEqualsMethod(method))
            {
                return Boolean.valueOf(proxy == args[0]);
            }
            return invokeImpl(proxy, method, args);
        }

        protected abstract Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable;
    }

    @SuppressWarnings("serial")
    private static class InvokerInvoker extends AbstractInvoker
    {
        private final Invoker invoker;

        public InvokerInvoker(Invoker invoker)
        {
            this.invoker = invoker;
        }

        @Override
        public Object invokeImpl(Object proxy, Method method, Object[] args) throws Throwable
        {
            return invoker.invoke(proxy, method, args);
        }
    }

    protected static boolean isHashCode(Method method)
    {
        return "hashCode".equals(method.getName()) && Integer.TYPE.equals(method.getReturnType())
                && method.getParameterTypes().length == 0;
    }

    protected static boolean isEqualsMethod(Method method)
    {
        return "equals".equals(method.getName()) && Boolean.TYPE.equals(method.getReturnType())
                && method.getParameterTypes().length == 1 && Object.class.equals(method.getParameterTypes()[0]);
    }

    private static class ReflectionInvocation implements Invocation
    {
        private final Method method;
        private final Object[] arguments;
        private final Object proxy;
        private final Object target;

        public ReflectionInvocation(final Object target, final Object proxy, final Method method,
                final Object[] arguments)
        {
            this.method = method;
            this.arguments = ObjectUtils.defaultIfNull(ArrayUtils.clone(arguments), ProxyUtils.EMPTY_ARGUMENTS);
            this.proxy = proxy;
            this.target = target;
        }

        @Override
        public Object[] getArguments()
        {
            return arguments;
        }

        @Override
        public Method getMethod()
        {
            return method;
        }

        @Override
        public Object getProxy()
        {
            return proxy;
        }

        @Override
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
