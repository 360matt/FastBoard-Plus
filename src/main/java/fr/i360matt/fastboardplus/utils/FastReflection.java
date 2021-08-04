package fr.i360matt.fastboardplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * API de MrMicky, reformat par 360matt pour WizMC
 *
 * @author MrMicky
 * @author 360matt
 */
public final class FastReflection {

    private static final String NM_PACKAGE = "net.minecraft";
    public static final String OBC_PACKAGE = "org.bukkit.craftbukkit";
    public static final String NMS_PACKAGE = NM_PACKAGE + ".server";

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(OBC_PACKAGE.length() + 1);

    private static final MethodType VOID_METHOD_TYPE = MethodType.methodType(void.class);
    private static final boolean NMS_REPACKAGED = optionalClass(NM_PACKAGE + ".network.protocol.Packet").isPresent();

    private static volatile Object theUnsafe;

    private FastReflection() {
        throw new UnsupportedOperationException();
    }

    public static boolean isRepackaged() {
        return NMS_REPACKAGED;
    }

    public static String nmsClassName(final String post1_17package, final String className) {
        if (NMS_REPACKAGED) {
            final String classPackage = post1_17package == null ? NM_PACKAGE : NM_PACKAGE + '.' + post1_17package;
            return classPackage + '.' + className;
        }
        return NMS_PACKAGE + '.' + VERSION + '.' + className;
    }

    public static Class<?> nmsClass(final String post1_17package, final String className) throws ClassNotFoundException {
        return Class.forName(nmsClassName(post1_17package, className));
    }

    public static Optional<Class<?>> nmsOptionalClass(final String post1_17package, final String className) {
        return optionalClass(nmsClassName(post1_17package, className));
    }

    public static String obcClassName(final String className) {
        return OBC_PACKAGE + '.' + VERSION + '.' + className;
    }

    public static Class<?> obcClass(final String className) throws ClassNotFoundException {
        return Class.forName(obcClassName(className));
    }

