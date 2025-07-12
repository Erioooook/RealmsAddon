package xyz.telosaddon.yuno;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import xyz.telosaddon.yuno.config.YunoConfigManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HealthDropClient implements ClientModInitializer {

    private static final ConcurrentHashMap<UUID, Float> lastHealth = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        // 1) Загрузить или создать конфиг
        YunoConfigManager.init();

        // 2) Команда /sethptrg <threshold>
        ClientCommandRegistrationCallback.EVENT.register((disp, reg) ->
            disp.register(ClientCommandManager.literal("sethptrg")
                .then(ClientCommandManager.argument("threshold", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> {
                        int thr = IntegerArgumentType.getInteger(ctx, "threshold");
                        ClientPlayerEntity pl = MinecraftClient.getInstance().player;
                        if (pl != null) {
                            YunoConfigManager.setThreshold(thr);
                            pl.sendMessage(Text.literal("§aThreshold сохранён: " + thr), false);
                        }
                        return 1;
                    })
                )
            )
        );

        // (Опционально) команда /togglehptrg
        ClientCommandRegistrationCallback.EVENT.register((disp, reg) ->
            disp.register(ClientCommandManager.literal("togglehptrg")
                .executes(ctx -> {
                    ClientPlayerEntity pl = MinecraftClient.getInstance().player;
                    if (pl != null) {
                        YunoConfigManager.toggleEnabled();
                        pl.sendMessage(Text.literal(
                            "§aAutodrop " + (YunoConfigManager.isEnabled() ? "включён" : "отключён")),
                            false
                        );
                    }
                    return 1;
                })
            )
        );

        // 3) Тик-обработчик автодропа
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity pl = client.player;
            if (pl == null) return;

            UUID id = pl.getUuid();
            float prev = lastHealth.getOrDefault(id, pl.getHealth());
            float curr = pl.getHealth();

            if (YunoConfigManager.isEnabled() &&
                prev > YunoConfigManager.getThreshold() &&
                curr <= YunoConfigManager.getThreshold()) {
                pl.dropSelectedItem(false);
            }

            lastHealth.put(id, curr);
        });
    }
}
