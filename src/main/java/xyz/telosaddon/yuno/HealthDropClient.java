package xyz.telosaddon.yuno;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HealthDropClient implements ClientModInitializer {
    private static final Map<UUID, Float> thresholds = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        // 1) Команда /sethptrg <threshold>
        ClientCommandRegistrationCallback.EVENT.register((disp, reg) ->
            disp.register(ClientCommandManager.literal("sethptrg")
                .then(ClientCommandManager.argument("threshold", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> {
                        int thr = IntegerArgumentType.getInteger(ctx, "threshold");
                        ClientPlayerEntity player = MinecraftClient.getInstance().player;
                        if (player != null) {
                            thresholds.put(player.getUuid(), (float) thr);
                            player.sendMessage(Text.literal("§aHealth threshold set to " + thr), false);
                        }
                        return 1;
                    })
                )
            )
        );

        // 2) Автодроп при падении здоровья
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) return;
            Float thr = thresholds.get(player.getUuid());
            if (thr != null && player.getHealth() <= thr) {
                player.dropSelectedItem(false);
                thresholds.remove(player.getUuid());
            }
        });
    }
}
