# Bukkit-groovy

This plugin allows you to run groovy scripts on the server.

Make sure to read the caveats chapter if you encounter any problems first!


## Why groovy?

You can re-run scripts on the fly which can drastically speed up the development cycle as 
you can test your code without restarting the server or reloading the whole plugin. Also, 
groovy can run basic java code without any modifications and adds some QoL changes to the
plain old java. It can also call any java functions visible to the plugin directly, no need 
to maintain the interface manually.


## Building the project

The project uses maven. You can simply build it via:
```shell
mvn package
```


## Writing groovy scripts

Scripts must be in the `[server_folder]/scripts` folder by default.

Every groovy script is expected to return with an object that implements the 
`LoadableScript` interface. It has 2 functions:

```java
public interface LoadableScript {
    void onLoad(JavaPlugin javaPlugin);
    void onUnload(JavaPlugin javaPlugin);
}
```

The return value of a script is the return value of the last line. See 
[scripts/on_login.groovy](scripts/on_login.groovy) as an example.

The `onLoad` will be called when the script is loaded and the `onUnload` when it is 
unloaded. These are used to support reloading a script without restarting the server.


### Sharing variables between scripts

The `Binding` object is shared between all loaded scripts, so if you need to share values 
just add it to it. Be aware these won't be automatically saved, so you must use some other 
methods (e.g. SQL) if you need a persistent variable.


## Loading and reloading scripts

Loading / unloading scripts require the `minecraft.command.debug` permission. Loading scripts
can be done via the `gx` user command:

```
/gx my_script.groovy
```
(`gx` stands for "groovy execute (script)". The `.groovy` extension is not necessary.)

If the script was already loaded the `onUnload` function will be automatically called 
before loading it again.

You can also load or reload all scripts with the `/gxall` command that has no arguments.

You can unload a script via
```
/gxunload my_script.groovy
```
(Extension is not necessary here either.)


## Caveats

### Always set the return value of event listener functions explicitly to void

If you use `def` when declaring an event handler it might (never?) set the return value
to `void`, even if the function doesn't return with anything. For most plugins this only
results in a warning, but for this plugin it causes an error, see
[https://bukkit.org/threads/adding-event-listener-from-groovy.496144/](https://bukkit.org/threads/adding-event-listener-from-groovy.496144/)

Event handler functions that don't return void are unsupported, so it's a good practice
anyway - for this plugin it's a necessity.


### Don't put a folder among the scripts that's name ends with '.groovy'

Due to a weird java bug I cannot filter out folders from the list of paths to be loaded by 
groovy, but only ones that ends with '.groovy' will be loaded, so if you put a folder like 
`something.groovy` inside the plugin will try to execute it as a script - probably won't 
work.