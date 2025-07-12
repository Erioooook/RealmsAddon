package xyz.telosaddon.yuno;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import xyz.telosaddon.yuno.config.YunoConfig;

public class HealthDropClient implements ClientModInitializer {

    // держим холдер конфига
    public static ConfigHolder<YunoConfig> CONFIG;

    @Override
    public void onInitializeClient() {
        // 1. Регистрируем AutoConfig и подгружаем конфиг (создаст config/yuno.json)
        CONFIG = AutoConfig.register(YunoConfig.class, GsonConfigSerializer::new);

        // 2. Команда /sethptrg <threshold>
        ClientCommandRegistrationCallback.EVENT.register((disp, reg) ->
            disp.register(ClientCommandManager.literal("sethptrg")
                .then(ClientCommandManager.argument("threshold", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> {
                        int thr = IntegerArgumentType.getInteger(ctx, "threshold");
                        ClientPlayerEntity pl = MinecraftClient.getInstance().player;
                        if (pl != null) {
                            // сохраним в конфиг и запишем на диск
                            CONFIG.getConfig().healthThreshold = thr;
                            CONFIG.save();

                            pl.sendMessage(
                                Text.literal("§aThreshold сохранён: " + thr),
                                false
                            );
                        }
                        return 1;
                    })
                )
            )
        );

        // 3. Тик-обработчик автодропа
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity pl = client.player;
            if (pl == null) return;

            YunoConfig cfg = CONFIG.getConfig();
            if (!cfg.autoDropEnabled) return;

            float currentHp = pl.getHealth();
            // здесь можно сравнивать с предыдущим тиком,
            // но если надо просто при любом падении ≤ порога — так:
            if (currentHp <= cfg.healthThreshold) {
                pl.dropSelectedItem(false);
            }
        });
    }
}
