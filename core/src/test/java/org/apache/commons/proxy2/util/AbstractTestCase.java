package org.apache.commons.proxy2.util;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

import static org.junit.Assert.*;

/**
 * @author James Carman
 * @since 1.1
 */
public abstract class AbstractTestCase
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
