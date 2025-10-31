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
    implementation 'io.typst:command-bukkit:3.1.4'
}
```

## Maven

```xml
<dependency>
    <groupId>io.typst</groupId>
    <artifactId>command-bukkit</artifactId>
    <version>3.1.4</version>
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
