package fr.i360matt.fastboardplus;



import fr.i360matt.fastboardplus.utils.FastBoard;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class ScoreBoardAPI {

    protected static BoardPlus defaultInstance;

    static Plugin registeredPlugin, disabled;
    public static void registerPlugin (final Plugin plugin) {
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
        disabled = registeredPlugin;
        registeredPlugin = null;
    }

    public static void enable () {
        registeredPlugin = disabled;
        disabled = null;
    }


    /**
     *
     * @return l'instance qui a été register par défaut
     */
    public static BoardPlus getDefaultInstance () { return defaultInstance; }




}
