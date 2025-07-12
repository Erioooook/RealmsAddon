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

    // пороги здоровья игроков
    private static final Map<UUID, Float> thresholds    = new ConcurrentHashMap<>();
    // здоровье из предыдущего тика
    private static final Map<UUID, Float> lastHealth    = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        // регистрация команды /sethptrg <threshold>
        ClientCommandRegistrationCallback.EVENT.register((disp, reg) ->
            disp.register(ClientCommandManager.literal("sethptrg")
                .then(ClientCommandManager.argument("threshold", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> {
                        int thr = IntegerArgumentType.getInteger(ctx, "threshold");
                        ClientPlayerEntity pl = MinecraftClient.getInstance().player;
                        if (pl != null) {
                            thresholds.put(pl.getUuid(), (float) thr);
                            pl.sendMessage(Text.literal("§aThreshold установлено: " + thr), false);
                        }
                        return 1;
                    })
                )
            )
        );

        // детектор спуска здоровья
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity pl = client.player;
            if (pl == null) return;

            UUID id = pl.getUuid();
            float currentHp = pl.getHealth();
            float previousHp = lastHealth.getOrDefault(id, currentHp);
            Float thr = thresholds.get(id);

            if (thr != null) {
                // если в прошлом тике было выше порога, а теперь ≤ порога
                if (previousHp > thr && currentHp <= thr) {
                    pl.dropSelectedItem(false);
                }
            }

            // сохраняем текущее здоровье для следующего тика
            lastHealth.put(id, currentHp);
        });
    }
}
