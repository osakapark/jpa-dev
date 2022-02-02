package com.mystudy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties("app")
public class AppProperties {
	private String host;
	private String mailFrom;
}
