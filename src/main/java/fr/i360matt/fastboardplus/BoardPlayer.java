package fr.i360matt.fastboardplus;


import fr.i360matt.fastboardplus.utils.FastBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BoardPlayer {
    public final Player player;
    public FastBoard board;

    public BoardPlus current;

    private static final Map<Player, BoardPlayer> players = new HashMap<>();
    public static BoardPlayer getPlayer (final Player player) {
        if (players.containsKey(player))
            return players.get(player);
        else {
            final BoardPlayer res = new BoardPlayer(player);
            players.put(player, res);
            return res;
        }
    }

    public static void deletePlayer (final Player player) {
        if (players.containsKey(player)) {
            final BoardPlayer pBoard = players.get(player);
            if (pBoard.current != null)
                pBoard.current.removePlayer(pBoard);
            if (pBoard.board != null)
                pBoard.board.delete();
            players.remove(player);
        }
    }

    public static void deleteAll () {
        final Iterator<Map.Entry<Player, BoardPlayer>> it = players.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Player, BoardPlayer> entry = it.next();
            final BoardPlayer pBoard = entry.getValue();
            if (pBoard.current != null)
                pBoard.current.removePlayer(pBoard);
            if (pBoard.board != null)
                pBoard.board.delete();
            it.remove();
        }

    }

    protected BoardPlayer (final Player player) {
        this.player = player;
        this.board = new FastBoard(player);
    }

    public void setCurrent (final BoardPlus scoreboard) {
        if (this.current != null)
            this.current.removePlayer(this);

        setBlank();

        (this.current = scoreboard).addPlayer(this);
    }

    public void setBlank () {
        if (this.current != null) {
            this.current.removePlayer(this);
            for (int i = 0; i <= 16; i++)
                this.board.removeLine(i);
            this.current = null;
        }
    }

    public void remove () {
        if (current != null)
            current.removePlayer(this);
        if (board != null)
            board.delete();
        players.remove(player);
    }

}
