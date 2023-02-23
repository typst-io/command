package io.typecraft.command;

import io.vavr.*;
import lombok.Data;
import lombok.With;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
@Data(staticConstructor = "of")
@With
public class Argument<A> {
    private final List<String> names;
    private final Function<List<String>, Tuple2<Optional<A>, List<String>>> parser;
    // TODO: Function<List<String>, Supplier<Collection<String>>>
    private final List<Supplier<List<String>>> tabCompleters;

    public static <A> Argument<A> ofUnary(
            String name,
            Function<String, Optional<A>> parser,
            Supplier<List<String>> tabCompleter
    ) {
        return of(
                Collections.singletonList(name),
                args -> {
                    List<String> newArgs = new ArrayList<>(args);
                    String arg = newArgs.size() >= 1 ? newArgs.remove(0) : "";
                    return new Tuple2<>(
                            arg.length() >= 1 ? parser.apply(arg) : Optional.empty(),
                            newArgs
                    );
                },
                Collections.singletonList(tabCompleter)
        );
    }

    public Argument<Optional<A>> asOptional() {
        return new Argument<>(
                getNames(),
                args -> getParser().apply(args).map1(Optional::of),
                getTabCompleters()
        );
    }

    public <B> Argument<B> map(Function<A, B> f) {
        return new Argument<>(
                getNames(),
                args -> {
                    Tuple2<Optional<A>, List<String>> pair = getParser().apply(args);
                    return pair.map1(a -> a.map(f));
                },
                getTabCompleters()
        );
    }

    public Argument<A> withTabCompleter(Supplier<List<String>> tabCompleter) {
        return withTabCompleters(Collections.singletonList(tabCompleter));
    }
}
