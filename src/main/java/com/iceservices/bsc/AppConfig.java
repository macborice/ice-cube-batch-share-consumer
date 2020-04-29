package com.iceservices.bsc;

import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
@Slf4j
public class AppConfig {

	private static final String APP_PROPERTIES = "application.properties";

	private @Getter final String partialDataToProcessLimit;

	public AppConfig() throws IOException {
		Properties configuration = getAppProperties();
		//TODO first read from UGV then from props
		partialDataToProcessLimit = configuration.getProperty("processDataLimit");
		log.info("AppConfig loaded");
	}

	private Properties getAppProperties() throws IOException {
		Properties properties = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		@Cleanup InputStream inputStream = loader.getResourceAsStream(APP_PROPERTIES);
		properties.load(inputStream);
		return properties;
	}
}
