package io.typst.command;

import lombok.Value;
import lombok.With;

import java.util.List;

@Value
@With
public class ParseContext {
    CommandSource source;
    List<String> args;
}
