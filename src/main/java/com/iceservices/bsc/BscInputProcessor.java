package com.iceservices.bsc;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@Component
@Slf4j
public class BscInputProcessor {

	@Autowired
	QueryService queryService;

	@Scheduled(fixedDelay = 15000, initialDelay = 10000)
	public void process() throws SQLException {
		BigDecimal dacTrlKey = BigDecimal.ZERO;
		log.info("process start with dacTrlKey {} ", dacTrlKey);
		while(dacTrlKey.compareTo(BigDecimal.ZERO) != -1) {
			log.info("processFromRow started with dacTrlKey {} ", dacTrlKey);
			dacTrlKey = processFromRow(dacTrlKey);
			log.info("processFromRow ended with dacTrlKey {} ", dacTrlKey);
		}
		log.info("process end with dacTrlKey {}", dacTrlKey);
	}

	private BigDecimal processFromRow(BigDecimal dacTrlKey) throws SQLException {

		@Cleanup PreparedStatement sourceStmt = queryService.getSourceStmt(dacTrlKey);
		@Cleanup PreparedStatement insertHeaderDataStmt = queryService.getInsertHeaderDataStmt();
		@Cleanup PreparedStatement insertWorkDataStmt = queryService.getInsertWorkDataStmt();
		@Cleanup ResultSet sourceResult = sourceStmt.executeQuery();

		int processedRows = 0;
		String prevHdrParams = null;
		String curHdrParams;
		BigDecimal hdrSequenceValue = null;
		while (sourceResult.next() && processedRows < queryService.getFetchDataLimit()) {
			curHdrParams = getCurrentHeaderParameters(sourceResult);
			if (!curHdrParams.equals(prevHdrParams)) {
				hdrSequenceValue =  queryService.getNextHdrSeqValue();
				bindInsertHeaderDataParams(insertHeaderDataStmt, sourceResult, hdrSequenceValue);
				insertHeaderDataStmt.addBatch();
				log.info("new data header added to batch -> " + curHdrParams);
			}
			prevHdrParams = curHdrParams;

			dacTrlKey = bindInsertWorkDataParams(insertWorkDataStmt, sourceResult, hdrSequenceValue);
			//TODO here we'll use log.debug
			log.info("new work added to batch -> " + sourceResult.getBigDecimal("DACTRLKEY")+ ", " + hdrSequenceValue + ", "+sourceResult.getBigDecimal("WORKKEY"));
			insertWorkDataStmt.addBatch();

			processedRows++;
		}

		insertHeaderDataStmt.executeBatch();
		log.info("execute header batch");
		insertWorkDataStmt.executeBatch();
		log.info("execute work batch");

		if (!sourceResult.next()) {
			return new BigDecimal(-2);
		}

		return dacTrlKey;
	}

	private BigDecimal bindInsertWorkDataParams(PreparedStatement insertWorkDataStmt, ResultSet sourceResult,
			BigDecimal hdrSequenceValue) throws SQLException {
		int paramIdx = 1;
		BigDecimal dacTrlKey = sourceResult.getBigDecimal("DACTRLKEY");
		insertWorkDataStmt.setBigDecimal(paramIdx++, dacTrlKey);
		insertWorkDataStmt.setBigDecimal(paramIdx++, hdrSequenceValue);
		insertWorkDataStmt.setBigDecimal(paramIdx++, sourceResult.getBigDecimal("WORKKEY"));
		return dacTrlKey;
	}

	private void bindInsertHeaderDataParams(PreparedStatement insertHeaderDataStmt, ResultSet sourceResult, BigDecimal hdrSequenceValue)
			throws SQLException {

		int paramIdx = 1;
		insertHeaderDataStmt.setBigDecimal(paramIdx++, hdrSequenceValue);
		insertHeaderDataStmt.setBigDecimal(paramIdx++, sourceResult.getBigDecimal("USAGETERR"));
		insertHeaderDataStmt.setDate(paramIdx++, sourceResult.getDate("USGDTE"));
		insertHeaderDataStmt.setDate(paramIdx++, sourceResult.getDate("DISTDTE"));
		insertHeaderDataStmt.setString(paramIdx++, sourceResult.getString("TYPEOFUSE1"));
		insertHeaderDataStmt.setString(paramIdx++, sourceResult.getString("TYPEOFUSE2"));
		insertHeaderDataStmt.setString(paramIdx++, sourceResult.getString("IPIRIGHTPR"));
		insertHeaderDataStmt.setString(paramIdx++, sourceResult.getString("IPIRIGHTMR"));
		insertHeaderDataStmt.setString(paramIdx++, sourceResult.getString("DISTSOCCDE"));
		insertHeaderDataStmt.setDate(paramIdx++, new java.sql.Date(new Date().getTime()));
		insertHeaderDataStmt.setBigDecimal(paramIdx++, new BigDecimal(1234));
		insertHeaderDataStmt.setString(paramIdx++, "BSC");
		insertHeaderDataStmt.setDate(paramIdx++, new java.sql.Date(new Date().getTime()));
		insertHeaderDataStmt.setBigDecimal(paramIdx++, new BigDecimal(1234));
		insertHeaderDataStmt.setString(paramIdx++, "BSC");
	}

	private String getCurrentHeaderParameters(ResultSet selectDataResult) throws SQLException {
		//TODO check order here with select order by clause
		return new StringBuilder()
				.append(selectDataResult.getInt("USAGETERR"))
				.append(selectDataResult.getDate("USGDTE"))
				.append(selectDataResult.getDate("DISTDTE"))
				.append(selectDataResult.getString("TYPEOFUSE1"))
				.append(selectDataResult.getString("TYPEOFUSE2"))
				.append(selectDataResult.getString("IPIRIGHTPR"))
				.append(selectDataResult.getString("IPIRIGHTMR"))
				.append(selectDataResult.getString("DISTSOCCDE"))
				.toString();
	}

}
