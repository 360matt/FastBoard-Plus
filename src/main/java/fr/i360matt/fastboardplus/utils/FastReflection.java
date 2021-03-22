package fr.i360matt.fastboardplus.utils;

import org.bukkit.Bukkit;

import java.util.Locale;
import java.util.Optional;

/**
 * API de MrMicky, reformat par 360matt pour WizMC
 *
 * @author MrMicky
 * @author 360matt
 */
public final class FastReflection {
    public static final String OBC_PACKAGE = "org.bukkit.craftbukkit";
    public static final String NMS_PACKAGE = "net.minecraft.server";

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(OBC_PACKAGE.length() + 1);

    private FastReflection() {
        throw new UnsupportedOperationException();
    }

    public static String nmsClassName(final String className) {
        return NMS_PACKAGE + '.' + VERSION + '.' + className;
    }

    public static Class<?> nmsClass(final String className) throws ClassNotFoundException {
        return Class.forName(nmsClassName(className));
    }

    public static Optional<Class<?>> nmsOptionalClass(final String className) {
        return optionalClass(nmsClassName(className));
    }

    public static String obcClassName(final String className) {
        return OBC_PACKAGE + '.' + VERSION + '.' + className;
    }

    public static Class<?> obcClass(final String className) throws ClassNotFoundException {
        return Class.forName(obcClassName(className));
    }

    public static Optional<Class<?>> obcOptionalClass(final String className) {
        return optionalClass(obcClassName(className));
    }

    public static Optional<Class<?>> optionalClass(final String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E enumValueOf(final Class<?> enumClass, final String enumName) {
        return Enum.valueOf((Class<E>) enumClass, enumName.toUpperCase(Locale.ROOT));
    }
}