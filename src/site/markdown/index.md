<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

## Commons Proxy: Dynamic Proxies Made Easy

  The *Proxy* design pattern ([GoF][]) allows you to provide
  "a surrogate or placeholder for another object to control access to it".
  Proxies can be used in many ways, some of which are:

  * **Deferred Initialization** -
     the proxy acts as a "stand-in" for the actual implementation allowing
     it to be instantiated only when absolutely necessary.
  * **Security** -
     the proxy object can verify that the user actually has the permission to
     execute the method (a la EJB).
  * **Logging** -
     the proxy can log evey method invocation, providing valuable debugging
     information.
  * **Performance Monitoring** -
     the proxy can log each method invocation to a performance monitor allowing
     system administrators to see what parts of the system are potentially
     bogged down.

  *Commons Proxy* supports dynamic proxy generation using proxy factories,
  object providers, invokers, and interceptors.

## Proxy Factories
  A [ProxyFactory][] encapsulates all necessary proxying logic away from your
  code. Switching proxying techniques/technologies is as simple as using a
  different proxy factory implementation class.
  *Commons Proxy* provides several proxy factory implementation modules:

  * [commons-proxy2-jdk][]
  * [commons-proxy2-cglib][]
  * [commons-proxy2-javassist][]
  * [commons-proxy2-asm4][]

  Additionally, the core library provides a proxy factory
  [implementation][defaultPF] that delegates to instances discoverable using
  the Java [ServiceLoader][] mechanism (including those provided by the listed
  modules).

  Proxy factories allow you to create three different types of proxy objects:

  * **Delegator Proxy** - delegates each method invocation to an object
     provided by an [ObjectProvider][].
  * **Interceptor Proxy** - allows an [Interceptor][] to intercept each
     method invocation as it makes its way to the target of the invocation.
  * **Invoker Proxy** - uses an [Invoker][] to handle all method invocations.

## Object Providers
  [Object providers][providers] provide the objects which will be the
  "target" of a proxy. There are two types of object providers:

### Core Object Providers
  A core object provider provides a core implementation object.
  *Commons Proxy* supports many different implementations including:

  * **Constant** - Always returns a specific object
  * **Bean** - Instantiates an object of a specified class each time
  * **Cloning** - Reflectively calls the public `clone()` method
                  on a `Cloneable` object

### Decorating Object Providers
  A decorating object provider decorates the object returned by another
  provider. *Commons Proxy* provides a few implementations including:

  * **Singleton** - Calls a nested provider at most once, returning that
                    original value on all subsequent invocations

## Invokers
  An [Invoker][] handles all method invocations using a single method.
  *Commons Proxy* provides a few invoker implementations:

  * **Null** - Always returns a `null` (useful for the "Null Object" pattern)
  * **Duck Typing** - Supports so-called "duck typing" by adapting a class to
                      an interface it does not implement.
  * **Invocation Handler Adapter** - Adapts an implementation of the JDK
[InvocationHandler][] interface as a *Commons Proxy* [Invoker][].

## Interceptors
  *Commons Proxy* allows you to "intercept" a method invocation using
  an [Interceptor][]. Interceptors provide *rudimentary* aspect-oriented
  programming (AOP) support, allowing you to alter the parameters/results
  of a method invocation without actually changing the implementation of
  the method itself. *Commons Proxy* provides a few interceptor
  implementations including:

  * **ObjectProvider** - returns the value from an [ObjectProvider][]
  * **Throwing** - throws an exception
  * **Switch** - provides a fluent API to configure the handling
                 of invoked methods

## Serialization
  The proxies created by the provided proxy factories are `Serializable` in
  most cases. For more complex cases *Commons Proxy* provides basic support
  for the "serialization proxy" pattern. See
  [org.apache.commons.proxy.serialization][serializationproxy] for details.

## Stubbing
  The [StubBuilder][] class allows you to create a proxy with customized
behavior specified by a typesafe DSL. The [AnnotationBuilder][] variant
provides a simple way to create Java annotation instances at runtime.

## Releases
  The latest version is v1.0. - [Download now!][download]

  For previous releases, see the [Apache archive][archive].

  _**Note:** The 1.x releases are compatible with JDK1.4+._

## Support
  The [Commons mailing lists][mailing-lists] act as the main support forum.
  The `user` list is suitable for most library usage queries.
  The `dev` list is intended for the development discussion.
  Please remember that the lists are shared between all Commons components,
  so prefix your email subject with `[proxy]`.

  Issues may be reported via [ASF JIRA][issue-tracking]. Please read the
  instructions carefully to submit a useful bug report or enhancement request.

[download]: http://commons.apache.org/downloads/download_proxy.cgi
[archive]: http://archive.apache.org/dist/commons/proxy/
[mailing-lists]: mail-lists.html
[issue-tracking]: issue-tracking.html

[commons-proxy2-jdk]: commons-proxy2-jdk/index.html
[commons-proxy2-cglib]: commons-proxy2-cglib/index.html
[commons-proxy2-javassist]: commons-proxy2-javassist/index.html
[commons-proxy2-asm4]: commons-proxy2-asm4/index.html
[ProxyFactory]: apidocs/org/apache/commons/proxy2/ProxyFactory.html
[ObjectProvider]: apidocs/org/apache/commons/proxy2/ObjectProvider.html
[Interceptor]: apidocs/org/apache/commons/proxy2/Interceptor.html
[Invoker]: apidocs/org/apache/commons/proxy2/Invoker.html
[defaultPF]: apidocs/org/apache/commons/proxy2/ProxyUtils.html#proxyFactory\(\)
[providers]: apidocs/index.html?org/apache/commons/proxy2/provider/package-summary.html
[StubBuilder]: apidocs/org/apache/commons/proxy2/stub/StubBuilder.html
[AnnotationBuilder]: apidocs/org/apache/commons/proxy2/stub/AnnotationBuilder.html
[serializationproxy]: apidocs/index.html?org/apache/commons/proxy2/serialization/package-summary.html

[ServiceLoader]: http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html
[InvocationHandler]: http://docs.oracle.com/javase/6/docs/api/java/lang/reflect/InvocationHandler.html
[GoF]: http://www.amazon.com/exec/obidos/tg/detail/-/0201633612/qid=1125413337/sr=1-1/ref=sr_1_1/104-0714405-6441551?v=glance&amp;s=books
