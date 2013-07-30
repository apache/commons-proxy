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

package org.apache.commons.proxy2.stub;

public interface StubInterface
{
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public String one(String value);
    public String three(String arg1, String arg2);
    public String two(String value);

    public byte[] byteArray();
    public char[] charArray();
    public short[] shortArray();
    public int[] intArray();
    public long[] longArray();
    public float[] floatArray();
    public double[] doubleArray();
    public boolean[] booleanArray();
    public String[] stringArray();

    public String arrayParameter(String... strings);

    public void voidMethod(String arg);
}
