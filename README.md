# Command

![Maven Central Version](https://img.shields.io/maven-central/v/io.typst/command-bukkit)

A pure, functional, typesafe command line parser.

# Import

## Gradle

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.typst:command-bukkit:3.1.6'
}
```

## Maven

```xml
<dependency>
    <groupId>io.typst</groupId>
    <artifactId>command-bukkit</artifactId>
    <version>3.1.6</version>
</dependency>
```

# Usage

```java
// core/src/test/../CommandTest.java
// MyCommand = AddItem | RemoveItem | OpenItemList | ReloadCommand
Command<MyCommand> command = Command.mapping(
        pair("item", Command.mapping(
                pair("open", Command.present(new OpenItemList())),
                // intArg: Argument<Integer>
                // strArg: Argument<String>
                // AddItem::new = (Integer, String) -> AddItem
                pair("add", Command.argument(AddItem::new, intArg, strArg)),
                pair("remove", Command.argument(RemoveItem::new, intArg))
        )),
        pair("reload", Command.present(new ReloadCommand()))
);
// parsing
String[] args = new String[] {"item", "add", "0", "NAME"};
MyCommand algebra = Command.parseO(args, command).orElse(null);
// execution, check with if-instanceof.
// assumes `MyCommand` is sealed, treat like Enum.
// therefore, this is a valid type casting (not unsafe).
if (algebra instanceof MyCommand.AddItem) {
    MyCommand.AddItem addItem = (MyCommand.AddItem) algebra;
    println(String.format("Adding item %s, %s!", addItem.getIndex(), addItem.getName()));
} else if (algebra instanceof MyCommand.RemoveItem) {
    MyCommand.RemoveItem removeItem = (MyCommand.RemoveItem) algebra;
    println(String.format("Removing item %s", removeItem.getIndex()));
}
```

## Core

`Command.parse` accepts a successful result of `null` for every supported argument count (zero through seven), retains the parsed command node and consumed token index, and allows `map` to transform that result. Result factories are called only when every argument parses successfully. `Command.parseO` returns an empty `Optional` for a successful null result; use `Command.parse` to distinguish that case from a parsing failure.

Command and argument completion match prefixes case-insensitively using `Locale.ROOT`, independently of the JVM default locale, while preserving the original suggestion text. Default messages use the JVM language code: Korean locales use Korean messages, and unsupported languages fall back to English.

## Bukkit

`BukkitCommands.execute` resolves the selected command path and checks its permission before invoking argument parsers or result factories. The returned success retains the original command node. Sender-based tab completion applies the same permission check before argument completion or result parsing, and checks permission before calling the custom completer. A denied named command remains selected and cannot fall through to an allowed fallback.

The final element in Bukkit's completion arguments is the current partial token. While a declared argument is being entered, its argument completer owns completion. Once the declared arguments are supplied, the preceding tokens are parsed and the `registerPrime` custom completer receives the result. Invalid preceding input yields no custom completions; greedy arguments continue completing their current token.

Generated help includes named and fallback commands. `hideNoPermissionCommands` controls visibility before formatting; set it to `false` to display restricted command help. `BukkitCommandHelp.format` formats the supplied help without an additional permission filter, so direct callers should apply their visibility policy themselves. YAML normalization preserves null values, nested maps and collections, and insertion order.

Help sent to players by `BukkitCommands.execute` is clickable: each line uses the [Spigot Chat Component API](https://www.spigotmc.org/wiki/the-chat-component-api/) with `SUGGEST_COMMAND` to fill the chat input with the invoked command label and subcommand path. Commands accepting arguments include a trailing space for further input; argument placeholders and descriptions are excluded. Custom formatter text and colors are preserved. Console help and error messages remain plain text.

## Brigadier

Register a command tree using `BrigadierCommands.from` from the `command-brigadier` module:

```java
Command<Optional<Integer>> command = Command.argument(value -> value, intArg.asOptional());
dispatcher.register(BrigadierCommands.from("amount", command, (source, value) -> {
    // Both "amount" and "amount 42" execute this callback.
}));
```

`asOptional()` allows an argument to be omitted. Brigadier creates an execution path wherever all remaining arguments are optional, and passes only the supplied tokens to the core parser. Invalid supplied input still fails parsing. `map` and argument withers preserve the optional and greedy properties.

`strsArg` consumes all remaining whitespace-separated tokens, including zero tokens. A greedy argument must be last; registration rejects any other position. Quotes are literal characters in greedy tokens, so `echo "alpha beta"` supplies `["\"alpha", "beta\""]`. A single `strArg` uses Brigadier's quoted string syntax, so `name "alpha beta"` supplies one value, `alpha beta`.

Custom parsers can declare these properties using `withOptional(true)` and `withGreedy(true)`. These methods describe the parser's behavior; they do not change how it parses. Set `optional` only when the parser accepts an empty token list, and `greedy` only when it consumes every remaining token. The existing four-argument `of` and `ofContext` factories remain available and default to greedy for `List.class`. `ofUnary` always consumes one token, including when its result is a list. Scala's standard `strsArg` declares the same optional and greedy behavior for `Seq[String]`.

Argument completions filter candidates by the current prefix using `Locale.ROOT`. Quoted prefixes are decoded for matching, and single-value suggestions are escaped when necessary so that the inserted value can be parsed. Greedy suggestions replace only the current token. The contextual completer receives an immutable `ParseContext.args` list containing subcommand names, decoded preceding values, and the current typed fragment. The root command name and text after the completion cursor are excluded. Registration and completion do not invoke user parsers, result factories, or execution callbacks.

# FAQ

## Why not just execute?

```java
Command<Void> node = Command.mapping(
  pair("foo", Command.argument(integer -> {
    GlobalVariables.someVar = integer;
    System.out.println("Input is: " + integer); // here to break purity
    return null;
  }, intArg))
)
```

### 1. Lines too long

If you inline command implementation into the node declaration, it will easily to be ugly and hard to maintain.

### 2. Concurrent

You can't run `Command.parse` without synchronization, because it mutates global variable.

### 3. Type safety

Loose type safety even in typed programming language, you don't know what command is parsed.

### 4. Testing

Hard to test what it printed.

### 5. Reusability

Can't run the command implementation without parsing the command arguments.
