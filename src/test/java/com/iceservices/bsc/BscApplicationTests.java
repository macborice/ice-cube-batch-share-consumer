package com.iceservices.bsc;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class BscApplicationTests {


	private static final String DATABASE_PROPERTIES = "database.properties";
	private static final String VERIFY_SQL_1 = "select count(1) from BSCHDR LIMIT 1";

	@Autowired
	DatabaseCleaner databaseCleaner;

	@Autowired
	BscInputProcessor bscInputProcessor;

	@Test
	void contextLoads() {
	}

	@Test
	void databaseAccess() throws ClassNotFoundException, SQLException {
		Class.forName("com.ibm.as400.access.AS400JDBCDriver");
		Properties configuration = getDatabaseProperties();
		String url = configuration.getProperty("url");
		String user = configuration.getProperty("usr");
		String password = configuration.getProperty("pwd");
		@Cleanup Connection connection = DriverManager.getConnection(url, user, password);
		connection.setSchema(configuration.getProperty("bscSchema"));
		@Cleanup PreparedStatement preparedStatement = connection.prepareStatement(VERIFY_SQL_1);
		@Cleanup ResultSet resultSet = preparedStatement.executeQuery();
		if (!resultSet.next()) {
			fail();
		}
	}

	@SneakyThrows
	private Properties getDatabaseProperties() {
		Properties properties = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		@Cleanup InputStream inputStream = loader.getResourceAsStream(DATABASE_PROPERTIES);
		properties.load(inputStream);
		return properties;
	}

	@Test
	void inputProcessing() throws SQLException {
		databaseCleaner.clearBscTables();
		bscInputProcessor.process();
	}

}
