package fr.i360matt.fastboardplus;


import fr.i360matt.fastboardplus.utils.FastReflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple Bukkit ScoreBoard API with 1.7 to 1.16 support.
 * Everything is at packet level so you don't need to use it in the main server thread.
 * <p>
 * You can find the project on <a href="https://github.com/MrMicky-FR/FastBoard">GitHub</a>
 *
 * new features added by 360matt:
 * - multi-View system
 * - Player Mapping
 *
 * @author MrMicky
 * @author 360matt (refactor)
 */
public class BoardPlayer {


    private static final Map<Player, BoardPlayer> players = new HashMap<>();

    public static BoardPlayer getPlayer (final Player player) {
        BoardPlayer boardPlayer = players.get(player);
        if (boardPlayer == null) {
            boardPlayer = new BoardPlayer(player);
            players.put(player, boardPlayer);
        }
        return boardPlayer;
    }

    public static BoardPlayer getPlayerOrNull (final Player player) {
        return players.get(player);
    }

    public static void deletePlayer (final Player player) {
        final BoardPlayer boardPlayer = players.get(player);
        if (boardPlayer != null) {
            boardPlayer.delete();
        }
    }

    public static Collection<BoardPlayer> getPlayerBoards () {
        return players.values();
    }

    public static void setAllToDefault () {
        for (final BoardPlayer boardPlayer : players.values()) {
            boardPlayer.setToDefault();
        }
    }

    public static void deleteAll () {
        for (final BoardPlayer boardPlayer : players.values()) {
            boardPlayer.delete();
        }
    }


    public void setCurrent (final BoardView scoreboard) {
        for (int i = 0; i <= 16; i++)
            this.removeLine(i);

        if (this.current == scoreboard) {
            scoreboard.init(this);
            return;
        }

        (this.current = scoreboard).addPlayer(this);
    }

    public void setToDefault () {
        this.setCurrent(BoardView.getDefaultInstance());
    }

    public void clear () {
        if (this.current != null) {
            this.current.removePlayer(this);
            for (int i = 0; i <= 16; i++)
                this.removeLine(i);
            this.current = null;
        }
    }














    private final Player player;
    private final String id;
    protected BoardView current;

    private final List<String> lines = new ArrayList<>();
    private String title = ChatColor.RESET.toString();

    private boolean deleted = false;

    /**
     * Creates a new FastBoard.
     *
     * @param player the owner of the scoreboard
     */
    public BoardPlayer (final Player player) {
        this.player = Objects.requireNonNull(player, "player");
        this.id = "fb-" + Integer.toHexString(ThreadLocalRandom.current().nextInt());

        try {
            sendObjectivePacket(FastReflection.ObjectiveMode.CREATE);
            sendDisplayObjectivePacket();
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to create scoreboard", t);
        }
    }

    /**
     * Get the scoreboard title.
     *
     * @return the scoreboard title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Update the scoreboard title.
     *
     * @param title the new scoreboard title
     * @throws IllegalArgumentException if the title is longer than 32 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateTitle(final String title) {
        if (this.title.equals(Objects.requireNonNull(title, "title"))) {
            return;
        }

        if (!FastReflection.VersionType.V1_13.isHigherOrEqual() && title.length() > 32) {
            throw new IllegalArgumentException("Title is longer than 32 chars");
        }

        this.title = title;

        try {
            sendObjectivePacket(FastReflection.ObjectiveMode.UPDATE);
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to update scoreboard title", t);
        }
    }

    /**
     * Get the scoreboard lines.
     *
     * @return the scoreboard lines
     */
    public List<String> getLines() {
        return new ArrayList<>(this.lines);
    }

    /**
     * Get the specified scoreboard line.
     *
     * @param line the line number
     * @return the line
     * @throws IndexOutOfBoundsException if the line is higher than {@code size}
     */
    public String getLine(final int line) {
        checkLineNumber(line, true, false);

        return this.lines.get(line);
    }

