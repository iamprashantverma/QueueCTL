package com.prashant.queuectl.cli;

import com.prashant.queuectl.config.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class ConfigShellCommand {

    private final AppConfig appConfig;

    @ShellMethod(key = "config set", value = "Manage configuration (retry, backoff, etc.)")
    public String setConfig(@ShellOption(help = "Configuration key, e.g., max-retries or backoff") String key, @ShellOption(help = "Configuration value") int value
    ) {
        switch (key.toLowerCase()) {
            case "max-retries":
                appConfig.setMaxRetries(value);
                return "maxRetries set to " + value;
            case "backoff":
                appConfig.setBackoffSeconds(value);
                return "backoffSeconds set to " + value;
            default:
                return "Unknown config key: " + key;
        }
    }

    @ShellMethod(key = "config get", value = "Show current configuration")
    public String getConfig(@ShellOption(defaultValue = "all") String key) {
        switch (key.toLowerCase()) {
            case "max-retries":
                return "maxRetries = " + appConfig.getMaxRetries();
            case "backoff":
                return "backoffSeconds = " + appConfig.getBackoffSeconds();
            case "all":
            default:
                return "maxRetries = " + appConfig.getMaxRetries()
                        + ", backoffSeconds = " + appConfig.getBackoffSeconds();
        }
    }
}
