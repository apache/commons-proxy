package org.apache.commons.proxy2.util;

import junit.framework.TestCase;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

/**
 * @author James Carman
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
