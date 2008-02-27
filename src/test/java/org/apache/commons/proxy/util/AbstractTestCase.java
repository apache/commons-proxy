package org.apache.commons.proxy.util;

import junit.framework.TestCase;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;

/**
 * @auothor James Carman
 * @since 1.1
 */
public abstract class AbstractTestCase extends TestCase
{
//**********************************************************************************************************************
// Other Methods
//**********************************************************************************************************************

    protected void assertSerializable( Object o )
    {
        assertTrue(o instanceof Serializable);
        SerializationUtils.clone(( Serializable ) o);
    }
}
