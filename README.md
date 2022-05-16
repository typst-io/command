# Command

A typesafe command line parser.

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
