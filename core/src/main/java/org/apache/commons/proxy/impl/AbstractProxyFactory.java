package org.apache.commons.proxy.impl;

import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.ProxyUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class AbstractProxyFactory implements ProxyFactory
{   
    /**
     * Returns true if all <code>proxyClasses</code> are interfaces.
     *
     * @param proxyClasses the proxy classes
     * @return true if all <code>proxyClasses</code> are interfaces
     */
    public boolean canProxy( Class... proxyClasses )
    {
        for( Class proxyClass : proxyClasses )
        {
            if( !proxyClass.isInterface() )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyClasses     the interfaces that the proxy should implement
     * @return a proxy which delegates to the object provided by the target object provider
     */
    public Object createDelegatorProxy( ObjectProvider delegateProvider, Class... proxyClasses )
    {
        return createDelegatorProxy(Thread.currentThread().getContextClassLoader(), delegateProvider, proxyClasses);
    }

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param delegateProvider the delegate provider
     * @param proxyClass     the class/interface that the proxy should implement
     * @return a proxy which delegates to the object provided by the target object provider
     */
    @SuppressWarnings("unchecked")
    public <T> T createDelegatorProxy( ObjectProvider<T> delegateProvider, Class<T> proxyClass )
    {
        return (T)createDelegatorProxy(delegateProvider, new Class[] {proxyClass});
    }

    /**
     * Creates a proxy which delegates to the object provided by <code>delegateProvider</code>.
     *
     * @param classLoader      the class loader to use when generating the proxy
     * @param delegateProvider the delegate provider
     * @param proxyClass     the class/interface that the proxy should implement
     * @return a proxy which delegates to the object provided by the target <code>delegateProvider>
     */
    @SuppressWarnings( "unchecked" )
    public <T> T createDelegatorProxy( ClassLoader classLoader, ObjectProvider<T> delegateProvider,
                                       Class<T> proxyClass )
    {
        return ( T ) createDelegatorProxy(classLoader, delegateProvider, new Class[] {proxyClass});
    }

    /**
     * Creates a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.  The proxy will be generated using the current thread's "context class loader."
     *
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    public Object createInterceptorProxy( Object target, Interceptor interceptor,
                                          Class... proxyClasses )
    {
        return createInterceptorProxy(Thread.currentThread().getContextClassLoader(), target, interceptor,
                                      proxyClasses);
    }

    /**
     * Creates a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.  The proxy will be generated using the current thread's "context class loader."
     *
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClass the class/interface that the proxy should implement
     * @return a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    @SuppressWarnings( "unchecked" )
    public <T> T createInterceptorProxy( Object target, Interceptor interceptor,
                                         Class<T> proxyClass )
    {
        return ( T ) createInterceptorProxy(target, interceptor, new Class[] {proxyClass});
    }

    /**
     * Creates a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     * <code>target</code> object.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param target       the target object
     * @param interceptor  the method interceptor
     * @param proxyClass the class/interface that the proxy should implement.
     * @return a proxy which passes through a {@link Interceptor interceptor} before eventually reaching the
     *         <code>target</code> object.
     */
    @SuppressWarnings( "unchecked" )
    public <T> T createInterceptorProxy( ClassLoader classLoader,
                                         Object target,
                                         Interceptor interceptor,
                                         Class<T> proxyClass )
    {
        return ( T ) createInterceptorProxy(classLoader, target, interceptor, new Class[] {proxyClass});
    }

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param invoker      the invoker
     * @param proxyClasses the interfaces that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    public Object createInvokerProxy( Invoker invoker, Class... proxyClasses )
    {
        return createInvokerProxy(Thread.currentThread().getContextClassLoader(), invoker,
                                  proxyClasses);
    }

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.  The proxy will be
     * generated using the current thread's "context class loader."
     *
     * @param invoker      the invoker
     * @param proxyClass the class/interface that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    @SuppressWarnings( "unchecked" )
    public <T> T createInvokerProxy( Invoker invoker, Class<T> proxyClass )
    {
        return ( T ) createInvokerProxy(invoker, new Class[] {proxyClass});
    }

    /**
     * Creates a proxy which uses the provided {@link Invoker} to handle all method invocations.
     *
     * @param classLoader  the class loader to use when generating the proxy
     * @param invoker      the invoker
     * @param proxyClass the class/interface that the proxy should implement
     * @return a proxy which uses the provided {@link Invoker} to handle all method invocations
     */
    @SuppressWarnings( "unchecked" )
    public <T> T createInvokerProxy( ClassLoader classLoader, Invoker invoker,
                                     Class<T> proxyClass )
    {
        return ( T ) createInvokerProxy(classLoader, invoker, new Class[] {proxyClass});
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private static class DelegatorInvocationHandler extends AbstractInvocationHandler
    {
        private final ObjectProvider delegateProvider;

        protected DelegatorInvocationHandler( ObjectProvider delegateProvider )
        {
            this.delegateProvider = delegateProvider;
        }

        public Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable
        {
            try
            {
                return method.invoke(delegateProvider.getObject(), args);
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }

    private static class InterceptorInvocationHandler extends AbstractInvocationHandler
    {
        private final Object target;
        private final Interceptor methodInterceptor;

        public InterceptorInvocationHandler( Object target, Interceptor methodInterceptor )
        {
            this.target = target;
            this.methodInterceptor = methodInterceptor;
        }

        public Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable
        {
            final ReflectionInvocation invocation = new ReflectionInvocation(target, method, args);
            return methodInterceptor.intercept(invocation);
        }
    }

    private abstract static class AbstractInvocationHandler implements InvocationHandler, Serializable
    {
        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            if( isHashCode(method) )
            {
                return System.identityHashCode(proxy);
            }
            else if( isEqualsMethod(method) )
            {
                return proxy == args[0];
            }
            else
            {
                return invokeImpl(proxy, method, args);
            }
        }

        protected abstract Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable;
    }

    private static class InvokerInvocationHandler extends AbstractInvocationHandler
    {
        private final Invoker invoker;

        public InvokerInvocationHandler( Invoker invoker )
        {
            this.invoker = invoker;
        }

        public Object invokeImpl( Object proxy, Method method, Object[] args ) throws Throwable
        {
            return invoker.invoke(proxy, method, args);
        }
    }

    protected static boolean isHashCode( Method method )
    {
        return "hashCode".equals(method.getName()) &&
                Integer.TYPE.equals(method.getReturnType()) &&
                method.getParameterTypes().length == 0;
    }

    protected static boolean isEqualsMethod( Method method )
    {
        return "equals".equals(method.getName()) &&
                Boolean.TYPE.equals(method.getReturnType()) &&
                method.getParameterTypes().length == 1 &&
                Object.class.equals(method.getParameterTypes()[0]);
    }

    private static class ReflectionInvocation implements Invocation, Serializable
    {
        private final Method method;
        private final Object[] arguments;
        private final Object target;

        public ReflectionInvocation( Object target, Method method, Object[] arguments )
        {
            this.method = method;
            this.arguments = ( arguments == null ? ProxyUtils.EMPTY_ARGUMENTS : arguments );
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
            return target;
        }

        public Object proceed() throws Throwable
        {
            try
            {
                return method.invoke(target, arguments);
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
