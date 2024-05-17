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

    public boolean hasAgent() {
        return agent != null;
    }

    public boolean hasArgument(@NotNull String name) {
        return arguments.containsKey(name);
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

        final int start = command.getSubStart(user);
        if (command.hasSubCommands() && start == 0 && args.length > 0) {
            path.add(name);
            commands.add(command);

            final String sub = args[start];
            for (CommandNode<SenderT> node : command.getSubCommands()) {
                if (node.matches(sub)) {
                    final InputContext<SenderT> context = then(sub, node, Arrays.copyOfRange(args, 1, args.length));
                    if (context.getResult() == CommandResult.BREAK) {
                        break;
                    } else if (context.getResult() != CommandResult.CONTINUE) {
                        return this;
                    }
                }
            }

            if (getResult() == CommandResult.BREAK) {
                setResult(command.execute(this));
                return this;
            }
        }

        final int consumedArgs = command.parseInput(args, arguments::put);
        boolean consumed = false;
        final int minArgs = command.getMinArgs(user);
        if (getSize() < minArgs) {
            if (consumedArgs < args.length) {
                consumed = true;
                for (int i = getSize(); getSize() < minArgs && i < args.length; i++) {
                    arguments.put(String.valueOf(i), Dual.of(args[i], null));
                }
            }
            if (getSize() < minArgs) {
                sendUsage();
                return this;
            }
        }

        path.add(name);
        commands.add(command);

        if (command.hasSubCommands() && getSize() == start) {
            final String sub = args[start];
            for (CommandNode<SenderT> node : command.getSubCommands()) {
                if (node.matches(sub)) {
                    final InputContext<SenderT> context = then(sub, node, consumedArgs < args.length ? Arrays.copyOfRange(args, consumedArgs + 1, args.length) : new String[0]);
                    if (context.getResult() == CommandResult.BREAK) {
                        break;
                    } else if (context.getResult() != CommandResult.CONTINUE) {
                        return this;
                    }
                }
            }
        } else if (!consumed && consumedArgs < args.length) {
            for (int i = getSize(); i < args.length; i++) {
                arguments.put(String.valueOf(i), Dual.of(args[i], null));
            }
        }

        setResult(command.execute(this));
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

        final int start = command.getSubStart(user);
        if (command.hasSubCommands() && start == 0) {
            if (args.length < 1) {
                return command.getSubCommandsSuggestion();
            } else {
                final CommandNode<SenderT> node = command.getSubCommand(args[0]);
                if (node == null) {
                    return CommandSuggestion.empty();
                } else {
                    return suggest(node, args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
                }
            }
        }

        final int consumedArgs = command.compileInput(args, (key, input) -> arguments.put(key, Dual.of(input, null)));
        if (getSize() < command.getMinArgs(user)) {
            final CommandArgument<SenderT> argument = command.getArgument(getSize());
            if (argument == null) {
                return CommandSuggestion.empty();
            } else {
                final CommandSuggestion<SenderT> suggestion = argument.getSuggestion();
                if (suggestion == null) {
                    return CommandSuggestion.empty();
                }
                return new CommandSuggestion<>() {
                    @Override
                    public @Nullable Map<String, String> suggest(@NotNull InputContext<SenderT> context) {
                        return suggestion.suggest(context);
                    }
                    @Override
                    public @Nullable Map<String, String> get() {
                        return suggestion.suggest(InputContext.this);
                    }
                };
            }
        } else if (command.hasSubCommands() && getSize() == start) {
            if (args.length <= consumedArgs + 1) {
                return command.getSubCommandsSuggestion();
            } else {
                final CommandNode<SenderT> node = command.getSubCommand(args[consumedArgs + 1]);
                if (node == null) {
                    return CommandSuggestion.empty();
                } else {
                    return suggest(node, args.length > consumedArgs + 2 ? Arrays.copyOfRange(args, consumedArgs + 1, args.length) : new String[0]);
                }
            }
        }

        return CommandSuggestion.empty();
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
