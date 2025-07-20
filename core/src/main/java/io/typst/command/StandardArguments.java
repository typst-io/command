package io.typst.command;

import io.typst.command.algebra.Tuple2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StandardArguments {
    public static final Argument<String> strArg =
            Argument.ofUnary("string", String.class, Optional::of, Collections::emptyList);

    public static final Argument<Integer> intArg =
            Argument.ofUnary("int", Integer.class, Converters::parseInt, Collections::emptyList);

    public static final Argument<Long> longArg =
            Argument.ofUnary("long", Long.class, Converters::parseLong, Collections::emptyList);

    public static final Argument<Float> floatArg =
            Argument.ofUnary("float", Float.class, Converters::parseFloat, Collections::emptyList);

    public static final Argument<Double> doubleArg =
            Argument.ofUnary("double", Double.class, Converters::parseDouble, Collections::emptyList);

    public static final Argument<Boolean> boolArg =
            Argument.ofUnary("bool", Boolean.class, Converters::parseBoolean, () -> Arrays.asList("true", "false"));

    public static final Argument<List<String>> strsArg =
            Argument.of(
                    "strings",
                    List.class,
                    args -> new Tuple2<>(Optional.of(args), Collections.emptyList()),
                    Collections::emptyList
            );
}
