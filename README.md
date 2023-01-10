httpclient
==========

Library providing convenience methods for accessing web resources via HTTP.

Based on the Jersey REST framework.

### Maven

Add the dependency to your Maven pom.xml

```xml
<dependency>
  <groupId>dk.dbc</groupId>
  <artifactId>dbc-commons-httpclient</artifactId>
  <version>2.0-SNAPSHOT</version>
</dependency>
```

### usage

In your Java code

```java
import dk.dbc.httpclient;

HttpClient httpClient = HttpClient.create(HttpClient.newClient());
```

GET requests:

```java
try (final Response response = new HttpGet(httpClient)
            .withBaseUrl("http://somehost:someport")
            .withPathElements("path", "to", "resource")
            .withQueryParameter("key", "value")
            .withHeader("Accept", "text/html")
            .execute()) {

    // do something with the response...
}
```

POST requests:

```java
try (final Response response = new HttpPost(httpClient)
            .withBaseUrl("http://somehost:someport")
            .withPathElements("path", "to", "resource")
            .withData(obj, MediaType.APPLICATION_JSON)
            .withHeader("User-Agent", "MyAwesomeJavaClient")
            .withHeader("ETag", "1234")
            .execute()) {

    // do something with the response...
}
```

PUT requests:

```java
try (final Response response = new HttpPut(httpClient)
            .withBaseUrl("http://somehost:someport")
            .withPathElements("path", "to", "resource")
            .withData(obj, MediaType.APPLICATION_JSON)
            .withHeader("User-Agent", "StillAnAwesomeJavaClient")
            .execute()) {

    // do something with the response...
}
```

DELETE requests:

```java
try (final Response response = new HttpDelete(httpClient)
            .withBaseUrl("http://somehost:someport")
            .withPathElements("path", "to", "resource", "id")
            .execute()) {

    // do something with the response...
}
```

HTTP requests can also be executed in a fail-safe manner with automatic retry functionality.

Note that the FailSafeHttpClient will forcibly override a RetryPolicy.onRetry() listener set by the client, 
unless the three argument create() method is used with the overrideOnRetry flag set to false.

```java
final RetryPolicy<Response> retryPolicy = new RetryPolicy<Response>()
        .handle(ProcessingException.class)
        .handleResultIf(response -> response.getStatus() == 404 || response.getStatus() == 500)
        .withDelay(Duration.ofSeconds(1))
        .withMaxRetries(3);

final FailSafeHttpClient failSafeHttpClient = FailSafeHttpClient.create(HttpClient.newClient(), retryPolicy);

try (final Response response = new HttpGet(failSafeHttpClient)
            .withBaseUrl("http://somehost:someport")
            .withPathElements("path", "to", "resource")
            .withQueryParameter("key", "value")
            .withHeader("Accept", "text/html")
            .execute()) {

    // do something with the response...
}
```

Resource paths containing variables can be interpolated using the PathBuilder class.

```java
final PathBuilder path = new PathBuilder("path/to/resource/{id}")
            .bind("id", "id42");

try (final Response response = new HttpGet(httpClient)
            .withBaseUrl("http://somehost:someport")
            .withPathElements(path.build())
            .execute()) {

    // do something with the response...
}
```

### development

**Requirements**

To build this project JDK 11 or higher and Apache Maven are required.

### License
                                             
Copyright © 2018-2023 [DBC Digital A/S](http://www.dbc.dk)
See license text in LICENSE.txt
