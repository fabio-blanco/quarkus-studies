# quarkus-reactive - A sample of Quarkus REST App

This project uses Quarkus, the Supersonic Subatomic Java Framework.
It was created by following the [GETTING STARTED WITH REACTIVE](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources) tutorial.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Notes from the tutorial execution

- [Quarkus vs Spring Boot comparison](https://www.baeldung.com/spring-boot-vs-quarkus)
- [Reactive SQL clients](https://quarkus.io/guides/reactive-sql-clients)
- [Mutiny is a reactive programming library](https://quarkus.io/guides/mutiny-primer) used by Quarkus.
It is similar to Project Reactor.
- When testing reactive applications it is not possible to use the `@TestTransaction` annotation.
I didn't find any way to rollback a database operation after a test execution in order
to avoid test contamination. At least not with *Panache*.
- [Assertj is a fluent assertions java library for tests](https://assertj.github.io/doc/)
- [Hibernate Reactive](https://hibernate.org/reactive/) is a reactive API form Hibernate ORM.
- [Hibernate Reactive with Panache](https://pt.quarkus.io/guides/hibernate-reactive-panache) is a Quarkus extension that 
allows a simplified entity reactive api

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/quarkus-reactive-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Related Guides

- Reactive PostgreSQL client ([guide](https://quarkus.io/guides/reactive-sql-clients)): Connect to the PostgreSQL database using the reactive pattern

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
