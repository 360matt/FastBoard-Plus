package fr.i360matt.fastboardplus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BoardEvents implements Listener {
    /* start interscoarboard events private methods */
    @EventHandler
    private static void eventMove (final PlayerMoveEvent event) {
        if (ScoreBoardAPI.registeredPlugin == null) return;
        if (!event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()))
            for (final BoardPlus instanceCandidate : BoardPlus.boards.values())
                for (final BoardPlayer pBoardCandidate : instanceCandidate.getAllPlayerBoards())
                    instanceCandidate.onMove(pBoardCandidate.board);
    }

    @EventHandler
    private static void eventJoin (final PlayerJoinEvent event) {
        if (ScoreBoardAPI.registeredPlugin == null) return;
        if (ScoreBoardAPI.defaultInstance != null)
            BoardPlayer.getPlayer(event.getPlayer()).setCurrent(ScoreBoardAPI.defaultInstance);

        for (final BoardPlus instanceCandidate : BoardPlus.boards.values())
            for (final BoardPlayer pBoardCandidate : instanceCandidate.getAllPlayerBoards())
                instanceCandidate.onOnlineChange(pBoardCandidate.board);
    }

    @EventHandler
    private static void eventQuit (final PlayerQuitEvent event) {
        BoardPlayer.deletePlayer(event.getPlayer());
        if (ScoreBoardAPI.registeredPlugin == null) return;

        for (final BoardPlus instanceCandidate : BoardPlus.boards.values())
            for (final BoardPlayer pBoardCandidate : instanceCandidate.getAllPlayerBoards())
                instanceCandidate.onOnlineChange(pBoardCandidate.board);
    }
}
