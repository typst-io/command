package io.typecraft.command.bukkit;

import io.typecraft.command.Argument;
import io.typecraft.command.algebra.Either;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class BukkitArguments {
    public static final Argument<Player> playerArg =
            Argument.ofUnary(
                    "player",
                    s -> Optional.ofNullable(Bukkit.getPlayer(s)),
                    () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())
            );
    @SuppressWarnings("deprecation")
    public static final Argument<OfflinePlayer> offlinePlayerArg =
            Argument.ofUnary(
                    "offlineplayer",
                    s -> Optional.of(Bukkit.getOfflinePlayer(s)),
                    () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())
            );

    public static final Argument<Material> materialArg =
            Argument.ofUnary(
                    "material",
                    s -> Either.catching(() -> Material.valueOf(s)).toJavaOptional(),
                    () -> Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList())
            );

    public static final Argument<PotionEffectType> potionArg =
            Argument.ofUnary(
                    "potion",
                    s -> Optional.ofNullable(PotionEffectType.getByName(s)),
                    () -> Arrays.stream(PotionEffectType.values())
                            .filter(Objects::nonNull)
                            .map(PotionEffectType::getName)
                            .collect(Collectors.toList())
            );

    @SuppressWarnings("deprecation")
    public static final Argument<Enchantment> enchantArg =
            Argument.ofUnary(
                    "enchant",
                    s -> Optional.ofNullable(Enchantment.getByName(s)),
                    () -> Arrays.stream(Enchantment.values())
                            .filter(Objects::nonNull)
                            .map(Enchantment::getName)
                            .collect(Collectors.toList())
            );
}
