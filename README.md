# Command

A typesafe command line parser.

```java
// core/src/test/../CommandTest.java
// MyCommand = AddItem | RemoveItem | ...
Command.map(
        pair("item", Command.map(
                pair("open", Command.present(new OpenItemList())),
                // intArg: Argument<Integer>
                // strArg: Argument<String>
                // AddItem::new = (Integer, String) -> AddItem
                pair("add", Command.argument(AddItem::new, intArg, strArg)),
                pair("remove", Command.argument(RemoveItem::new, intArg))
        )),
        pair("reload", Command.present(new ReloadCommand()))
);
```
