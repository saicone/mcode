package com.saicone.mcode.module.script.condition;

import com.saicone.mcode.module.script.Condition;
import com.saicone.mcode.module.script.EvalUser;
import com.saicone.mcode.module.script.ScriptFunction;
import com.saicone.mcode.util.text.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class Compare extends Condition {

    public Compare(@Nullable String value) {
        super(value);
    }

    @Override
    public @Nullable ScriptFunction<EvalUser, Boolean> build() {
        if (getValue() == null) {
            return null;
        }
        final String[] split = Strings.splitBySpaces(getValue());
        if (split.length < 3) {
            return (user) -> user.parseBoolean(getValue(), false);
        }

        switch (split[1].replace("-" , "").replace(" ", "")) {
            case "==":
            case "=":
            case "is":
            case "equals":
                return (user) -> user.parse(split[0]).equals(user.parse(split[2]));
            case "-eq":
            case "numberequals":
                return user -> user.parseBy(Double::parseDouble, split[0], 0D).equals(user.parseBy(Double::parseDouble, split[2], 0));
            case "!=":
            case "isnot":
            case "isnotequals":
                return (user) -> !user.parse(split[0]).equals(user.parse(split[2]));
            case "-=":
            case "equalsignorecase":
                return (user) -> user.parse(split[0]).equalsIgnoreCase(user.parse(split[2]));
            case ">":
            case "-gt":
            case "upperthan":
            case "morethan":
                return (user) -> user.parseInt(split[0], 0) > user.parseInt(split[2], 0);
            case ">=":
            case "upperthanorequal":
            case "morethanorequal":
                return (user) -> user.parseInt(split[0], 0) >= user.parseInt(split[2], 0);
            case "<":
            case "-lt":
            case "lowerthan":
            case "lessthan":
                return (user) -> user.parseInt(split[0], 0) < user.parseInt(split[2], 0);
            case "<=":
            case "lowerthanorequal":
            case "lessthanorequal":
                return (user) -> user.parseInt(split[0], 0) <= user.parseInt(split[2], 0);
            case "$=":
            case "=~":
            case "matches":
                final Pattern pattern = Pattern.compile(split[0]);
                return (user) -> pattern.matcher(user.parse(split[2])).matches();
            default:
                return null;
        }
    }
}
