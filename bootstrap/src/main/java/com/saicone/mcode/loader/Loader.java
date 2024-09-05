package com.saicone.mcode.loader;

import com.saicone.ezlib.Ezlib;
import com.saicone.ezlib.EzlibLoader;
import com.saicone.mcode.bootstrap.Addon;
import com.saicone.mcode.bootstrap.Bootstrap;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

public interface Loader {

    String JAR_NAME = loadJarName();
    EzlibLoader LIBRARY_LOADER = loadLibraryLoader();

    private static String loadJarName() {
        try {
            return Paths.get(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getFileName()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static EzlibLoader loadLibraryLoader() {
        final Ezlib ezlib = new Ezlib();
        ezlib.init();

        final boolean versionMatches = "${gson_version}".equals(new String(new char[] {'$','{','g','s','o','n','_','v','e','r','s','i','o','n','}'}));
        final String pkg = new String(new char[] {'c','o','m','.','g','o','o','g','l','e','.','g','s','o','n'});
        final boolean packageMatches = "com.google.gson".equals(pkg);
        if (!versionMatches || !packageMatches) {
            final var gson = ezlib.dependency("com.google.code.gson:gson:" + (versionMatches ? "2.10.1" : "${gson_version}"));
            if (!packageMatches) {
                gson.relocations(Map.of(pkg, "com.google.gson"));
            }
            gson.parent(true);
            gson.load();
        }

        final EzlibLoader libraryLoader = new EzlibLoader(Bootstrap.class.getClassLoader(), null, ezlib).logger((level, msg) -> {
            if (level <= 3) {
                final String prefix = level == 1 ? "\u001B[31m" : level == 2 ? "\u001B[33m" : "";
                System.out.println(prefix + "[" + JAR_NAME + "] " + msg);
            }
        }).xmlParser(new EzlibLoader.XmlParser());
        libraryLoader.loadRelocations(Addon.RELOCATIONS);
        libraryLoader.applyDependency(Addon.COMMON.dependency());
        return libraryLoader;
    }

    @NotNull
    default EzlibLoader getLibraryLoader() {
        return LIBRARY_LOADER;
    }
}
