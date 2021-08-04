package fr.i360matt.fastboardplus;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScoreBoardAPI implements Listener {

    protected static ScheduledExecutorService timer;
    protected static boolean enabled = false;

    public static boolean isEnabled () {
        return enabled;
    }

    public static void disable () {
        enabled = false;
        for (final BoardView boardPlus : BoardView.boards.values()) {
            boardPlus.removeAllPlayers();
        }
    }

    public static void enable () {
        enabled = true;
        BoardPlayer.setAllToDefault();
    }

    public static void registerPlugin (final JavaPlugin plugin) {
        timer = Executors.newSingleThreadScheduledExecutor();
        Bukkit.getPluginManager().registerEvents(new ScoreBoardAPI(), plugin);
        enabled = true;
    }


    /* start interscoarboard events private methods */
    @EventHandler
    private static void eventMove (final PlayerMoveEvent event) {
        if (!event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()))
            for (final BoardView instanceCandidate : BoardView.boards.values())
                for (final BoardPlayer pBoardCandidate : instanceCandidate.getAllPlayerBoards())
                    instanceCandidate.onMove(pBoardCandidate);
    }

    @EventHandler
    private static void eventJoin (final PlayerJoinEvent event) {
        if (BoardView.defaultInstance != null)
            BoardPlayer.getPlayer(event.getPlayer()).setCurrent(BoardView.defaultInstance);

        for (final BoardView instanceCandidate : BoardView.boards.values())
            for (final BoardPlayer pBoardCandidate : instanceCandidate.getAllPlayerBoards())
                instanceCandidate.onOnlineChange(pBoardCandidate);
    }

    @EventHandler
    private static void eventQuit (final PlayerQuitEvent event) {
        BoardPlayer.deletePlayer(event.getPlayer());

        for (final BoardView instanceCandidate : BoardView.boards.values())
            for (final BoardPlayer pBoardCandidate : instanceCandidate.getAllPlayerBoards())
                instanceCandidate.onOnlineChange(pBoardCandidate);
    }
}
