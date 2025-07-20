package io.typst.command;

import io.typst.command.algebra.Tuple2;
import lombok.Value;
import lombok.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("DuplicatedCode")
@Value(staticConstructor = "of")
@With
public class Argument<A> {
    String name;
    Class<?> classType;
    Function<List<String>, Tuple2<Optional<A>, List<String>>> parser;
    Supplier<List<String>> tabCompletes;

    public static <A> Argument<A> ofUnary(
            String name,
            Class<?> classType,
            Function<String, Optional<A>> parser,
            Supplier<List<String>> tabCompleter
    ) {
        return of(
                name,
                classType,
                args -> {
                    List<String> newArgs = new ArrayList<>(args);
                    String arg = newArgs.size() >= 1 ? newArgs.remove(0) : "";
                    return new Tuple2<>(
                            arg.length() >= 1 ? parser.apply(arg) : Optional.empty(),
                            newArgs
                    );
                },
                tabCompleter
        );
    }

    public Argument<Optional<A>> asOptional() {
        return new Argument<>(
                name,
                classType,
                args -> getParser().apply(args).map1(Optional::of),
                getTabCompletes()
        );
    }

    public <B> Argument<B> map(Function<A, B> f) {
        return new Argument<>(
                name,
                classType,
                args -> {
                    Tuple2<Optional<A>, List<String>> pair = getParser().apply(args);
                    return pair.map1(a -> a.map(f));
                },
                getTabCompletes()
        );
    }
}
