# Contribution guidelines

This project follows [SCM API's Contribution Guidelines](https://github.com/jenkinsci/scm-api-plugin/blob/master/CONTRIBUTING.md).
Source code is formatted with `mvn spotless:apply`.

# Developer Information

## Environment

The following build environment is required to build this plugin

- Java 11 or Java 17
- Apache Maven 3.9.3 or later

## Build

To build the plugin locally:

```
mvn clean verify
```

## Formatting

To format the plugin source files:

```
mvn spotless:apply
```

## Test local instance

To test in a local Jenkins instance

```
mvn hpi:run
```
