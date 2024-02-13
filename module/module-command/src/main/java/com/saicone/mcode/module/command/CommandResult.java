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
     * Command generates an error while been executed.
     */
    FAIL_EXECUTION,
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
        return this == NOT_FOUND;
    }

    public boolean isFail() {
        return this == NO_PERMISSION || this == FAIL_SYNTAX || this == FAIL_EVAL || this == FAIL_EXECUTION;
    }
}
