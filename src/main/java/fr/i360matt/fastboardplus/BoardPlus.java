package fr.i360matt.fastboardplus;

import fr.i360matt.fastboardplus.utils.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class BoardPlus {
    protected static ScheduledExecutorService timer = Executors.newScheduledThreadPool(128);
    private final Map<Integer, Set<Consumer<FastBoard>>> consumers = new HashMap<>();
    protected static final Map<String, BoardPlus> boards = new HashMap<>();

    /**
     * Permet d'update le scoreboard pour tous les joueurs
     *
     * @param instance le nom du scoreboard qui permettra de retrouver l'instance
     * @param board le board des joueurs qui sera loop
     */
    public static void updateAllBoards (final String instance, final Consumer<FastBoard> board) {
        for (final BoardPlayer playerBoard : BoardPlus.getInstance(instance).playerBoards) {
            board.accept(playerBoard.board);
        }
    }

    public static void deleteAll () {
        boards.values().forEach(BoardPlus::unregister);
    }

    /**
     *
     * @param name le nom de l'instance qui nous interresse
     * @return l'instance en question
     */
    public static BoardPlus getInstance (final String name) { return boards.get(name); }


    private String name;

    public BoardPlus () {
        registerYourSchedulersHere();
    }

    /* start registration */
    public final void register (final String name) {
        boards.put(name, this);
    }
    public final void registerAsDefault (final String name) {
        this.name = name;
        ScoreBoardAPI.defaultInstance = this;
        boards.put(name, this);
    }

    public final void unregister () {
        if (ScoreBoardAPI.defaultInstance.equals(this))
            ScoreBoardAPI.defaultInstance = null;
        consumers.clear();
        removeAllPlayers();
        boards.remove(name);
    }

    /* end registration */



    /* Start player related */
    private final Set<BoardPlayer> playerBoards = ConcurrentHashMap.newKeySet();
    public final Set<BoardPlayer> getAllPlayerBoards () { return playerBoards; }
    public void addPlayer (final BoardPlayer pBoard) {
        playerBoards.add(pBoard);
        init(pBoard.board);
    }
    public void addPlayer (final Player player) {
        addPlayer(BoardPlayer.getPlayer(player));
    }
    public void removeAllPlayers () {
        this.playerBoards.forEach(BoardPlayer::remove);
    }
    protected void removePlayer (final BoardPlayer pBoard) { playerBoards.remove(pBoard); }
    protected boolean isPresentPlayer (final Player player) { return playerBoards.contains(BoardPlayer.getPlayer(player)); }
    protected boolean isPresentPlayer (final BoardPlayer pBoard) { return playerBoards.contains(pBoard); }
    /*   END player related   */



    /*   Start internal methods / fields   */
    public static class SchedulingBascule {
        protected Object old;
        protected Object value;
        public void setValue (final Object obj) {
            this.value = obj;
        }
    }
    /*   END internal methods / fields   */









    /* start of API use */

    public abstract void init (final FastBoard board);
    public abstract void registerYourSchedulersHere ();

    /* predef events */
    public void onMove (final FastBoard board) {}
    public void onOnlineChange (final FastBoard board) {}


    public void schedule (int tick, final Consumer<FastBoard> lambda) {
        if (!consumers.containsKey(tick)) {
            final Set<Consumer<FastBoard>> set = new HashSet<>();
            set.add(lambda);
            consumers.put(tick, set);

            timer.scheduleAtFixedRate(() -> {
                if (ScoreBoardAPI.registeredPlugin != null) {
                    playerBoards.forEach(playerRelated -> {
                        consumers.get(tick).forEach(consum -> {
                            consum.accept(playerRelated.board);
                        });
                    });
                }
            },0, 50L*tick, TimeUnit.MILLISECONDS);
        } else {
            consumers.get(tick).add(lambda);
        }
    }

    public void basculeIfUnchanging (int period, final Player player, final BoardPlus scb, final SchedulingBascule bascule) {
        final ScheduledExecutorService task = Executors.newScheduledThreadPool(1);
        task.scheduleAtFixedRate(() -> {
            if (ScoreBoardAPI.registeredPlugin != null && isPresentPlayer(player)) {
                if (bascule.old == null || bascule.old.equals(bascule.value)) {
                    bascule.old = bascule.value;
                    return;
                }
                BoardPlayer.getPlayer(player).setCurrent(scb);
            }
            task.shutdownNow();
        }, 50L*period, 50L*period, TimeUnit.MILLISECONDS);
    }

    /* end of API use */


}
