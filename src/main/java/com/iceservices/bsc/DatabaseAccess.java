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
public class DatabaseAccess {

	private static final String DATABASE_PROPERTIES = "database.properties";

	private final String url;
	private final String user;
	private final String password;

	private final @Getter Connection bscConnection;
	private final @Getter Connection iacConnection;

	public DatabaseAccess() throws ClassNotFoundException, SQLException, IOException {
		Class.forName("com.ibm.as400.access.AS400JDBCDriver");
		Properties configuration = getDatabaseProperties();
		url = configuration.getProperty("url");
		user = configuration.getProperty("usr");
		password = configuration.getProperty("pwd");

		bscConnection = DriverManager.getConnection(url, user, password);
		bscConnection.setSchema(configuration.getProperty("bscSchema"));
		log.info("Database bscConnection created for bsc schema with "+ url);

		iacConnection = DriverManager.getConnection(url, user, password);
		iacConnection.setSchema(configuration.getProperty("iacSchema"));
		log.info("Database iacConnection created for iac schema with "+ url);
	}

	private Properties getDatabaseProperties() throws IOException {
		Properties properties = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		@Cleanup InputStream inputStream = loader.getResourceAsStream(DATABASE_PROPERTIES);
		properties.load(inputStream);
		return properties;
	}
}
