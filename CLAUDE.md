# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a pure, functional, typesafe command line parser library for Java, with language-specific wrappers for Kotlin, Scala, and Bukkit (Minecraft). The library is published to Maven Central under `io.typst:command-*`.

## Build System

Multi-module Gradle project (Java 8 target):
- `command-core`: Core Java library with functional command parsing
- `command-bukkit`: Bukkit/Spigot integration for Minecraft plugins
- `command-scala`: Scala 3 wrapper with idiomatic syntax
- `command-kotlin`: Kotlin wrapper with DSL-style helpers

### Common Commands

```bash
# Build all modules
./gradlew build

# Run tests
./gradlew test

# Run tests for a specific module
./gradlew :command-core:test
./gradlew :command-bukkit:test
./gradlew :command-scala:test
./gradlew :command-kotlin:test

# Clean build artifacts
./gradlew clean

# Build without tests
./gradlew assemble

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Publish to Maven Central (requires credentials)
./gradlew publish
```

## Architecture

### Core Design Pattern

The library uses **algebraic data types** (sum types) to represent commands as pure data structures, separating command definition from execution. This provides:
- Type safety: Commands return strongly-typed result objects
- Purity: Parsing doesn't execute side effects
- Concurrency: Safe to parse commands in parallel
- Testability: Execute business logic without parsing
- Reusability: Command implementations independent of parsing

### Key Components

**Command<A>** (sum type: Mapping | Parser)
- `Command.Mapping<A>`: Key-value pairs for subcommands (e.g., `/item add`, `/item remove`)
- `Command.Parser<A>`: Argument parser that constructs result type A

**Argument<A>**
- Defines how to parse a single argument type (int, string, etc.)
- Supports tab completion
- Can be contextual (access to sender/source)
- Standard arguments in `StandardArguments` class

**CommandSpec**
- Metadata for a command (arguments, description, permission)
- Extracted from Command nodes for help/validation

**Either<L, R>** and **Option<T>**
- Functional types for error handling without exceptions
- `Either.Left` = failure, `Either.Right` = success
- Used throughout parsing results

### Parsing Flow

1. Define command structure using `Command.mapping()` and `Command.argument()`
2. Parse args: `Command.parse(args, command)` returns `Either<CommandFailure, CommandSuccess>`
3. Extract result: `CommandSuccess.getValue()` gives the typed command object
4. Execute: Pattern match on result type (sealed interface/enum-like)

### Module-Specific Patterns

**command-core**: Pure Java with Lombok for data classes. Uses custom `FunctionN` interfaces (Function3-Function8) for multi-arg commands since Java lacks them.

**command-bukkit**:
- `BukkitCommands.register()` / `BukkitCommands.registerPrime()` for integration
- `BukkitArguments` provides Bukkit-specific argument types (Player, World, etc.)
- Permission validation via `CommandSpec.permission`

**command-scala**: Trait-based design with implicit arguments. Use `ScalaCommand.mapping()` and `ScalaCommand.argument()` for Scala-friendly varargs.

**command-kotlin**: Extension functions and inline reified generics. Use `commandMap()` for mappings and `command()` for parsers with Kotlin lambdas.

## Code Generation

The core module uses Gradle tasks to generate boilerplate:
- `generateTuples`: Creates `TupleN` classes (Tuple1-Tuple8) with map functions
- `generateFunctions`: Creates `FunctionN` interfaces (Function3-Function8)

These are generated at build time to `build/tmp/<taskName>/` but should be manually copied to source if definitions change.

## Testing

Tests follow the pattern of defining sealed command interfaces (e.g., `MyCommand` with implementations like `AddItem`, `RemoveItem`) and asserting parsed results match expected instances. See `core/src/test/java/io/typst/command/CommandTest.java` for examples.

## Publishing

Publishing is configured in root `build.gradle` with the `registerPublish()` helper. Requires:
- GPG signing key
- Sonatype OSSRH credentials in `~/.gradle/gradle.properties`:
  ```
  ossrhUsername=...
  ossrhPassword=...
  ```

Version is set in root `build.gradle` (`version = '3.1.6'`). Update this for releases.
