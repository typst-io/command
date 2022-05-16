package io.typecraft.command.bukkit;

import io.typecraft.command.Argument;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class BukkitArgument {
    public static final Argument<Player> playerArg =
            Argument.ofUnary(
                    "player",
                    s -> Optional.ofNullable(Bukkit.getPlayer(s)),
                    () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())
            );
    @SuppressWarnings("deprecation")
    public static final Argument<OfflinePlayer> offlinePlayerArg =
            Argument.ofUnary(
                    "offline-player",
                    s -> Optional.of(Bukkit.getOfflinePlayer(s)),
                    () -> Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList())
            );
}
