package com.prashant.queuectl.config;


import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.jline.utils.AttributedString;

import org.springframework.shell.jline.PromptProvider;


@Configuration
public class AppConfig {

    private int maxRetries = 3;
    private int backoffSeconds = 2;

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Bean
    public ModelMapper getModelMapper(){
        return new ModelMapper();
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("queuectl:> ");
    }

    public void setBackoffSeconds(int value) {
        backoffSeconds = value;
    }

    public int getBackoffSeconds() {
        return backoffSeconds;
    }
}
