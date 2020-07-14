package com.example.csv.crossjoinflatfile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

@Configuration
public class Configs {
	@Bean
	public StopWatch stopWatch() {
		return new StopWatch();
	}
}
