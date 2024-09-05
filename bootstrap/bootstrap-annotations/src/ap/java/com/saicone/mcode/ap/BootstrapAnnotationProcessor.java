package com.saicone.mcode.ap;

import com.saicone.mcode.ap.serializer.BukkitPluginSerializer;
import com.saicone.mcode.ap.serializer.BungeecordPluginSerializer;
import com.saicone.mcode.ap.serializer.PaperPluginSerializer;
import com.saicone.mcode.ap.serializer.VelocityPluginSerializer;
import com.saicone.mcode.bootstrap.PluginDependencies;
import com.saicone.mcode.bootstrap.PluginDependency;
import com.saicone.mcode.bootstrap.PluginDescription;
import com.saicone.mcode.platform.PlatformType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({"com.saicone.mcode.bootstrap.PluginDescription", "com.saicone.mcode.bootstrap.PluginDependencies", "com.saicone.mcode.bootstrap.PluginDependency"})
public class BootstrapAnnotationProcessor extends AbstractProcessor {

    private static final Map<String, PlatformType> PLATFORM_CLASS = Map.of(
            "org.bukkit.plugin.java.JavaPlugin", PlatformType.BUKKIT,
            "net.md_5.bungee.api.plugin.Plugin", PlatformType.BUNGEECORD
    );

    private ProcessingEnvironment environment;
    private Map<PlatformType, PluginSerializer> serializers;
    private Set<PlatformType> generatedFiles;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.environment = processingEnv;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public Map<PlatformType, PluginSerializer> getSerializers() {
        if (serializers == null) {
            serializers = new HashMap<>();
        }
        return serializers;
    }

    public Set<PlatformType> getGeneratedFiles() {
        if (generatedFiles == null) {
            generatedFiles = new HashSet<>();
        }
        return generatedFiles;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element e : roundEnv.getElementsAnnotatedWith(PluginDescription.class)) {
            final TypeElement element = (TypeElement) e;
            final PluginDescription plugin = element.getAnnotation(PluginDescription.class);

            // Create serializer
            serialize(plugin, element);
            
            // Save into files
            for (Map.Entry<PlatformType, PluginSerializer> entry : getSerializers().entrySet()) {

                // Avoid double file generation (rare cases using lombok)
                if (getGeneratedFiles().contains(entry.getKey())) {
                    continue;
                }
                getGeneratedFiles().add(entry.getKey());

                try {
                    final FileObject fileObject = environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", entry.getKey().getFileName());
                    try (BufferedWriter writer = new BufferedWriter(fileObject.openWriter())) {
                        entry.getValue().write(writer);
                    }
                } catch (IOException ex) {
                    environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to generate plugin file at " + entry.getKey() + "\nReason:" + ex.getMessage());
                }
            }
        }
        return false;
    }

    private void serialize(PluginDescription plugin, TypeElement element) {
        PlatformType type = null;
        final String mainClass;
        if (plugin.main().isBlank()) {
            String name = element.getQualifiedName().toString();

            TypeElement superElement = element;
            while (superElement != null) {
                final String qualifiedName = superElement.getQualifiedName().toString();
                if (qualifiedName.equals("com.saicone.mcode.Plugin")) {
                    name = "!" + name;
                    break;
                } else if (PLATFORM_CLASS.containsKey(qualifiedName)) {
                    type = PLATFORM_CLASS.get(qualifiedName);
                    break;
                }
                superElement = (TypeElement) environment.getTypeUtils().asElement(superElement.getSuperclass());
            }
            mainClass = name;
        } else {
            mainClass = plugin.main();
        }

        final Set<PlatformType> platforms = new HashSet<>(Set.of(plugin.platform()));

        if (platforms.isEmpty()) {
            if (mainClass.isBlank()) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot generate boostrap without provided platform");
            } else {
                serialize(plugin, type != null ? type : PlatformType.VELOCITY, mainClass);
            }
        } else {
            if (type != null && !platforms.contains(type)) {
                final boolean compatible;
                switch (type) {
                    case BUKKIT:
                    case SPIGOT:
                    case PAPER:
                        compatible = platforms.contains(PlatformType.PAPER) || platforms.contains(PlatformType.SPIGOT) || platforms.contains(PlatformType.BUKKIT);
                        break;
                    case BUNGEECORD:
                    case VELOCITY:
                    default:
                        compatible = false;
                        break;
                }
                if (!compatible) {
                    environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot generate plugin information for " + platforms.stream().map(PlatformType::name).collect(Collectors.joining(", ")) + " platform" + (platforms.size() == 1 ? "" : "s") + ", due " + type + " plugin was detected");
                    return;
                } else {
                    platforms.add(type);
                }
            }
            for (PlatformType platform : platforms) {
                serialize(plugin, platform, mainClass);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void serialize(PluginDescription plugin, PlatformType type, String mainClass) {
        if (generatedFiles.contains(type == PlatformType.SPIGOT ? PlatformType.BUKKIT : type)) {
            return;
        }
        switch (type) {
            case PAPER:
                serializers.put(PlatformType.PAPER, new PaperPluginSerializer(plugin, mainClass, serializeDependencies(plugin, PlatformType.BUKKIT)));
            case BUKKIT:
            case SPIGOT:
                serializers.put(PlatformType.BUKKIT, new BukkitPluginSerializer(plugin, mainClass, serializeDependencies(plugin, PlatformType.BUKKIT)));
                break;
            case BUNGEECORD:
                serializers.put(PlatformType.BUNGEECORD, new BungeecordPluginSerializer(plugin, mainClass, serializeDependencies(plugin, PlatformType.BUNGEECORD)));
                break;
            case VELOCITY:
                serializers.put(PlatformType.VELOCITY, new VelocityPluginSerializer(plugin, mainClass, serializeDependencies(plugin, PlatformType.VELOCITY)));
                break;
            default:
                break;
        }
    }

    private Map<String, Set<SerializedDependency>> serializeDependencies(PluginDescription plugin, PlatformType platform) {
        final Map<String, Set<SerializedDependency>> dependencies = new HashMap<>();
        for (PluginDependencies list : plugin.dependencies()) {
            if (list.platform().length > 0) {
                boolean allow = false;
                for (PlatformType type : list.platform()) {
                    if (type.isChild(platform)) {
                        allow = true;
                        break;
                    }
                }
                if (!allow) {
                    continue;
                }
            }
            final Set<SerializedDependency> set = dependencies.computeIfAbsent(list.section().isBlank() ? "server" : list.section().toLowerCase(), s -> new HashSet<>());
            for (PluginDependency dependency : list.value()) {
                set.add(SerializedDependency.of(dependency));
            }
            dependencies.put(list.section(), set);
        }
        for (String name : plugin.depend()) {
            dependencies.computeIfAbsent("server", s -> new HashSet<>()).add(SerializedDependency.required(name));
        }
        for (String name : plugin.softDepend()) {
            dependencies.computeIfAbsent("server", s -> new HashSet<>()).add(SerializedDependency.optional(name));
        }
        for (String name : plugin.loadBefore()) {
            dependencies.computeIfAbsent("server", s -> new HashSet<>()).add(SerializedDependency.optional(name, "AFTER"));
        }
        return dependencies;
    }
}