    /**
     * Update a single scoreboard line.
     *
     * @param line the line number
     * @param text the new line text
     * @throws IndexOutOfBoundsException if the line is higher than {@link #size() size() + 1}
     */
    public synchronized void updateLine(final int line, final String text) {
        checkLineNumber(line, false, true);

        try {
            if (line < size()) {
                this.lines.set(line, text);

                sendTeamPacket(getScoreByLine(line), FastReflection.TeamMode.UPDATE);
                return;
            }

            final List<String> newLines = new ArrayList<>(this.lines);

            if (line > size()) {
                for (int i = size(); i < line; i++) {
                    newLines.add("");
                }
            }

            newLines.add(text);

            updateLines(newLines);
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to update scoreboard lines", t);
        }
    }

    /**
     * Remove a scoreboard line.
     *
     * @param line the line number
     */
    public synchronized void removeLine(final int line) {
        checkLineNumber(line, false, false);

        if (line >= size()) {
            return;
        }

        final List<String> newLines = new ArrayList<>(this.lines);
        newLines.remove(line);
        updateLines(newLines);
    }

    /**
     * Update all the scoreboard lines.
     *
     * @param lines the new lines
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateLines(final String... lines) {
        updateLines(Arrays.asList(lines));
    }

    /**
     * Update the lines of the scoreboard
     *
     * @param lines the new scoreboard lines
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public synchronized void updateLines(final Collection<String> lines) {
        Objects.requireNonNull(lines, "lines");
        checkLineNumber(lines.size(), false, true);

        if (!FastReflection.VersionType.V1_13.isHigherOrEqual()) {
            int lineCount = 0;
            for (final String s : lines) {
                if (s != null && s.length() > 30) {
                    throw new IllegalArgumentException("Line " + lineCount + " is longer than 30 chars");
                }
                lineCount++;
            }
        }

        final List<String> oldLines = new ArrayList<>(this.lines);
        this.lines.clear();
        this.lines.addAll(lines);

        final int linesSize = this.lines.size();

        try {
            if (oldLines.size() != linesSize) {
                final List<String> oldLinesCopy = new ArrayList<>(oldLines);

                if (oldLines.size() > linesSize) {
                    for (int i = oldLinesCopy.size(); i > linesSize; i--) {
                        sendTeamPacket(i - 1, FastReflection.TeamMode.REMOVE);
                        sendScorePacket(i - 1, FastReflection.ScoreboardAction.REMOVE);

                        oldLines.remove(0);
                    }
                } else {
                    for (int i = oldLinesCopy.size(); i < linesSize; i++) {
                        sendScorePacket(i, FastReflection.ScoreboardAction.CHANGE);
                        sendTeamPacket(i, FastReflection.TeamMode.CREATE);

                        oldLines.add(oldLines.size() - i, getLineByScore(i));
                    }
                }
            }

            for (int i = 0; i < linesSize; i++) {
                if (!Objects.equals(getLineByScore(oldLines, i), getLineByScore(i))) {
                    sendTeamPacket(i, FastReflection.TeamMode.UPDATE);
                }
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to update scoreboard lines", t);
        }
    }

    /**
     * Get the player who has the scoreboard.
     *
     * @return current player for this FastBoard
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Get the scoreboard id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get if the scoreboard is deleted.
     *
     * @return true if the scoreboard is deleted
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * Get the scoreboard size (the number of lines).
     *
     * @return the size
     */
    public int size() {
        return this.lines.size();
    }

    /**
     * Delete this FastBoard, and will remove the scoreboard for the associated player if he is online.
     * After this, all uses of {@link #updateLines} and {@link #updateTitle} will throws an {@link IllegalStateException}
     *
     * @throws IllegalStateException if this was already call before
     */
    public void delete() {
        try {
            players.remove(player);

            if (this.current != null)
                this.current.removePlayer(this);

            for (int i = 0; i < this.lines.size(); i++) {
                sendTeamPacket(i, FastReflection.TeamMode.REMOVE);
            }

            sendObjectivePacket(FastReflection.ObjectiveMode.REMOVE);
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to delete scoreboard", t);
        }

        this.deleted = true;
    }

