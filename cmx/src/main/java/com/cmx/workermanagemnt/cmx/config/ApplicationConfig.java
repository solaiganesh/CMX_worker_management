package com.cmx.workermanagemnt.cmx.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ StorageProperties.class, TranslationProperties.class })
public class ApplicationConfig {
}
