package io.typecraft.command.bukkit;

import io.typecraft.command.Argument;
import io.typecraft.command.bukkit.i18n.BukkitLangId;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class BukkitArgument {
    public static final Argument<Player> playerArg =
            Argument.ofUnary(
                    BukkitLangId.typeBukkitPlayer,
                    s -> Optional.ofNullable(Bukkit.getPlayer(s)),
                    () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())
            );
    @SuppressWarnings("deprecation")
    public static final Argument<OfflinePlayer> offlinePlayerArg =
            Argument.ofUnary(
                    BukkitLangId.typeBukkitOfflinePlayer,
                    s -> Optional.of(Bukkit.getOfflinePlayer(s)),
                    () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())
            );

    public static final Argument<Material> materialArg =
            Argument.ofUnary(
                    BukkitLangId.typeMaterial,
                    s -> Try.of(() -> Material.valueOf(s)).toJavaOptional(),
                    () -> Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList())
            );
}
