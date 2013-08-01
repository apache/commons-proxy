package org.apache.commons.proxy2.stub;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.proxy2.*;
import org.apache.commons.proxy2.interceptor.SwitchInterceptor;
import org.apache.commons.proxy2.interceptor.matcher.ArgumentMatcher;
import org.apache.commons.proxy2.interceptor.matcher.InvocationMatcher;
import org.apache.commons.proxy2.invoker.NullInvoker;
import org.apache.commons.proxy2.invoker.RecordedInvocation;

import java.lang.reflect.Method;
import java.util.*;

public class TrainingContext
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final ThreadLocal<TrainingContext> TRAINING_CONTEXT = new ThreadLocal<TrainingContext>();

    private final ProxyFactory proxyFactory;

    private Deque<TrainingContextFrame<?>> frameDeque = new LinkedList<TrainingContextFrame<?>>();

//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    public static void clear()
    {
        TRAINING_CONTEXT.remove();
    }

    public static TrainingContext getCurrent()
    {
        return TRAINING_CONTEXT.get();
    }

    public static TrainingContext set(ProxyFactory proxyFactory)
    {
        TrainingContext context = new TrainingContext(proxyFactory);
        TRAINING_CONTEXT.set(context);
        return context;
    }

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public TrainingContext(ProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    private TrainingContextFrame<?> peek()
    {
        return frameDeque.peek();
    }

    <T> T popStub(Class<T> type)
    {
        return proxyFactory.createInterceptorProxy(
                proxyFactory.createInvokerProxy(NullInvoker.INSTANCE, type),
                frameDeque.pop().stubInterceptor,
                type);
    }

    <T> T push(Class<T> type)
    {
        return push(type, new SwitchInterceptor());
    }

    <T> T push(Class<T> type, SwitchInterceptor switchInterceptor)
    {
        TrainingContextFrame<T> frame = new TrainingContextFrame<T>(switchInterceptor);
        Invoker invoker = new TrainingInvoker(frame);
        frameDeque.push(frame);
        return proxyFactory.createInvokerProxy(invoker, type);
    }

    public void record(ArgumentMatcher argumentMatcher)
    {
        peek().argumentMatchers.add(argumentMatcher);
    }

    public void then(Interceptor interceptor)
    {
        peek().then(interceptor);
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static final class ExactArgumentsMatcher implements InvocationMatcher
    {
        private final RecordedInvocation recordedInvocation;

        private ExactArgumentsMatcher(RecordedInvocation recordedInvocation)
        {
            this.recordedInvocation = recordedInvocation;
        }

        @Override
        public boolean matches(Invocation invocation)
        {
            return invocation.getMethod().equals(recordedInvocation.getInvokedMethod()) &&
                    Arrays.deepEquals(invocation.getArguments(), recordedInvocation.getArguments());
        }
    }

    private static final class MatchingArgumentsMatcher implements InvocationMatcher
    {
        private final RecordedInvocation recordedInvocation;
        private final ArgumentMatcher[] matchers;

        private MatchingArgumentsMatcher(RecordedInvocation recordedInvocation, ArgumentMatcher[] matchers)
        {
            this.recordedInvocation = recordedInvocation;
            this.matchers = ArrayUtils.clone(matchers);
        }

        @Override
        public boolean matches(Invocation invocation)
        {
            return invocation.getMethod().equals(recordedInvocation.getInvokedMethod()) &&
                    allArgumentsMatch(invocation.getArguments());
        }

        private boolean allArgumentsMatch(Object[] arguments)
        {
            for (int i = 0; i < arguments.length; i++)
            {
                Object argument = arguments[i];
                if (!matchers[i].matches(argument))
                {
                    return false;
                }
            }
            return true;
        }
    }

    private static class TrainingContextFrame<T>
    {
        private final String id = UUID.randomUUID().toString();

        private final SwitchInterceptor stubInterceptor;

        private final List<ArgumentMatcher> argumentMatchers = new LinkedList<ArgumentMatcher>();

        private InvocationMatcher matcher = null;

        private TrainingContextFrame(SwitchInterceptor stubInterceptor)
        {
            this.stubInterceptor = stubInterceptor;
        }

        private String getId()
        {
            return id;
        }

        void then(Interceptor thenInterceptor)
        {
            if (matcher == null)
            {
                throw new IllegalStateException("No when!");
            }
            stubInterceptor.when(matcher).then(thenInterceptor);
            matcher = null;
        }

        void methodInvoked(Method method, Object[] arguments)
        {
            final ArgumentMatcher[] matchersArray = argumentMatchers.toArray(new ArgumentMatcher[argumentMatchers.size()]);
            argumentMatchers.clear();
            final RecordedInvocation invocation = new RecordedInvocation(method, arguments);
            if (ArrayUtils.isEmpty(matchersArray))
            {
                this.matcher = new ExactArgumentsMatcher(invocation);
            }
            else if (matchersArray.length == arguments.length)
            {
                this.matcher = new MatchingArgumentsMatcher(invocation, matchersArray);
            }
            else
            {
                throw new IllegalStateException("Either use exact arguments or argument matchers, but not both.");
            }
        }
    }

    private static class TrainingInvoker implements Invoker
    {
        private final String id;

        private TrainingInvoker(TrainingContextFrame<?> frame)
        {
            this.id = frame.getId();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable
        {
            final TrainingContextFrame<?> frame = getCurrent().peek();
            if (!frame.getId().equals(id))
            {
                throw new IllegalStateException("Wrong stub!");
            }
            else
            {
                frame.methodInvoked(method, arguments);
            }
            return ProxyUtils.nullValue(method.getReturnType());
        }
    }
}
