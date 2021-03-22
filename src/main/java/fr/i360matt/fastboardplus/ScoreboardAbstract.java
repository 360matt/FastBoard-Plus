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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class ScoreboardAbstract implements Listener {
    protected static ScheduledExecutorService timer = Executors.newScheduledThreadPool(128);
    public static final Map<String, ScoreboardAbstract> allInstances = new HashMap<>();

    protected final boolean registered;

    public ScoreboardAbstract() {
        Bukkit.getPluginManager().registerEvents(this, ScoreBoardAPI.registeredPlugin);
        registerYourSchedulersHere();
        this.registered = true;
    }

    /* start registration */
    public final void register (final String name) {
        allInstances.put(name, this);
    }
    public final void registerAsDefault (final String name) {
        ScoreBoardAPI.defaultInstance = this;
        allInstances.put(name, this);
    }
    /* end registration */



    /* Start player related */
    protected final Set<PlayerBoard> playerBoards = ConcurrentHashMap.newKeySet();
    public final Set<PlayerBoard> getAllBoards () { return playerBoards; }
    public void addPlayer (PlayerBoard pBoard) {
        playerBoards.add(pBoard);
        init(pBoard.board);
    }
    protected void removePlayer (final PlayerBoard pBoard) { playerBoards.remove(pBoard); }
    protected boolean isPresentPlayer (final Player player) { return playerBoards.contains(PlayerBoard.getPlayer(player)); }
    protected boolean isPresentPlayer (final PlayerBoard pBoard) { return playerBoards.contains(pBoard); }
    /*   END player related   */



    /*   Start internal methods / fields   */
    private static class SchedulingBascule {
        protected int id;
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
    public void onMove (final FastBoard board) { }
    public void onOnlineChange (final FastBoard board) {}


    public void schedule (int tick, final Consumer<FastBoard> lambda) {
        if (!registered) {
            timer.scheduleAtFixedRate(() -> {
                playerBoards.forEach(x -> {
                    try {
                        lambda.accept(x.board);
                    } catch (final Exception e) { e.printStackTrace(); }
                });

            },0, 50L*tick, TimeUnit.MILLISECONDS);
        }
    }

    public void basculeIfUnchanging (int period, final Player player, final ScoreboardAbstract scb, final SchedulingBascule bascule) {
        bascule.old = bascule.value;

        bascule.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(ScoreBoardAPI.registeredPlugin, () -> {
            final Object current = bascule.value;

            if (isPresentPlayer(player)) {
                if (bascule.old != null && bascule.old.equals(current)) {
                    PlayerBoard.getPlayer(player).setCurrent(scb);
                    Bukkit.getScheduler().cancelTask(bascule.id);
                } else {
                    bascule.old = current;
                }
            } else {
                Bukkit.getScheduler().cancelTask(bascule.id);
            }

        }, period, period);
    }

    /* end of API use */










    /* start interscoarboard events private methods */

    @EventHandler
    public static void eventMove (final PlayerMoveEvent event) {
        if (!event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()))
            for (final ScoreboardAbstract instanceCandidate : allInstances.values())
                for (final PlayerBoard pBoardCandidate : instanceCandidate.playerBoards)
                    instanceCandidate.onMove(pBoardCandidate.board);
    }

    @EventHandler
    public static void eventJoin (final PlayerJoinEvent event) {
        if (ScoreBoardAPI.defaultInstance != null)
            PlayerBoard.getPlayer(event.getPlayer()).setCurrent(ScoreBoardAPI.defaultInstance);

        for (final ScoreboardAbstract instanceCandidate : allInstances.values())
            for (final PlayerBoard pBoardCandidate : instanceCandidate.playerBoards)
                instanceCandidate.onOnlineChange(pBoardCandidate.board);
    }

    @EventHandler
    public static void eventQuit (final PlayerQuitEvent event) {
        PlayerBoard.removePlayer(event.getPlayer());

        for (final ScoreboardAbstract instanceCandidate : allInstances.values())
            for (final PlayerBoard pBoardCandidate : instanceCandidate.playerBoards)
                instanceCandidate.onOnlineChange(pBoardCandidate.board);
    }

}