    public static Optional<Class<?>> optionalClass(final String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public static Object enumValueOf(final Class<?> enumClass, final String enumName) {
        return Enum.valueOf(enumClass.asSubclass(Enum.class), enumName);
    }

    public static Object enumValueOf(final Class<?> enumClass, final String enumName, final int fallbackOrdinal) {
        try {
            return enumValueOf(enumClass, enumName);
        } catch (final IllegalArgumentException e) {
            final Object[] constants = enumClass.getEnumConstants();
            if (constants.length > fallbackOrdinal) {
                return constants[fallbackOrdinal];
            }
            throw e;
        }
    }

    static Class<?> innerClass(final Class<?> parentClass, final Predicate<Class<?>> classPredicate) throws ClassNotFoundException {
        for (final Class<?> innerClass : parentClass.getDeclaredClasses()) {
            if (classPredicate.test(innerClass)) {
                return innerClass;
            }
        }
        throw new ClassNotFoundException("No class in " + parentClass.getCanonicalName() + " matches the predicate.");
    }

    public static PacketConstructor findPacketConstructor(final Class<?> packetClass, final MethodHandles.Lookup lookup) throws Exception {
        try {
            final MethodHandle constructor = lookup.findConstructor(packetClass, VOID_METHOD_TYPE);
            return constructor::invoke;
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            // try below with Unsafe
        }

        if (theUnsafe == null) {
            synchronized (FastReflection.class) {
                if (theUnsafe == null) {
                    final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                    final Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                    theUnsafeField.setAccessible(true);
                    theUnsafe = theUnsafeField.get(null);
                }
            }
        }

        final Method allocateMethod = theUnsafe.getClass().getMethod("allocateInstance", Class.class);
        return () -> allocateMethod.invoke(theUnsafe, packetClass);
    }

    @FunctionalInterface
    public interface PacketConstructor {
        Object invoke() throws Throwable;
    }


    public static final Map<Class<?>, Field[]> PACKETS = new HashMap<>(8);
    public static final String[] COLOR_CODES = Arrays.stream(ChatColor.values())
            .map(Object::toString)
            .toArray(String[]::new);
    public static final VersionType VERSION_TYPE;
    // Packets and components
    public static final Class<?> CHAT_COMPONENT_CLASS;
    public static final Class<?> CHAT_FORMAT_ENUM;
    public static final Object EMPTY_MESSAGE;
    public static final Object RESET_FORMATTING;
    public static final MethodHandle MESSAGE_FROM_STRING;
    public static final MethodHandle PLAYER_CONNECTION;
    public static final MethodHandle SEND_PACKET;
    public static final MethodHandle PLAYER_GET_HANDLE;
    // Scoreboard packets
    public static final PacketConstructor PACKET_SB_OBJ;
    public static final PacketConstructor PACKET_SB_DISPLAY_OBJ;
    public static final PacketConstructor PACKET_SB_SCORE;
    public static final PacketConstructor PACKET_SB_TEAM;
    public static final PacketConstructor PACKET_SB_SERIALIZABLE_TEAM;
    // Scoreboard enums
    public static final Class<?> ENUM_SB_HEALTH_DISPLAY;
    public static final Class<?> ENUM_SB_ACTION;
    public static final Object ENUM_SB_HEALTH_DISPLAY_INTEGER;
    public static final Object ENUM_SB_ACTION_CHANGE;
    public static final Object ENUM_SB_ACTION_REMOVE;

    static {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            if (FastReflection.isRepackaged()) {
                VERSION_TYPE = VersionType.V1_17;
            } else if (FastReflection.nmsOptionalClass(null, "ScoreboardServer$Action").isPresent()) {
                VERSION_TYPE = VersionType.V1_13;
            } else if (FastReflection.nmsOptionalClass(null, "IScoreboardCriteria$EnumScoreboardHealthDisplay").isPresent()) {
                VERSION_TYPE = VersionType.V1_8;
            } else {
                VERSION_TYPE = VersionType.V1_7;
            }

            final String gameProtocolPackage = "network.protocol.game";
            final Class<?> craftPlayerClass = FastReflection.obcClass("entity.CraftPlayer");
            final Class<?> craftChatMessageClass = FastReflection.obcClass("util.CraftChatMessage");
            final Class<?> entityPlayerClass = FastReflection.nmsClass("server.level", "EntityPlayer");
            final Class<?> playerConnectionClass = FastReflection.nmsClass("server.network", "PlayerConnection");
            final Class<?> packetClass = FastReflection.nmsClass("network.protocol", "Packet");
            final Class<?> packetSbObjClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardObjective");
            final Class<?> packetSbDisplayObjClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardDisplayObjective");
            final Class<?> packetSbScoreClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardScore");
            final Class<?> packetSbTeamClass = FastReflection.nmsClass(gameProtocolPackage, "PacketPlayOutScoreboardTeam");
            final Class<?> sbTeamClass = VersionType.V1_17.isHigherOrEqual()
                    ? FastReflection.innerClass(packetSbTeamClass, innerClass -> !innerClass.isEnum()) : null;
            final Field playerConnectionField = Arrays.stream(entityPlayerClass.getFields())
                    .filter(field -> field.getType().isAssignableFrom(playerConnectionClass))
                    .findFirst().orElseThrow(NoSuchFieldException::new);

            MESSAGE_FROM_STRING = lookup.unreflect(craftChatMessageClass.getMethod("fromString", String.class));
            CHAT_COMPONENT_CLASS = FastReflection.nmsClass("network.chat", "IChatBaseComponent");
            CHAT_FORMAT_ENUM = FastReflection.nmsClass(null, "EnumChatFormat");
            EMPTY_MESSAGE = Array.get(MESSAGE_FROM_STRING.invoke(""), 0);
            RESET_FORMATTING = FastReflection.enumValueOf(CHAT_FORMAT_ENUM, "RESET", 21);
            PLAYER_GET_HANDLE = lookup.findVirtual(craftPlayerClass, "getHandle", MethodType.methodType(entityPlayerClass));
            PLAYER_CONNECTION = lookup.unreflectGetter(playerConnectionField);
            SEND_PACKET = lookup.findVirtual(playerConnectionClass, "sendPacket", MethodType.methodType(void.class, packetClass));
            PACKET_SB_OBJ = FastReflection.findPacketConstructor(packetSbObjClass, lookup);
            PACKET_SB_DISPLAY_OBJ = FastReflection.findPacketConstructor(packetSbDisplayObjClass, lookup);
            PACKET_SB_SCORE = FastReflection.findPacketConstructor(packetSbScoreClass, lookup);
            PACKET_SB_TEAM = FastReflection.findPacketConstructor(packetSbTeamClass, lookup);
            PACKET_SB_SERIALIZABLE_TEAM = sbTeamClass == null ? null : FastReflection.findPacketConstructor(sbTeamClass, lookup);

            for (final Class<?> clazz : Arrays.asList(packetSbObjClass, packetSbDisplayObjClass, packetSbScoreClass, packetSbTeamClass, sbTeamClass)) {
                if (clazz == null) {
                    continue;
                }
                final Field[] fields = Arrays.stream(clazz.getDeclaredFields())
                        .filter(field -> !Modifier.isStatic(field.getModifiers()))
                        .toArray(Field[]::new);
                for (final Field field : fields) {
                    field.setAccessible(true);
                }
                PACKETS.put(clazz, fields);
            }

            if (VersionType.V1_8.isHigherOrEqual()) {
                final String enumSbActionClass = VersionType.V1_13.isHigherOrEqual()
                        ? "ScoreboardServer$Action"
                        : "PacketPlayOutScoreboardScore$EnumScoreboardAction";
                ENUM_SB_HEALTH_DISPLAY = FastReflection.nmsClass("world.scores.criteria", "IScoreboardCriteria$EnumScoreboardHealthDisplay");
                ENUM_SB_ACTION = FastReflection.nmsClass("server", enumSbActionClass);
                ENUM_SB_HEALTH_DISPLAY_INTEGER = FastReflection.enumValueOf(ENUM_SB_HEALTH_DISPLAY, "INTEGER", 0);
                ENUM_SB_ACTION_CHANGE = FastReflection.enumValueOf(ENUM_SB_ACTION, "CHANGE", 0);
                ENUM_SB_ACTION_REMOVE = FastReflection.enumValueOf(ENUM_SB_ACTION, "REMOVE", 1);
            } else {
                ENUM_SB_HEALTH_DISPLAY = null;
                ENUM_SB_ACTION = null;
                ENUM_SB_HEALTH_DISPLAY_INTEGER = null;
                ENUM_SB_ACTION_CHANGE = null;
                ENUM_SB_ACTION_REMOVE = null;
            }
        } catch (final Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public enum ObjectiveMode {
        CREATE, REMOVE, UPDATE
    }

    public enum TeamMode {
        CREATE, REMOVE, UPDATE, ADD_PLAYERS, REMOVE_PLAYERS
    }

    public enum ScoreboardAction {
        CHANGE, REMOVE
    }

    public enum VersionType {
        V1_7, V1_8, V1_13, V1_17;

        public boolean isHigherOrEqual() {
            return VERSION_TYPE.ordinal() >= ordinal();
        }
    }


}