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
/**
 * The various {@link org.apache.commons.proxy2.ProxyFactory} implementations create {@link Serializable} proxies;
 * however it is not always possible or practical to serialize the complete structure of a given proxy object. The
 * intent of this package is to facilitate the "serialization proxy pattern" by means of the {@code readResolve()} and
 * {@code writeReplace} methods supported by Java's serialization mechanism. This would normally be problematic with
 * Commons Proxy because its proxies are generalized to expose only methods declared by superclasses (where applicable)
 * or proxied interfaces. Therefore we declare the following interfaces:
 * <ul>
 *   <li>{@link ReadResolve}</li>
 *   <li>{@link WriteReplace}</li>
 * </ul>
 *
 * Typically, you should define your proxy to include {@link WriteReplace} among its interfaces, and implement it to
 * return some object that implements {@link ReadResolve} (or simply declares the {@code Object readResolve()} method
 * in any scope, but using the interface brings compiler assistance).
 *
 * Hint: Your {@link ReadResolve#readResolve()} implementation will typically use serialized information to recreate an
 * equivalent proxy object, which probably implies some form of {@code static} access.
 */
package org.apache.commons.proxy2.serialization;
import java.io.Serializable;
