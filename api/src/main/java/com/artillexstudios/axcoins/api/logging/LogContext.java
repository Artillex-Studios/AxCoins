package com.artillexstudios.axcoins.api.logging;

import com.artillexstudios.axapi.collections.IdentityArrayMap;
import com.artillexstudios.axcoins.api.logging.arguments.LogArgument;
import org.jspecify.annotations.Nullable;

public class LogContext {
    private final IdentityArrayMap<LogArgument<Object>, Object> arguments;

    LogContext(IdentityArrayMap<LogArgument<Object>, Object> arguments) {
        this.arguments = arguments;
    }

    public boolean contains(LogArgument<?> argument) {
        return this.arguments.containsKey(argument);
    }

    @Nullable
    public <T> T argument(LogArgument<T> argument) {
        return (T) this.arguments.get(argument);
    }

    public static class Builder {
        private final IdentityArrayMap<LogArgument<Object>, Object> arguments = new IdentityArrayMap<>();

        public <T> LogContext.Builder withArgument(LogArgument<T> argument, T value) {
            this.arguments.put((LogArgument<Object>) argument, value);
            return this;
        }

        public LogContext build() {
            return new LogContext(this.arguments);
        }
    }
}
