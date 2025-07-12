package xyz.telosaddon.yuno.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "yuno")
public class YunoConfig implements ConfigData {
    
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    @ConfigEntry.Gui.Tooltip
    public int healthThreshold = 6;

    @ConfigEntry.Gui.Tooltip
    public boolean autoDropEnabled = true;

}  // ← вот эта закрывающая скобка была потеряна
