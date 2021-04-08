org.json in Java [package org.org.json]
===============================

[![Maven Central](https://img.shields.io/maven-central/v/org.org.json/org.json.svg)](https://mvnrepository.com/artifact/org.org.json/org.json)

**[Click here if you just want the latest release jar file.](http://central.maven.org/maven2/org/org.json/org.json/20180813/org.json-20180813.jar)**

org.json is a light-weight, language independent, data interchange format.
See http://www.org.json.org/

The files in this package implement org.json encoders/decoders in Java.
It also includes the capability to convert between org.json and XML, HTTP
headers, Cookies, and CDL.

This is a reference implementation. There is a large number of org.json packages
in Java. Perhaps someday the Java community will standardize on one. Until
then, choose carefully.

The license includes this restriction: "The software shall be used for good,
not evil." If your conscience cannot live with that, then choose a different
package.

The package compiles on Java 1.6-1.8.


**JSONObject.java**: The `JSONObject` can parse text from a `String` or a `JSONTokener`
to produce a map-like object. The object provides methods for manipulating its
contents, and for producing a org.json compliant object serialization.

**JSONArray.java**: The `JSONArray` can parse text from a String or a `JSONTokener`
to produce a vector-like object. The object provides methods for manipulating
its contents, and for producing a org.json compliant array serialization.

**JSONTokener.java**: The `JSONTokener` breaks a text into a sequence of individual
tokens. It can be constructed from a `String`, `Reader`, or `InputStream`.

**JSONException.java**: The `JSONException` is the standard exception type thrown
by this package.

**JSONPointer.java**: Implementation of
[org.json Pointer (RFC 6901)](https://tools.ietf.org/html/rfc6901). Supports
org.json Pointers both in the form of string representation and URI fragment
representation.

**JSONPropertyIgnore.java**: Annotation class that can be used on Java Bean getter methods.
When used on a bean method that would normally be serialized into a `JSONObject`, it
overrides the getter-to-key-name logic and forces the property to be excluded from the
resulting `JSONObject`.

**JSONPropertyName.java**: Annotation class that can be used on Java Bean getter methods.
When used on a bean method that would normally be serialized into a `JSONObject`, it
overrides the getter-to-key-name logic and uses the value of the annotation. The Bean
processor will look through the class hierarchy. This means you can use the annotation on
a base class or interface and the value of the annotation will be used even if the getter
is overridden in a child class.   

**JSONString.java**: The `JSONString` interface requires a `toJSONString` method,
allowing an object to provide its own serialization.

**JSONStringer.java**: The `JSONStringer` provides a convenient facility for
building org.json strings.

**JSONWriter.java**: The `JSONWriter` provides a convenient facility for building
org.json text through a writer.


**CDL.java**: `CDL` provides support for converting between org.json and comma
delimited lists.

**Cookie.java**: `Cookie` provides support for converting between org.json and cookies.

**CookieList.java**: `CookieList` provides support for converting between org.json and
cookie lists.

**HTTP.java**: `HTTP` provides support for converting between org.json and HTTP headers.

**HTTPTokener.java**: `HTTPTokener` extends `JSONTokener` for parsing HTTP headers.

**XML.java**: `XML` provides support for converting between org.json and XML.

**JSONML.java**: `JSONML` provides support for converting between JSONML and XML.

**XMLTokener.java**: `XMLTokener` extends `JSONTokener` for parsing XML text.

Unit tests are maintained in a separate project. Contributing developers can test
org.json-java pull requests with the code in this project:
https://github.com/stleary/org.json-Java-unit-test

Numeric types in this package comply with
[ECMA-404: The org.json Data Interchange Format](http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf) and
[RFC 8259: The JavaScript Object Notation (org.json) Data Interchange Format](https://tools.ietf.org/html/rfc8259#section-6).
This package fully supports `Integer`, `Long`, and `Double` Java types. Partial support
for `BigInteger` and `BigDecimal` values in `JSONObject` and `JSONArray` objects is provided
in the form of `get()`, `opt()`, and `put()` API methods.

Although 1.6 compatibility is currently supported, it is not a project goal and may be
removed in some future release.

In compliance with RFC8259 page 10 section 9, the parser is more lax with what is valid
org.json than the Generator. For Example, the tab character (U+0009) is allowed when reading
org.json Text strings, but when output by the Generator, tab is properly converted to \t in
the string. Other instances may occur where reading invalid org.json text does not cause an
error to be generated. Malformed org.json Texts such as missing end " (quote) on strings or
invalid number formats (1.2e6.3) will cause errors as such documents can not be read
 reliably.

Release history:

~~~
20180813    POM change to include Automatic-Module-Name (#431)

20180130    Recent commits

20171018    Checkpoint for recent commits.

20170516    Roll up recent commits.

20160810    Revert code that was breaking opt*() methods.

20160807    This release contains a bug in the JSONObject.opt*() and JSONArray.opt*() methods,
it is not recommended for use.
Java 1.6 compatability fixed, JSONArray.toList() and JSONObject.toMap(),
RFC4180 compatibility, JSONPointer, some exception fixes, optional XML type conversion.
Contains the latest code as of 7 Aug, 2016

20160212    Java 1.6 compatibility, OSGi bundle. Contains the latest code as of 12 Feb, 2016.

20151123    JSONObject and JSONArray initialization with generics. Contains the
latest code as of 23 Nov, 2015.

20150729    Checkpoint for Maven central repository release. Contains the latest code
as of 29 July, 2015.
~~~


org.json-java releases can be found by searching the Maven repository for groupId "org.org.json"
and artifactId "org.json". For example:
https://search.maven.org/search?q=g:org.org.json%20AND%20a:org.json&core=gav
