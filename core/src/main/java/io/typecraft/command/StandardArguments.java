package io.typecraft.command;

import io.typecraft.command.algebra.Tuple2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StandardArguments {
    public static final Argument<String> strArg =
            Argument.ofUnary("string", Optional::of, Collections::emptyList);

    public static final Argument<Integer> intArg =
            Argument.ofUnary("int", Converters::parseInt, Collections::emptyList);

    public static final Argument<Long> longArg =
            Argument.ofUnary("long", Converters::parseLong, Collections::emptyList);

    public static final Argument<Float> floatArg =
            Argument.ofUnary("float", Converters::parseFloat, Collections::emptyList);

    public static final Argument<Double> doubleArg =
            Argument.ofUnary("double", Converters::parseDouble, Collections::emptyList);

    public static final Argument<Boolean> boolArg =
            Argument.ofUnary("bool", Converters::parseBoolean, () -> Arrays.asList("true", "false"));

    public static final Argument<List<String>> strsArg =
            Argument.of(
                    Collections.singletonList("strings"),
                    args -> new Tuple2<>(Optional.of(args), Collections.emptyList()),
                    Collections.emptyList()
            );
}
