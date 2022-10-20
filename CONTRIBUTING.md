# Contribution guidelines

This project follows [SCM API's Contribution Guidelines](https://github.com/jenkinsci/scm-api-plugin/blob/master/CONTRIBUTING.md).

# Developer Information

## Environment

The following build environment is required to build this plugin

- `java-1.8` and `maven-3.5.2`

## Build

To build the plugin locally:

```
mvn clean verify
```

## Release

To release the plugin:

```
mvn release:prepare release:perform -B
```

## Test local instance

To test in a local Jenkins instance

```
mvn hpi:run
```

