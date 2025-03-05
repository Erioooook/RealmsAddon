package xyz.telosaddon.yuno.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import xyz.telosaddon.yuno.TelosAddon;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TabListUtils {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final String pingPattern = "^Ping:\\s\\d+$";
    public static final String famePattern = "^Fame:\\s\\d+$";
    public static final String levelPattern = "^Class:.+$";
    public static final String serverPattern = "^Server:\\s.+$";

    public static Optional<String> getPattern(String pattern){
        Optional<String> ping = getLineMatches(pattern);
        if(ping.isEmpty()) return Optional.empty();
        String result = ping.get().split(":")[1];
        return Optional.of(result);
    }

    public static Optional<String> getPing(){
        return getPattern(pingPattern);
    }

    public static Optional<String> getServer(){
        return getPattern(serverPattern);
    }

    public static Optional<String> getCharInfo(){
        return getPattern(levelPattern);
    }

    public static Optional<String> getFame(){
        return getPattern(famePattern);
    }

    public static Optional<List<String>> getTabList(){
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        if(networkHandler == null) return Optional.empty();

        Collection<PlayerListEntry> playerCollection =  networkHandler.getPlayerList();
        List<PlayerListEntry> PlayerEntries = new ArrayList<>(playerCollection);

        if(PlayerEntries.isEmpty()) return Optional.empty();

        List<String> playerNameList = PlayerEntries.stream()
                .filter(player -> Objects.nonNull(player.getDisplayName()))
                .map(player -> player.getDisplayName().getString())
                .filter(Objects::nonNull)
                .map(playerString -> stripColors(playerString).trim())
                .filter(playerString -> !playerString.isEmpty())
                .toList();


        return Optional.of(playerNameList);
    }

    public static Optional<String> getLineMatches(String pattern){
        if (getTabList().isEmpty()) return Optional.empty();

        return getTabList().get().stream().filter(player -> Pattern.matches(pattern, player)).findFirst();
    }

    public static String stripAllFormatting(String input) {
        return stripColors(input).replaceAll("[^\\p{ASCII}]", "");
    }

    private static String stripColors(String input) {
        String withoutColors = input.replaceAll("§[0-9a-fk-or]", "");
        return withoutColors;
    }

}