package fr.i360matt.fastboardplus;



import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executors;


public class ScoreBoardAPI {
    protected static BoardPlus defaultInstance;

    protected static Plugin registeredPlugin, disabled;
    public static void registerPlugin (final Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new BoardEvents(), plugin);
        registeredPlugin = plugin;
        startService();
    }

    public static void unregisterPlugin () {
        if (registeredPlugin != null) {
            BoardPlus.timer.shutdownNow();
            BoardPlus.deleteAll();
            registeredPlugin = null;
            stopService();
        }
    }

    public static void disable () {
        if (registeredPlugin != null) {
            disabled = registeredPlugin;
            registeredPlugin = null;
            stopService();
        }
    }

    public static void enable () {
        if (disabled != null) {
            registeredPlugin = disabled;
            disabled = null;
            startService();
        }
    }

    private static void startService () {
        if (BoardPlus.timer == null || BoardPlus.timer.isShutdown())
            BoardPlus.timer = Executors.newScheduledThreadPool(128);
    }

    private static void stopService () {
        if (BoardPlus.timer != null && !BoardPlus.timer.isShutdown())
            BoardPlus.timer.shutdownNow();
    }


    /**
     *
     * @return l'instance qui a été register par défaut
     */
    public static BoardPlus getDefaultInstance () { return defaultInstance; }
}
