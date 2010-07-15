package org.apache.commons.proxy.cglib;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.impl.AbstractSubclassingProxyFactory;

import java.io.Serializable;
import java.lang.reflect.Method;

public class CglibProxyFactory extends AbstractSubclassingProxyFactory
{
//**********************************************************************************************************************
// Fields
//**********************************************************************************************************************

    private static CallbackFilter callbackFilter = new CglibProxyFactoryCallbackFilter();

//**********************************************************************************************************************
// ProxyFactory Implementation
//**********************************************************************************************************************

    public Object createDelegatorProxy(ClassLoader classLoader, ObjectProvider targetProvider,
                                       Class... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new ObjectProviderDispatcher(targetProvider), new EqualsHandler(), new HashCodeHandler()});
        return enhancer.create();
    }

    public Object createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                         Class... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new InterceptorBridge(target, interceptor), new EqualsHandler(), new HashCodeHandler()});
        return enhancer.create();
    }

    public Object createInvokerProxy(ClassLoader classLoader, Invoker invoker,
                                     Class... proxyClasses)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setInterfaces(toInterfaces(proxyClasses));
        enhancer.setSuperclass(getSuperclass(proxyClasses));
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbacks(new Callback[]{new InvokerBridge(invoker), new EqualsHandler(), new HashCodeHandler()});
        return enhancer.create();
    }

//**********************************************************************************************************************
// Inner Classes
//**********************************************************************************************************************

    private static class CglibProxyFactoryCallbackFilter implements CallbackFilter
    {
        public int accept(Method method)
        {
            if (isEqualsMethod(method))
            {
                return 1;
            }
            else if (isHashCode(method))
            {
                return 2;
            }
            else
            {
                return 0;
            }
        }
    }

    private static class EqualsHandler implements MethodInterceptor, Serializable
    {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
        {
            return o == objects[0];
        }
    }

    private static class HashCodeHandler implements MethodInterceptor, Serializable
    {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
        {
            return System.identityHashCode(o);
        }
    }

    private static class InterceptorBridge implements net.sf.cglib.proxy.MethodInterceptor, Serializable
    {
        private final Interceptor inner;
        private final Object target;

        public InterceptorBridge(Object target, Interceptor inner)
        {
            this.inner = inner;
            this.target = target;
        }

        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
        {
            return inner.intercept(new MethodProxyInvocation(target, method, args, methodProxy));
        }
    }

    private static class InvokerBridge implements net.sf.cglib.proxy.InvocationHandler, Serializable
    {
        private final Invoker original;

        public InvokerBridge(Invoker original)
        {
            this.original = original;
        }

        public Object invoke(Object object, Method method, Object[] objects) throws Throwable
        {
            return original.invoke(object, method, objects);
        }
    }

    private static class MethodProxyInvocation implements Invocation, Serializable
    {
        private final MethodProxy methodProxy;
        private final Method method;
        private final Object[] args;
        private final Object target;

        public MethodProxyInvocation(Object target, Method method, Object[] args, MethodProxy methodProxy)
        {
            this.target = target;
            this.method = method;
            this.methodProxy = methodProxy;
            this.args = args;
        }

        public Method getMethod()
        {
            return method;
        }

        public Object[] getArguments()
        {
            return args;
        }

        public Object proceed() throws Throwable
        {
            return methodProxy.invoke(target, args);
        }

        public Object getProxy()
        {
            return target;
        }
    }

    private static class ObjectProviderDispatcher implements Dispatcher, Serializable
    {
        private final ObjectProvider delegateProvider;

        public ObjectProviderDispatcher(ObjectProvider delegateProvider)
        {
            this.delegateProvider = delegateProvider;
        }

        public Object loadObject()
        {
            return delegateProvider.getObject();
        }
    }
}
