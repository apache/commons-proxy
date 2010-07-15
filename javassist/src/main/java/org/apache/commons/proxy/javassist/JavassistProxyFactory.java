package org.apache.commons.proxy.javassist;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.exception.ProxyFactoryException;
import org.apache.commons.proxy.impl.AbstractProxyClassGenerator;
import org.apache.commons.proxy.impl.AbstractSubclassingProxyFactory;
import org.apache.commons.proxy.impl.ProxyClassCache;

import java.lang.reflect.Method;

public class JavassistProxyFactory extends AbstractSubclassingProxyFactory
{
    //**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static final String GET_METHOD_METHOD_NAME = "_javassistGetMethod";

    private static final ProxyClassCache delegatingProxyClassCache = new ProxyClassCache(
            new DelegatingProxyClassGenerator());
    private static final ProxyClassCache interceptorProxyClassCache = new ProxyClassCache(
            new InterceptorProxyClassGenerator());
    private static final ProxyClassCache invocationHandlerProxyClassCache = new ProxyClassCache(
            new InvokerProxyClassGenerator());

//**********************************************************************************************************************
// Static Methods
//**********************************************************************************************************************

    private static void addGetMethodMethod(CtClass proxyClass) throws CannotCompileException
    {
        final CtMethod method = new CtMethod(JavassistUtils.resolve(Method.class), GET_METHOD_METHOD_NAME,
                JavassistUtils.resolve(new Class[]{String.class, String.class, Class[].class}), proxyClass);
        final String body = "try { return Class.forName($1).getMethod($2, $3); } catch( Exception e ) " +
                "{ throw new RuntimeException(\"Unable to look up method.\", e); }";
        method.setBody(body);
        proxyClass.addMethod(method);
    }

//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    public Object createDelegatorProxy(ClassLoader classLoader, ObjectProvider targetProvider,
                                       Class... proxyClasses)
    {
        try
        {
            final Class clazz = delegatingProxyClassCache.getProxyClass(classLoader, proxyClasses);
            return clazz.getConstructor(ObjectProvider.class)
                    .newInstance(targetProvider);
        }
        catch (Exception e)
        {
            throw new ProxyFactoryException("Unable to instantiate proxy from generated proxy class.", e);
        }
    }

    public Object createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                         Class... proxyClasses)
    {
        try
        {
            final Class clazz = interceptorProxyClassCache.getProxyClass(classLoader, proxyClasses);
            return clazz.getConstructor(Object.class, Interceptor.class)
                    .newInstance(target, interceptor);
        }
        catch (Exception e)
        {
            throw new ProxyFactoryException("Unable to instantiate proxy class instance.", e);
        }
    }

    public Object createInvokerProxy(ClassLoader classLoader, Invoker invoker,
                                     Class... proxyClasses)
    {
        try
        {
            final Class clazz = invocationHandlerProxyClassCache.getProxyClass(classLoader, proxyClasses);
            return clazz.getConstructor(Invoker.class)
                    .newInstance(invoker);
        }
        catch (Exception e)
        {
            throw new ProxyFactoryException("Unable to instantiate proxy from generated proxy class.", e);
        }
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private static class DelegatingProxyClassGenerator extends AbstractProxyClassGenerator
    {
        public Class generateProxyClass(ClassLoader classLoader, Class[] proxyClasses)
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass(getSuperclass(proxyClasses));
                JavassistUtils.addField(ObjectProvider.class, "provider", proxyClass);
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve(new Class[]{ObjectProvider.class}),
                        proxyClass);
                proxyConstructor.setBody("{ this.provider = $1; }");
                proxyClass.addConstructor(proxyConstructor);
                JavassistUtils.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                final Method[] methods = getImplementationMethods(proxyClasses);
                for (int i = 0; i < methods.length; ++i)
                {
                    if (!isEqualsMethod(methods[i]) && !isHashCode(methods[i]))
                    {
                        final Method method = methods[i];
                        final CtMethod ctMethod = new CtMethod(JavassistUtils.resolve(method.getReturnType()),
                                method.getName(),
                                JavassistUtils.resolve(method.getParameterTypes()),
                                proxyClass);
                        final String body = "{ return ( $r ) ( ( " + method.getDeclaringClass().getName() +
                                " )provider.getObject() )." +
                                method.getName() + "($$); }";
                        ctMethod.setBody(body);
                        proxyClass.addMethod(ctMethod);
                    }
                }
                return proxyClass.toClass(classLoader);
            }
            catch (CannotCompileException e)
            {
                throw new ProxyFactoryException("Could not compile class.", e);
            }
        }
    }

    private static class InterceptorProxyClassGenerator extends AbstractProxyClassGenerator
    {
        public Class generateProxyClass(ClassLoader classLoader, Class[] proxyClasses)
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass(getSuperclass(proxyClasses));
                final Method[] methods = getImplementationMethods(proxyClasses);
                JavassistUtils.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                JavassistUtils.addField(Object.class, "target", proxyClass);
                JavassistUtils.addField(Interceptor.class, "interceptor", proxyClass);
                addGetMethodMethod(proxyClass);
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve(
                                new Class[]{Object.class, Interceptor.class}),
                        proxyClass);
                proxyConstructor
                        .setBody(
                                "{\n\tthis.target = $1;\n\tthis.interceptor = $2; }");
                proxyClass.addConstructor(proxyConstructor);
                for (int i = 0; i < methods.length; ++i)
                {
                    if (!isEqualsMethod(methods[i]) && !isHashCode(methods[i]))
                    {
                        final CtMethod method = new CtMethod(JavassistUtils.resolve(methods[i].getReturnType()),
                                methods[i].getName(),
                                JavassistUtils.resolve(methods[i].getParameterTypes()),
                                proxyClass);
                        final Class invocationClass = JavassistInvocation
                                .getMethodInvocationClass(classLoader, methods[i]);

                        final String body = "{\n\t return ( $r ) interceptor.intercept( new " + invocationClass.getName() +
                                "( " + GET_METHOD_METHOD_NAME + "(\"" + methods[i].getDeclaringClass().getName() +
                                "\", \"" + methods[i].getName() + "\", $sig), target, $args ) );\n }";
                        method.setBody(body);
                        proxyClass.addMethod(method);
                    }

                }
                return proxyClass.toClass(classLoader);
            }
            catch (CannotCompileException e)
            {
                throw new ProxyFactoryException("Could not compile class.", e);
            }
        }


    }

    private static void addEqualsMethod(CtClass proxyClass)
            throws CannotCompileException
    {
        final CtMethod equalsMethod = new CtMethod(JavassistUtils.resolve(Boolean.TYPE), "equals",
                JavassistUtils.resolve(new Class[]{Object.class}), proxyClass);
        final String body = "{\n\treturn this == $1;\n}";
        equalsMethod.setBody(body);
        proxyClass.addMethod(equalsMethod);
    }

    private static void addHashCodeMethod(CtClass proxyClass)
            throws CannotCompileException
    {
        final CtMethod hashCodeMethod = new CtMethod(JavassistUtils.resolve(Integer.TYPE), "hashCode",
                new CtClass[0], proxyClass);
        hashCodeMethod.setBody("{\n\treturn System.identityHashCode(this);\n}");
        proxyClass.addMethod(hashCodeMethod);
    }

    private static class InvokerProxyClassGenerator extends AbstractProxyClassGenerator
    {
        public Class generateProxyClass(ClassLoader classLoader, Class[] proxyClasses)
        {
            try
            {
                final CtClass proxyClass = JavassistUtils.createClass(getSuperclass(proxyClasses));
                final Method[] methods = getImplementationMethods(proxyClasses);
                JavassistUtils.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                JavassistUtils.addField(Invoker.class, "invoker", proxyClass);
                final CtConstructor proxyConstructor = new CtConstructor(
                        JavassistUtils.resolve(
                                new Class[]{Invoker.class}),
                        proxyClass);
                proxyConstructor
                        .setBody("{\n\tthis.invoker = $1; }");
                proxyClass.addConstructor(proxyConstructor);
                addGetMethodMethod(proxyClass);
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                for (int i = 0; i < methods.length; ++i)
                {
                    if (!isEqualsMethod(methods[i]) && !isHashCode(methods[i]))
                    {
                        final CtMethod method = new CtMethod(JavassistUtils.resolve(methods[i].getReturnType()),
                                methods[i].getName(),
                                JavassistUtils.resolve(methods[i].getParameterTypes()),
                                proxyClass);
                        final String body = "{\n\t return ( $r ) invoker.invoke( this, " + GET_METHOD_METHOD_NAME + "(\"" +
                                methods[i].getDeclaringClass().getName() +
                                "\", \"" + methods[i].getName() + "\", $sig), $args );\n }";
                        method.setBody(body);
                        proxyClass.addMethod(method);
                    }
                }
                return proxyClass.toClass(classLoader);
            }
            catch (CannotCompileException e)
            {
                throw new ProxyFactoryException("Could not compile class.", e);
            }
        }
    }
}
