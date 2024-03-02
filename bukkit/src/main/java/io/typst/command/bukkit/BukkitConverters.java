package io.typst.command.bukkit;

import io.typst.command.Converters;
import io.typst.command.algebra.Tuple2;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Bukkit converters, following naming conventions:
 * <ul>
 *     <li>parse*: string parsing</li>
 *     <li>as*: type casting</li>
 *     <li>to*: value computation</li>
 *     <li>normalize*: object normalization</li>
 * </ul>
 */
@UtilityClass
public class BukkitConverters {
    public static Object normalizeYamlObject(Object obj) {
        if (obj instanceof Collection) {
            return Converters.toCollectionAs(
                    BukkitConverters::normalizeYamlObject,
                    obj
            ).orElse(Collections.emptyList());
        } else if (obj instanceof Map) {
            return Converters.toMapAs(
                    pair -> new Tuple2<>(
                            pair.getA(),
                            normalizeYamlObject(pair.getB())
                    ),
                    obj
            ).orElse(Collections.emptyMap());
        } else if (obj instanceof ConfigurationSection) {
            return normalizeYamlObject(((ConfigurationSection) obj).getValues(false));
        } else {
            return obj;
        }
    }

    public static Map<String, Object> normalizeYamlMap(Map<String, Object> map) {
        return Converters.toMapAs(
                pair -> new Tuple2<>(
                        pair.getA().toString(),
                        normalizeYamlObject(pair.getB())
                ),
                map
        ).orElse(Collections.emptyMap());
    }
}
