package com.tndt;

import org.bukkit.plugin.java.JavaPlugin;

public interface LoadableScript {
    void onLoad(JavaPlugin javaPlugin);
    void onUnload(JavaPlugin javaPlugin);
}
