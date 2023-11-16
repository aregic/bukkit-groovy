package com.tndt;

import groovy.lang.*;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class GroovyEngine {
    public GroovyEngine(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        binding = new Binding();
        groovyClassLoader =  new GroovyClassLoader(javaPlugin.getClass().getClassLoader());
        String[] paths = {scriptsFolder};
        try {
            groovyScriptEngine = new GroovyScriptEngine(paths, groovyClassLoader);
            Bukkit.getLogger().info("Groovy script engine loaded.");
        } catch (IOException e) {
            groovyScriptEngine = null;
            Bukkit.getLogger().warning(e.getMessage());
            Bukkit.getLogger().warning(ExceptionUtils.getStackTrace(e));
        }
        scripts = new HashMap<>();
        commandScripts = new HashMap<>();
        customCommands = new HashMap<>();
    }

    public void loadAllScripts() {
        File file = new File(scriptsFolder);
        if (!file.exists()) {
            Bukkit.getLogger().warning("Scripts folder " + scriptsFolder + " does not exist! (Ok on first load)");
            if (!file.mkdir()) {
                String errormsg = "Scripts folder " + scriptsFolder + " could not be created so no scripts " +
                        "will be loaded.";
                Bukkit.getLogger().severe(errormsg);
                throw new RuntimeException(errormsg);
            }
        }
        List<File> scriptFiles = getScriptFiles(file);
        for (File f: scriptFiles){
            loadScript(f.getAbsolutePath());
        }
    }

    public void unloadAllScripts() {
        if (scripts != null) {
            for (String s : scripts.keySet()) {
                scripts.get(s).onUnload(javaPlugin);
            }
            scripts.clear();
        }
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

    List<File> getScriptFiles(File folder) {
        // This check shouldn't be necessary but isDirectory behaves in a weird way, see below
        if (folder == null || ! folder.isDirectory())
            return new ArrayList<File>();
        Bukkit.getLogger().info("Folder: " + folder.getPath());
        Bukkit.getLogger().info("In folder: " + Arrays.toString(folder.listFiles()));
        File[] fileList = folder.listFiles(
                // due to a weird bug dir.isDirectory() will be true even for files
                // maybe due to scripts folder being a soft link on my system? (linux)
                (dir, name) -> name.toLowerCase().endsWith(".groovy")
        );
        ArrayList<File> files = new ArrayList<>(Arrays.asList(fileList));
        // For now loading sub-files is not necessary and doesn't work
        /*
        Bukkit.getLogger().info("File list: " + Arrays.toString(fileList));
        File[] folders = folder.listFiles(
                (file, name) -> file.isDirectory()
        );
        Bukkit.getLogger().info("Subdirs: " + Arrays.toString(folders));
        for (File f : folders)
            files.addAll(getScriptFiles(f));
        */
        return files;
    }

    void loadScript(String name) {
        String fullName = getFullName(name);

        if (scripts.containsKey(fullName))
            unloadScript(fullName);

        try {
            //File f = new File(scriptsFolder + fullName);
            Object scriptObj = groovyScriptEngine.run(fullName, binding);
            if (scriptObj instanceof LoadableScript) {
                LoadableScript script = (LoadableScript) scriptObj;
                scripts.put(fullName, script);
                script.onLoad(javaPlugin);
            }
            if (scriptObj instanceof ScriptCommandExecutor) {
                commandScripts.put(fullName, (ScriptCommandExecutor) scriptObj);
                Bukkit.getLogger().info("Script with ScriptCommandExecutor loaded: " + fullName);
            }
            Bukkit.getLogger().info("Script loaded: " + fullName);
        //} catch (ResourceException | ScriptException | RuntimeException | Exception e) {
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
            Bukkit.getLogger().warning(ExceptionUtils.getStackTrace(e));
        }
    }

    boolean executeCommand(@NotNull CommandSender sender,
                        @NotNull String[] args,
                        @NotNull String scriptName) {
        String fullName = getFullName(scriptName);
        if (commandScripts.containsKey(fullName)) {
            String[] newArgs = {};
            if (args.length > 0) {
                if (args.length > 1)
                    newArgs = Arrays.copyOfRange(args, 1, args.length);
                String commandName = args[0];
                return commandScripts.get(fullName).onCommand(sender, commandName, newArgs, scriptName);
            } else {
                sender.sendMessage("Missing second parameter: it must be a command.");
                return false;
            }

        } else {
            sender.sendMessage("Script " + scriptName + " is not found.");
            return false;
        }
    }

    // add file extension if not present
    static String getFullName(String scriptName) {
        String fullName = scriptName;
        if (fullName.contains("/")) {
            String[] f = fullName.split("/");
            fullName = f[f.length-1];
        }
        if (!scriptName.endsWith(".groovy"))
            fullName = scriptName + ".groovy";
        return fullName;
    }

    private GroovyScriptEngine groovyScriptEngine;

    private final GroovyClassLoader groovyClassLoader;
    private final Binding binding;
    private final HashMap<String, LoadableScript> scripts;
    private final HashMap<String, ScriptCommandExecutor> commandScripts;
    private final JavaPlugin javaPlugin;
    private final HashMap<String, ScriptCommandExecutor> customCommands;

    final private static String scriptsFolder = "scripts/";
}