package xyz.telosaddon.yuno.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "yuno")    // файл будет yuno.json в папке config
public class YunoConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 20)
    public double healthThreshold = 6.0;

    @ConfigEntry.Gui.Tooltip
    public boolean autoDropEnabled = true;

}