    /**
     * Return if the player has a prefix/suffix characters limit.
     * By default, it returns true only in 1.12 or lower.
     * This method can be overridden to fix compatibility with some versions support plugin.
     *
     * @return max length
     */
    protected boolean hasLinesMaxLength() {
        return !FastReflection.VersionType.V1_13.isHigherOrEqual();
    }

    private void checkLineNumber(final int line, final boolean checkInRange, final boolean checkMax) {
        if (line < 0) {
            throw new IllegalArgumentException("Line number must be positive");
        }

        if (checkInRange && line >= this.lines.size()) {
            throw new IllegalArgumentException("Line number must be under " + this.lines.size());
        }

        if (checkMax && line >= FastReflection.COLOR_CODES.length - 1) {
            throw new IllegalArgumentException("Line number is too high: " + this.lines.size());
        }
    }

    private int getScoreByLine(final int line) {
        return this.lines.size() - line - 1;
    }

    private String getLineByScore(final int score) {
        return getLineByScore(this.lines, score);
    }

    private String getLineByScore(final List<String> lines, final int score) {
        return lines.get(lines.size() - score - 1);
    }

    private void sendObjectivePacket(final FastReflection.ObjectiveMode mode) throws Throwable {
        final Object packet = FastReflection.PACKET_SB_OBJ.invoke();

        setField(packet, String.class, this.id);
        setField(packet, int.class, mode.ordinal());

        if (mode != FastReflection.ObjectiveMode.REMOVE) {
            setComponentField(packet, this.title, 1);

            if (FastReflection.VersionType.V1_8.isHigherOrEqual()) {
                setField(packet, FastReflection.ENUM_SB_HEALTH_DISPLAY, FastReflection.ENUM_SB_HEALTH_DISPLAY_INTEGER);
            }
        } else if (FastReflection.VERSION_TYPE == FastReflection.VersionType.V1_7) {
            setField(packet, String.class, "", 1);
        }

        sendPacket(packet);
    }

    private void sendDisplayObjectivePacket() throws Throwable {
        final Object packet = FastReflection.PACKET_SB_DISPLAY_OBJ.invoke();

        setField(packet, int.class, 1); // Position (1: sidebar)
        setField(packet, String.class, this.id); // Score Name

        sendPacket(packet);
    }

    private void sendScorePacket(final int score, final FastReflection.ScoreboardAction action) throws Throwable {
        final Object packet = FastReflection.PACKET_SB_SCORE.invoke();

        setField(packet, String.class, FastReflection.COLOR_CODES[score], 0); // Player Name

        if (FastReflection.VersionType.V1_8.isHigherOrEqual()) {
            setField(packet, FastReflection.ENUM_SB_ACTION, action == FastReflection.ScoreboardAction.REMOVE ? FastReflection.ENUM_SB_ACTION_REMOVE : FastReflection.ENUM_SB_ACTION_CHANGE);
        } else {
            setField(packet, int.class, action.ordinal(), 1); // Action
        }

        if (action == FastReflection.ScoreboardAction.CHANGE) {
            setField(packet, String.class, this.id, 1); // Objective Name
            setField(packet, int.class, score); // Score
        }

        sendPacket(packet);
    }

