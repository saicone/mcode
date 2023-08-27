package com.saicone.mcode.module.command;

public enum CommandResult {

    /**
     * Command execution finish correctly.
     */
    DONE,
    /**
     * Command cannot be found.
     */
    NOT_FOUND,
    /**
     * Command was found, but is not registered.
     */
    NOT_REGISTERED,
    /**
     * User doesn't have permission to execute the command.
     */
    NO_PERMISSION,
    /**
     * Provided syntax is invalid.
     */
    FAIL_SYNTAX,
    /**
     * Command evaluator cannot pass the tests.
     */
    FAIL_EVAL,
    /**
     * Command execution was abruptly finished.
     */
    RETURN,
    /**
     * Execution must continue with next sub command in the list.
     */
    CONTINUE,
    /**
     * Execution must continue with parent command execution.
     */
    BREAK;

    public boolean isDone() {
        return this == DONE || this == RETURN;
    }

    public boolean isUnknown() {
        return this == NOT_FOUND || this == NOT_REGISTERED;
    }

    public boolean isFail() {
        return this == NO_PERMISSION || this == FAIL_SYNTAX || this == FAIL_EVAL;
    }
}
