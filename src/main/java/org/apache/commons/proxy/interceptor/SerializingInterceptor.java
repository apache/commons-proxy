package org.apache.commons.proxy.interceptor;

import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An interceptor which makes a serialized copy of all parameters and return values.  This
 * is useful when testing remote services to ensure that all parameter/return types
 * are in fact serializable/deserializable.
 * @since 1.0
 */
public class SerializingInterceptor implements Interceptor
{
    public Object intercept(Invocation invocation) throws Throwable
    {
        Object[] arguments = invocation.getArguments();
        for (int i = 0; i < arguments.length; i++)
        {
            arguments[i] = serializedCopy(arguments[i]);
        }
        return serializedCopy(invocation.proceed());
    }

    private Object serializedCopy(Object original)
    {
        try
        {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(original);
            oout.close();
            bout.close();
            final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            final ObjectInputStream oin = new ObjectInputStream(bin);
            final Object copy = oin.readObject();
            oin.close();
            bin.close();
            return copy;
        }
        catch (IOException e)
        {
            throw new RuntimeException( "Unable to make serialized copy of " + original.getClass().getName() + " object.", e );
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException( "Unable to make serialized copy of " + original.getClass().getName() + " object.", e );
        }
    }
}