    private void sendTeamPacket(final int score, final FastReflection.TeamMode mode) throws Throwable {
        if (mode == FastReflection.TeamMode.ADD_PLAYERS || mode == FastReflection.TeamMode.REMOVE_PLAYERS) {
            throw new UnsupportedOperationException();
        }

        final int maxLength = hasLinesMaxLength() ? 16 : 1024;
        final Object packet = FastReflection.PACKET_SB_TEAM.invoke();

        setField(packet, String.class, this.id + ':' + score); // Team name
        setField(packet, int.class, mode.ordinal(), FastReflection.VERSION_TYPE == FastReflection.VersionType.V1_8 ? 1 : 0); // Update mode

        if (mode == FastReflection.TeamMode.CREATE || mode == FastReflection.TeamMode.UPDATE) {
            final String line = getLineByScore(score);
            String prefix;
            String suffix = null;

            if (line == null || line.isEmpty()) {
                prefix = FastReflection.COLOR_CODES[score] + ChatColor.RESET;
            } else if (line.length() <= maxLength) {
                prefix = line;
            } else {
                // Prevent splitting color codes
                final int index = line.charAt(maxLength - 1) == ChatColor.COLOR_CHAR ? (maxLength - 1) : maxLength;
                prefix = line.substring(0, index);
                final String suffixTmp = line.substring(index);
                ChatColor chatColor = null;

                if (suffixTmp.length() >= 2 && suffixTmp.charAt(0) == ChatColor.COLOR_CHAR) {
                    chatColor = ChatColor.getByChar(suffixTmp.charAt(1));
                }

                final String color = ChatColor.getLastColors(prefix);
                final boolean addColor = chatColor == null || chatColor.isFormat();

                suffix = (addColor ? (color.isEmpty() ? ChatColor.RESET.toString() : color) : "") + suffixTmp;
            }

            if (prefix.length() > maxLength || (suffix != null && suffix.length() > maxLength)) {
                // Something went wrong, just cut to prevent client crash/kick
                prefix = prefix.substring(0, maxLength);
                suffix = (suffix != null) ? suffix.substring(0, maxLength) : null;
            }

            if (FastReflection.VersionType.V1_17.isHigherOrEqual()) {
                final Object team = FastReflection.PACKET_SB_SERIALIZABLE_TEAM.invoke();
                // Since the packet is initialized with null values, we need to change more things.
                setComponentField(team, "", 0); // Display name
                setField(team, FastReflection.CHAT_FORMAT_ENUM, FastReflection.RESET_FORMATTING); // Color
                setComponentField(team, prefix, 1); // Prefix
                setComponentField(team, suffix == null ? "" : suffix, 2); // Suffix
                setField(team, String.class, "always", 0); // Visibility
                setField(team, String.class, "always", 1); // Collisions
                setField(packet, Optional.class, Optional.of(team));
            } else {
                setComponentField(packet, prefix, 2); // Prefix
                setComponentField(packet, suffix == null ? "" : suffix, 3); // Suffix
                setField(packet, String.class, "always", 4); // Visibility for 1.8+
                setField(packet, String.class, "always", 5); // Collisions for 1.9+
            }

            if (mode == FastReflection.TeamMode.CREATE) {
                setField(packet, Collection.class, Collections.singletonList(FastReflection.COLOR_CODES[score])); // Players in the team
            }
        }

        sendPacket(packet);
    }

    private void sendPacket(final Object packet) throws Throwable {
        if (this.deleted) {
            throw new IllegalStateException("This FastBoard is deleted");
        }

        if (this.player.isOnline()) {
            final Object entityPlayer = FastReflection.PLAYER_GET_HANDLE.invoke(this.player);
            final Object playerConnection = FastReflection.PLAYER_CONNECTION.invoke(entityPlayer);
            FastReflection.SEND_PACKET.invoke(playerConnection, packet);
        }
    }

    private void setField(final Object object, final Class<?> fieldType, final Object value) throws ReflectiveOperationException {
        setField(object, fieldType, value, 0);
    }

    private void setField(final Object packet, final Class<?> fieldType, final Object value, final int count) throws ReflectiveOperationException {
        int i = 0;
        for (final Field field : FastReflection.PACKETS.get(packet.getClass())) {
            if (field.getType() == fieldType && count == i++) {
                field.set(packet, value);
            }
        }
    }

    private void setComponentField(final Object packet, final String value, final int count) throws Throwable {
        if (!FastReflection.VersionType.V1_13.isHigherOrEqual()) {
            setField(packet, String.class, value, count);
            return;
        }

        int i = 0;
        for (final Field field : FastReflection.PACKETS.get(packet.getClass())) {
            if ((field.getType() == String.class || field.getType() == FastReflection.CHAT_COMPONENT_CLASS) && count == i++) {
                field.set(packet, value.isEmpty() ? FastReflection.EMPTY_MESSAGE : Array.get(FastReflection.MESSAGE_FROM_STRING.invoke(value), 0));
            }
        }
    }


}