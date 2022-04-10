package com.tndt;

import groovy.lang.*;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class GroovyEngine {
    public GroovyEngine(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        binding = new Binding();
        groovyClassLoader =  new GroovyClassLoader();
        String[] paths = {scriptsFolder};
        try {
            groovyScriptEngine = new GroovyScriptEngine(paths);
            Bukkit.getLogger().info("Groovy script engine loaded.");
        } catch (IOException e) {
            groovyScriptEngine = null;
            Bukkit.getLogger().warning(e.getMessage());
            Bukkit.getLogger().warning(ExceptionUtils.getStackTrace(e));
        }
        scripts = new HashMap<>();

    }

    public void loadAllScripts() {
        File[] scriptFiles = getScriptFiles();
        for (File f: scriptFiles){
            loadScript(f.getName());
        }
    }

    public void unloadAllScripts() {
        for(String s: scripts.keySet())
            unloadScript(s);
    }

    public void unloadScript(String name) {
        String fullName = getFullName(name);
        if (scripts.containsKey(fullName)){
            scripts.get(fullName).onUnload(javaPlugin);
            scripts.remove(fullName);
        } else {
            Bukkit.getLogger().warning("Trying to unload script that was never loaded");
        }
    }

    File[] getScriptFiles() {
        File folder = new File(scriptsFolder);
        return folder.listFiles(
                // due to a weird bug dir.isDirectory() will be true even for files
                // maybe due to scripts folder being a soft link on my system? (linux)
                (dir, name) -> name.toLowerCase().endsWith(".groovy")
        );
    }

    void loadScript(String name) {
        String fullName = getFullName(name);

        if (scripts.containsKey(fullName))
            unloadScript(fullName);

        try {
            File f = new File(scriptsFolder + fullName);
            LoadableScript script = (LoadableScript) groovyScriptEngine.run(fullName, binding);
            scripts.put(fullName, script);
            script.onLoad(javaPlugin);
            Bukkit.getLogger().info("Script loaded: " + fullName);
        } catch (GroovyRuntimeException | ResourceException | ScriptException e) {
            Bukkit.getLogger().warning(e.getMessage());
            Bukkit.getLogger().warning(ExceptionUtils.getStackTrace(e));
        }
    }

    // add file extension if not present
    static String getFullName(String scriptName) {
        String fullName = scriptName;
        if (!scriptName.endsWith(".groovy"))
            fullName = scriptName + ".groovy";
        return fullName;
    }

    private GroovyScriptEngine groovyScriptEngine;

    private final GroovyClassLoader groovyClassLoader;
    private final Binding binding;
    private final HashMap<String, LoadableScript> scripts;
    private final JavaPlugin javaPlugin;

    final private static String scriptsFolder = "scripts/";
}