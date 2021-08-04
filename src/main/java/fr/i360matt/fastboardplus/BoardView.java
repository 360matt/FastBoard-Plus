package fr.i360matt.fastboardplus;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class BoardView {
    protected static final Map<String, BoardView> boards = new HashMap<>();

    protected static BoardView defaultInstance;

    private final Map<Integer, Set<Consumer<BoardPlayer>>> consumers = new HashMap<>();
    private boolean isEnabled;

    /**
     *
     * @return l'instance qui a été register par défaut
     */
    public static BoardView getDefaultInstance () {
        return defaultInstance;
    }


    /**
     * Permet d'update le scoreboard pour tous les joueurs
     *
     * @param instance le nom du scoreboard qui permettra de retrouver l'instance
     * @param board le board des joueurs qui sera loop
     */
    public static void updateAllPlayers (final String instance, final Consumer<BoardPlayer> board) {
        for (final BoardPlayer boardPlayer : BoardView.getInstance(instance).boardPlayers) {
            board.accept(boardPlayer);
        }
    }

    public static void deleteAll () {
        boards.values().forEach(BoardView::unregister);
    }

    /**
     *
     * @param name le nom de l'instance qui nous interresse
     * @return l'instance en question
     */
    public static BoardView getInstance (final String name) {
        return boards.get(name);
    }


    private String name;

    public BoardView (final String name) {
        this(name, false);
    }

    public BoardView (final String name, final boolean isDefault) {
        if (ScoreBoardAPI.timer == null) {
            throw new RuntimeException("FastBoard-Plus was not registered !!");
        }

        registerYourSchedulersHere();
        boards.put(name, this);
        this.isEnabled = true;
        if (isDefault) {
            defaultInstance = this;
        }
    }

    public final void unregister () {
        if (defaultInstance.equals(this))
            defaultInstance = null;
        this.consumers.clear();
        removeAllPlayers();
        boards.remove(name);
        this.isEnabled = false;
    }

    /* end registration */



    /* Start player related */
    private final Set<BoardPlayer> boardPlayers = ConcurrentHashMap.newKeySet();
    public final Set<BoardPlayer> getAllPlayerBoards () {
        return boardPlayers;
    }
    public void addPlayer (final BoardPlayer pBoard) {
        this.boardPlayers.add(pBoard);
        init(pBoard);
    }
    public void addPlayer (final Player player) {
        addPlayer(BoardPlayer.getPlayer(player));
    }
    public void removeAllPlayers () {
        this.boardPlayers.forEach(BoardPlayer::delete);
    }
    public void removePlayer (final BoardPlayer pBoard) {
        this.boardPlayers.remove(pBoard);
    }
    protected boolean isPresentPlayer (final Player player) {
        return this.boardPlayers.contains(BoardPlayer.getPlayerOrNull(player));
    }
    protected boolean isPresentPlayer (final BoardPlayer pBoard) {
        return this.boardPlayers.contains(pBoard);
    }
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

    public abstract void init (final BoardPlayer board);
    public abstract void registerYourSchedulersHere ();

    /* predef events */
    public void onMove (final BoardPlayer board) {}
    public void onOnlineChange (final BoardPlayer board) {}


    public void schedule (final int tick, final Consumer<BoardPlayer> lambda) {
        Set<Consumer<BoardPlayer>> eventsForTick = this.consumers.get(tick);
        if (eventsForTick == null) {
            eventsForTick = new HashSet<>();
            this.consumers.put(tick, eventsForTick);
            
            final AtomicReference<ScheduledFuture<?>> atomic = new AtomicReference<>();

            atomic.set(ScoreBoardAPI.timer.scheduleAtFixedRate(() -> {
                this.boardPlayers.forEach(playerRelated -> {
                    this.consumers.get(tick).forEach(consum -> {
                        consum.accept(playerRelated);
                    });
                });

                final ScheduledFuture<?> scheduledFuture = atomic.get();
                if (scheduledFuture != null && !this.isEnabled) {
                    // this board was unregistered
                    scheduledFuture.cancel(true);
                }
            }, 0, 50L * tick, TimeUnit.MILLISECONDS));
        }

        eventsForTick.add(lambda);
    }

    public void basculeIfUnchanged (final int period, final Player player, final BoardView scb, final SchedulingBascule bascule) {
        final AtomicReference<ScheduledFuture<?>> atomic = new AtomicReference<>();
        atomic.set(ScoreBoardAPI.timer.scheduleAtFixedRate(() -> {
            if (this.isPresentPlayer(player)) {
                if (Objects.equals(bascule.old, bascule.value)) {
                    bascule.old = bascule.value;
                    return;
                }
                BoardPlayer.getPlayer(player).setCurrent(scb);
            }
            atomic.get().cancel(true);
        }, 50L*period, 50L*period, TimeUnit.MILLISECONDS));
    }

    public void basculeIfchanged (final int period, final Player player, final BoardView scb, final SchedulingBascule bascule) {
        final AtomicReference<ScheduledFuture<?>> atomic = new AtomicReference<>();
        atomic.set(ScoreBoardAPI.timer.scheduleAtFixedRate(() -> {
            if (this.isPresentPlayer(player)) {
                if (!Objects.equals(bascule.old, bascule.value)) {
                    bascule.old = bascule.value;
                    return;
                }
                BoardPlayer.getPlayer(player).setCurrent(scb);
            }
            atomic.get().cancel(true);
        }, 50L*period, 50L*period, TimeUnit.MILLISECONDS));
    }

    /* end of API use */


}
