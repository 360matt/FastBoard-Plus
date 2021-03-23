package fr.i360matt.fastboardplus;



import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;


public class ScoreBoardAPI {
    protected static BoardPlus defaultInstance;

    static Plugin registeredPlugin, disabled;
    public static void registerPlugin (final Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new BoardEvents(), plugin);
        registeredPlugin = plugin;
    }

    public static void unregisterPlugin () {
        if (registeredPlugin != null) {
            BoardPlus.timer.shutdownNow();
            BoardPlus.deleteAll();
            registeredPlugin = null;
        }
    }

    public static void disable () {
        if (registeredPlugin != null) {
            disabled = registeredPlugin;
            registeredPlugin = null;
        }
    }

    public static void enable () {
        if (disabled != null) {
            registeredPlugin = disabled;
            disabled = null;
        }

    }


    /**
     *
     * @return l'instance qui a été register par défaut
     */
    public static BoardPlus getDefaultInstance () { return defaultInstance; }
}
