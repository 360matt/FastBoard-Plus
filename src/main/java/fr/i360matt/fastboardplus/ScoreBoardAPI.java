package fr.i360matt.fastboardplus;



import fr.i360matt.fastboardplus.utils.FastBoard;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class ScoreBoardAPI {

    protected static ScoreboardAbstract defaultInstance;

    static Plugin registeredPlugin;
    public static void registerPlugin (final Plugin plugin) {
        registeredPlugin = plugin;
    }


    /**
     *
     * @return l'instance qui a été register par défaut
     */
    public static ScoreboardAbstract getDefaultInstance () { return defaultInstance; }

    /**
     *
     * @param name le nom de l'instance qui nous interresse
     * @return l'instance en question
     */
    public static ScoreboardAbstract getInstance (final String name) { return ScoreboardAbstract.allInstances.get(name); }

    /**
     * Permet d'update le scoreboard pour tous les joueurs
     *
     * @param instance le nom du scoreboard qui permettra de retrouver l'instance
     * @param board le board des joueurs qui sera loop
     */
    public static void updateAllBoards (final String instance, final Consumer<FastBoard> board) {
        for (final PlayerBoard playerBoard : getInstance(instance).playerBoards) {
            board.accept(playerBoard.board);
        }
    }


}
