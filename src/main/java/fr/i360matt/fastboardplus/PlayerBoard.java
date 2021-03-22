package fr.i360matt.fastboardplus;


import fr.i360matt.fastboardplus.utils.FastBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerBoard {
    public final Player player;
    public FastBoard board;

    public ScoreboardAbstract current;

    private static final Map<Player, PlayerBoard> cached = new HashMap<>();
    public static PlayerBoard getPlayer (final Player player) {
        if (cached.containsKey(player))
            return cached.get(player);
        else {
            final PlayerBoard res = new PlayerBoard(player);
            cached.put(player, res);
            return res;
        }
    }

    public static void removePlayer (final Player player) {
        if (cached.containsKey(player)) {
            final PlayerBoard pBoard = cached.get(player);
            if (pBoard.current != null)
                pBoard.current.removePlayer(pBoard);
            if (pBoard.board != null)
                pBoard.board.delete();
            cached.remove(player);
        }
    }

    public PlayerBoard (final Player player) {
        this.player = player;
        this.board = new FastBoard(player);
    }

    public void setCurrent (final ScoreboardAbstract scoreboard) {
        if (this.current != null)
            this.current.removePlayer(this);

        // this.board.delete();
        setBlank();

        // this.board = new FastBoard(player);

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

}
