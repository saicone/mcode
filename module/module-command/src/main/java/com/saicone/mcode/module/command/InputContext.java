package com.saicone.mcode.module.command;

import com.saicone.mcode.util.Dual;
import com.saicone.types.TypeParser;
import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InputContext<SenderT> {

    static final CommandSuggestion<?> EMPTY_SUGGESTION = context -> null;

    // Sender instances
    private final SenderT user;
    private final SenderT agent;
    private final CommandThrowable<SenderT> throwable;

    // Past arguments
    private final List<Dual<String, Object>> inputs = new ArrayList<>();
    // Current arguments
    private final Map<String, Dual<String, Object>> arguments = new LinkedHashMap<>();

    // Commands path, not using a LinkedHashMap due some arguments can have same name
    private final List<String> path = new ArrayList<>();
    private final List<CommandNode<SenderT>> commands = new ArrayList<>();

    private CommandResult result = CommandResult.DONE;

    public InputContext(@NotNull SenderT user, @NotNull CommandThrowable<SenderT> throwable) {
        this(user, null, throwable);
    }

    public InputContext(@NotNull SenderT user, @Nullable SenderT agent, @NotNull CommandThrowable<SenderT> throwable) {
        this.user = user;
        this.agent = agent;
        this.throwable = throwable;
    }

    public boolean hasAgent() {
        return agent != null;
    }

    public boolean hasArgument(@NotNull String name) {
        return arguments.containsKey(name);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends SenderT> T getUser() {
        return (T) user;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends SenderT> T getPlayer() {
        return (T) user;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends SenderT> T getSender() {
        return (T) user;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends SenderT> T getSource() {
        return (T) user;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends SenderT> T getAgent() {
        return (T) (agent == null ? user : agent);
    }

    public int getSize() {
        return arguments.size();
    }

    @NotNull
    public String getFullInput() {
        final StringJoiner joiner = new StringJoiner(" ");
        for (Dual<String, Object> input : inputs) {
            joiner.add(input.getLeft());
        }
        for (Map.Entry<String, Dual<String, Object>> entry : arguments.entrySet()) {
            joiner.add(entry.getValue().getLeft());
        }
        return joiner.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(@NotNull Object obj) {
        return (T) getArgumentInput(obj).getRightOrLeft();
    }

    public <T> T getArgument(@NotNull Object obj, @NotNull Object type) {
        return getArgument(obj, Types.of(type));
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(@NotNull Object obj, @NotNull Class<T> type) {
        if (type == String.class) {
            final Dual<String, Object> input = getArgumentInput(obj);
            if (input.getRight() instanceof String) {
                return (T) input.getRight();
            }
            return (T) input.getLeft();
        }
        return getArgument(obj, Types.of(type));
    }

    public <T> T getArgument(@NotNull Object obj, @NotNull TypeParser<T> parser) {
        return parser.parse(getArgumentInput(obj).getRightOrLeft());
    }

    public Dual<String, Object> getArgumentInput(@NotNull Object obj) {
        if (obj instanceof Number) {
            return getArgumentInput(((Number) obj).intValue());
        } else if (obj instanceof String) {
            return getArgumentInput((String) obj);
        } else {
            throw new IllegalArgumentException("The object '" + obj + "' cannot be used to get an argument");
        }
    }

    public Dual<String, Object> getArgumentInput(int index) {
        if (index < 0) {
            return inputs.get(inputs.size() + index);
        }
        int i = 0;
        for (Map.Entry<String, Dual<String, Object>> entry : arguments.entrySet()) {
            if (i == index) {
                return entry.getValue();
            }
            i++;
        }
        throw new IllegalArgumentException("The argument at index " + index + " doesn't exist");
    }

    public Dual<String, Object> getArgumentInput(@NotNull String name) {
        final Dual<String, Object> argument = arguments.get(name);
        if (argument != null) {
            return argument;
        }
        throw new IllegalArgumentException("The argument with name '" + name + "' doesn't exist");
    }

    @NotNull
    public List<String> getPath() {
        return path;
    }

    @NotNull
    public CommandNode<SenderT> getCommand() {
        return getCommand(0);
    }

    @NotNull
    public CommandNode<SenderT> getCommand(int position) {
        return commands.get(commands.size() - (1 + position));
    }

    @NotNull
    public String getCommandName() {
        return getCommandName(0);
    }

    @NotNull
    public String getCommandName(int position) {
        return path.get(path.size() - (1 + position));
    }

    @NotNull
    public List<CommandNode<SenderT>> getCommands() {
        return commands;
    }

    @NotNull
    public CommandResult getResult() {
        return result;
    }

    public void addArgument(@NotNull String input) {
        addArgument(input, null);
    }

    public <T> void addArgument(@NotNull String input, @Nullable T type) {
        addArgument(String.valueOf(getSize()), input, type);
    }

    public <T> void addArgument(@NotNull String name, @NotNull String input, @Nullable T type) {
        arguments.put(name, Dual.of(input, type));
    }

    public void setResult(@NotNull CommandResult result) {
        this.result = result;
    }

    @NotNull
    public InputContext<SenderT> then(@NotNull CommandNode<SenderT> command, @NotNull String... args) {
        return then(command.getName(), command, args);
    }

    @NotNull
    public InputContext<SenderT> then(@NotNull String name, @NotNull CommandNode<SenderT> command, @NotNull String... args) {
        inputs.addAll(arguments.values());
        inputs.add(Dual.of(name, null));
        arguments.clear();
        path.add(name);
        commands.add(command);

        final CommandResult result = command.then(this, args);
        if (result == CommandResult.FAIL_SYNTAX) {
            sendUsage();
        }
        setResult(result);
        return this;
    }

    @NotNull
    public CommandSuggestion<SenderT> suggest(@NotNull CommandNode<SenderT> command, @NotNull String... args) {
        return suggest(command.getName(), command, args);
    }

    @NotNull
    public CommandSuggestion<SenderT> suggest(@NotNull String name, @NotNull CommandNode<SenderT> command, @NotNull String... args) {
        inputs.addAll(arguments.values());
        inputs.add(Dual.of(name, null));
        arguments.clear();
        path.add(name);
        commands.add(command);

        return command.suggest(this, args);
    }

    public void sendMessage(@NotNull String msg) {
        throwable.sendMessage(user, msg);
    }

    public void sendColoredMessage(@NotNull String msg) {
        throwable.sendColoredMessage(user, msg);
    }

    public void sendPermissionMessage(@NotNull String permission) {
        throwable.sendPermissionMessage(user, permission);
    }

    public void sendKey(@NotNull String key, @Nullable Object... args) {
        throwable.sendKey(user, getCommand().getPath() + '.' + key, args);
    }

    public void sendUsage() {
        throwable.sendUsage(user, this);
    }
}
