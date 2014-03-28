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

package org.apache.commons.proxy2.util;

import java.io.IOException;
import java.io.Serializable;

public class EchoImpl extends AbstractEcho implements DuplicateEcho, Serializable
{
    //**********************************************************************************************************************
    // Fields
    //**********************************************************************************************************************

    private static final long serialVersionUID = -4844873352607521103L;

    //**********************************************************************************************************************
    // Echo Implementation
    //**********************************************************************************************************************

    @Override
    public void echo()
    {
    }

    @Override
    public boolean echoBack(boolean b)
    {
        return b;
    }

    @Override
    public String echoBack(String[] messages)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.length; i++)
        {
            String message = messages[i];
            sb.append(message);
        }
        return sb.toString();
    }

    @Override
    public String echoBack(String[][] messages)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.length; i++)
        {
            sb.append(echoBack(messages[i]));
        }
        return sb.toString();
    }

    @Override
    public String echoBack(String[][][] messages)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.length; i++)
        {
            sb.append(echoBack(messages[i]));
        }
        return sb.toString();
    }

    @Override
    public int echoBack(int i)
    {
        return i;
    }

    @Override
    public String echoBack(String message1, String message2)
    {
        return message1 + message2;
    }

    @Override
    public void illegalArgument()
    {
        throw new IllegalArgumentException("dummy message");
    }

    @Override
    public void ioException() throws IOException
    {
        throw new IOException("dummy message");
    }
}
