# Mapping loaders/savers extension
This project has an extension for WireMock that allows to specify a location for a file both with a single
request/response or an array of requests/responses instead of a directory.

As it is in WireMock by default, the files have to be in the folder `resources/mappings`.

_The next examples are described using the programming language **Scala** but it can be used in any other language
that WireMock supports._

## UnitFileMappingSource

**WireMock** uses one file loader by default and if it detects files without its specific pattern, it fails.
This mapping loader avoid that. See the examples.

## SingleJsonFileMappingsSource
It has to be used in the next way:

```scala
private val wireMockServer: WireMockServer =
  new WireMockServer().loadMappingsUsing(SingleJsonFileMappingsSource("filename"))
```

The format of the file is the default one:

```json
{
  "id": "53c34296-4a18-11e8-842f-0ed5f89f718b",
  "request": {
    "method": "GET",
    "urlPattern": "/testmapping"
  },
  "response": {
    "status": 200,
    "body": "default test mapping",
    "headers": {
      "Content-Type": "text/plain"
    }
  }
}
```

## MultiJsonFileMappingSource
It has to be used in the next way:

```scala
private val wireMockServer: WireMockServer =
  new WireMockServer().loadMappingsUsing(MultiJsonFileMappingSource("filename"))
```

The format of the file is an array of the default one, so instead of having many files, you can keep
all the requests/responses in one file:

```json
[
  {
    "id": "481cc51a-4a19-11e8-842f-0ed5f89f718b",
    "request": {
      "method": "GET",
      "urlPattern": "/testmapping/1"
    },
    "response": {
      "status": 200,
      "body": "default test mapping",
      "headers": {
        "Content-Type": "text/plain"
      }
    }
  }, {
    "id": "481cc880-4a19-11e8-842f-0ed5f89f718b",
    "request": {
      "method": "GET",
      "urlPattern": "/testmapping/2"
    },
    "response": {
      "status": 200,
      "body": "default test mapping",
      "headers": {
        "Content-Type": "text/plain"
      }
    }
  }, {
    "id": "481cc9e8-4a19-11e8-842f-0ed5f89f718b",
    "request": {
      "method": "GET",
      "urlPattern": "/testmapping/3"
    },
    "response": {
      "status": 200,
      "body": "default test mapping",
      "headers": {
        "Content-Type": "text/plain"
      }
    }
  }
]
```

## Examples
There are some examples in the folder [test](src/test/scala/info/maalvarez/wiremock).