import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.Listener
import org.bukkit.Bukkit

import com.tndt.LoadableScript
import org.bukkit.plugin.java.JavaPlugin

//loginListener = new LoginListener()

class LoginListener implements Listener {
    @EventHandler
    void onLogin(PlayerLoginEvent pe) {
        var name = pe.getPlayer().getName()
        //pe.getPlayer().sendMessage(String.format("Hello %s!", name))
        Bukkit.getLogger().info("BBBB " + name + " has joined the server")
    }
}

class OnLogin implements LoadableScript, Listener {
    def loginListener = new LoginListener()

    void onLoad(JavaPlugin plugin) {
        println "on_login script load function called"
        plugin.getServer().getPluginManager().registerEvents(loginListener, plugin)
    }

    void onUnload(JavaPlugin plugin) {
        println "on_login script unload function called"
        HandlerList.unregisterAll(loginListener)
    }
}

new OnLogin()