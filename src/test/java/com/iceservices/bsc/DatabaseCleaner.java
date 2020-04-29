package com.iceservices.bsc;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Component
@Slf4j
public class DatabaseCleaner {

	private static final String CLEAR_BSCHDR_TABLE = "DELETE FROM BSCHDR";
	private static final String CLEAR_BSCWRK_TABLE = "DELETE FROM BSCWRK";

	@Autowired
	DatabaseAccess databaseAccess;

	@SneakyThrows
	public void clearBscTables() {

		Connection connection = databaseAccess.getBscConnection();
		@Cleanup PreparedStatement selectStatement = connection.prepareStatement(CLEAR_BSCHDR_TABLE);
		int result = selectStatement.executeUpdate();
		log.info("Deleted rows: " + result);

		selectStatement = connection.prepareStatement(CLEAR_BSCWRK_TABLE);
		result = selectStatement.executeUpdate();
		log.info("Deleted rows: " + result);
	}
}
