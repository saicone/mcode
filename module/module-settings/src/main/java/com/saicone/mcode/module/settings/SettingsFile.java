package com.saicone.mcode.module.settings;

import com.saicone.mcode.module.settings.update.SettingsUpdater;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class SettingsFile extends Settings {

    private static final Set<String> FILE_TYPES = Set.of("xml", "json", "yml", "yaml", "toml", "conf");

    // Parameters
    private final SourceType source;
    private final String path;
    private final String name;
    private String type;

    // Mutable parameters
    private SettingsFile optional;
    private SettingsParser parser;

    // Provided parameters
    private boolean transfer;
    private File parentFolder;
    private ClassLoader parentClassLoader;
    private SettingsUpdater updater;

    public SettingsFile(@NotNull File file) {
        this("file", file.getName());
        setParent(file.getParentFile());
    }

    public SettingsFile(@NotNull String pathName) {
        this(pathName, pathName);
    }

    public SettingsFile(@NotNull String source, @NotNull String pathName) {
        this.source = SourceType.of(source);
        final String name;
        int index = pathName.lastIndexOf('?');
        int pathIndex = pathName.lastIndexOf('/');
        int nameIndex = pathName.lastIndexOf('.');
        if (pathIndex > 0) {
            if (index > pathIndex && index > nameIndex) {
                this.path = pathName.substring(0, index + 1);
                this.name = pathName.substring(pathIndex + 1, index + 1);
                this.type = pathName.substring(index + 1);
                return;
            } else {
                this.path = pathName;
            }
            name = pathName.substring(pathIndex + 1);
        } else {
            if (index > nameIndex) {
                this.path = pathName.substring(0, index + 1);
                this.name = pathName.substring(pathIndex + 1, index + 1);
                this.type = pathName.substring(index + 1);
                return;
            } else {
                this.path = pathName;
            }
            name = pathName;
        }
        nameIndex = name.lastIndexOf('.') + 1;
        this.name = name.substring(0, nameIndex);
        this.type = name.substring(nameIndex);
    }

    public SettingsFile(@NotNull String source, @NotNull String path, @NotNull String name, @NotNull String type) {
        this.source = SourceType.of(source);
        this.path = path;
        this.name = name;
        this.type = type;
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsFile or(@NotNull String pathName) {
        this.optional = new SettingsFile(pathName);
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public SettingsFile or(@NotNull String from, @NotNull String pathName) {
        this.optional = new SettingsFile(from, pathName);
        return this;
    }

    @NotNull
    @Contract("_, _, _, _ -> this")
    public SettingsFile or(@NotNull String from, @NotNull String path, @NotNull String name, @NotNull String type) {
        this.optional = new SettingsFile(from, path, name, type);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsFile setTransfer(boolean transfer) {
        this.transfer = transfer;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsFile setParent(@Nullable File folder) {
        if (source == SourceType.FILE) {
            this.parentFolder = folder;
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsFile setParent(@Nullable ClassLoader classLoader) {
        if (source == SourceType.INPUT_STREAM) {
            this.parentClassLoader = classLoader;
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsFile setUpdater(SettingsUpdater updater) {
        this.updater = updater;
        return this;
    }

    @NotNull
    @Contract("-> this")
    public SettingsFile setSimpleUpdater() {
        this.updater = new SettingsUpdater.Simple();
        return this;
    }

    @NotNull
    public SourceType getSource() {
        return source;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @Nullable
    public SettingsFile getOptional() {
        return optional;
    }

    @NotNull
    public SettingsParser getParser() {
        if (parser == null) {
            parser = getParser(type);
        }
        return parser;
    }

    @NotNull
    public SettingsParser getParser(@NotNull String type) {
        final SettingsParser parser = SettingsParser.of(type);
        if (parser == null) {
            throw new RuntimeException("Cannot find SettingsParser for '" + type + "' file type");
        }
        return parser;
    }

    @Nullable
    public Reader getReader() throws IOException {
        switch (source) {
            case FILE:
                final File file = getFile();
                return file.exists() ? new BufferedReader(new FileReader(file)) : null;
            case URL:
                return new InputStreamReader(new BufferedInputStream(new URL(path).openConnection().getInputStream()));
            case INPUT_STREAM:
                final InputStream in = getResource();
                return in == null ? null : new InputStreamReader(new BufferedInputStream(in));
            default:
                return null;
        }
    }

    @Nullable
    public Writer getWriter() throws IOException {
        if (source == SourceType.FILE) {
            final File file = getFile();
            if (!file.exists()) {
                file.createNewFile();
            }
            return new BufferedWriter(new FileWriter(file));
        }
        return null;
    }

    @NotNull
    public File getFile() {
        File file = parentFolder;
        for (String s : path.split("/")) {
            file = new File(file, s);
        }
        if (file == null) {
            file = new File(path);
        }
        // Path marked with recursive search
        if (!file.exists() && path.endsWith(".*")) {
            file = file.getParentFile();
            // Check if the file type was specified or cached after search
            if (!type.equals("*")) {
                final File typeFile = new File(file, name + '.' + type);
                if (typeFile.exists()) {
                    return typeFile;
                }
            }
            for (String type : FILE_TYPES) {
                final File other = new File(file, name + '.' + type);
                if (other.exists()) {
                    file = other;
                    // Cache file type
                    this.type = type;
                    if (this.parser != null) {
                        // Update SettingsParser to the new file type
                        this.parser = SettingsParser.of(type);
                    }
                    break;
                }
            }
        }
        return file;
    }

    @NotNull
    public File getFileOrType(@NotNull String type) {
        final File file = getFile();
        if (file.getName().endsWith(".*")) {
            this.type = type;
            return new File(file.getParentFile(), name + '.' + type);
        }
        return file;
    }

    @Nullable
    public InputStream getResource() {
        return (parentClassLoader != null ? parentClassLoader : SettingsFile.class.getClassLoader()).getResourceAsStream(path);
    }

    public boolean load() {
        return load(transfer);
    }

    public boolean load(boolean transfer) {
        try {
            Reader reader = getReader();
            boolean optionalUpdate = optional != null;
            if (reader == null && optionalUpdate) {
                if (transfer) {
                    optional.transfer(this);
                    reader = getReader();
                } else {
                    reader = optional.getReader();
                }
                optionalUpdate = false;
            }

            if (reader == null) {
                return false;
            }

            boolean result = getParser().load(reader, this);
            reader.close();
            if (result && updater != null) {
                if (optionalUpdate) {
                    updater.onOptional(this, optional);
                }
                updater.onUpdate(this);
            }
            updateSuppliers();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save() {
        try (Writer writer = getWriter()) {
            if (writer == null) {
                return false;
            }
            return parser.save(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void transfer(@NotNull SettingsFile settingsFile) throws IOException {
        if (settingsFile.source == SourceType.FILE && settingsFile.type.equalsIgnoreCase(type)) {
            final File toFile = settingsFile.getFileOrType(type);
            if (!toFile.getParentFile().exists()) {
                toFile.getParentFile().mkdirs();
            }
            switch (source) {
                case FILE:
                    final File fromFile = getFile();
                    if (fromFile.exists()) {
                        Files.copy(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    break;
                case URL:
                    try (InputStream in = new URL(path).openStream()) {
                        Files.copy(in, toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    break;
                case INPUT_STREAM:
                    final InputStream in = getResource();
                    if (in != null) {
                        Files.copy(in, toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    break;
                default:
                    break;
            }
            return;
        }
        try (Reader reader = getReader(); Writer writer = settingsFile.getWriter()) {
            if (reader == null || writer == null) {
                return;
            }
            final Settings settings = getParser().read(reader);
            settingsFile.getParser().write(settings, writer);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public enum SourceType {

        FILE, URL, INPUT_STREAM;

        @NotNull
        public static SourceType of(@NotNull String s) {
            final String[] split = s.split(":", 1);
            final String type;
            if (split.length > 0) {
                type = split[0];
            } else {
                type = s;
            }
            switch (type.replace(' ', '_').toLowerCase()) {
                case "url":
                case "http":
                case "https":
                    return SourceType.URL;
                case "input":
                case "stream":
                case "inputstream":
                case "input_stream":
                    return SourceType.INPUT_STREAM;
                case "file":
                default:
                    return SourceType.FILE;
            }
        }
    }
}
