package com.iceservices.bsc;

import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@Slf4j
public class QueryService {

	@Autowired
	AppConfig appConfig;

	@Autowired
	DatabaseAccess db;

	@Getter private Integer fetchDataLimit;

	//TODO externalize from IACUGV
	private String status1 = "00";
	private String status2 = "01";
	private String status3 = "05";
	private BigDecimal usageValue = new BigDecimal(0.50);
	private BigDecimal highValueWorkKeyStart = new BigDecimal(1000000000);

	public void reloadConfiguration() {
		fetchDataLimit = Integer.valueOf(appConfig.getPartialDataToProcessLimit());
	}

	public PreparedStatement getSourceStmt(BigDecimal dacTrlKey) throws SQLException {
		fetchDataLimit = Integer.valueOf(appConfig.getPartialDataToProcessLimit());
		PreparedStatement stmt = db.getIacConnection().prepareStatement(getSourceQuery());
		int paramIdx = 1;
		stmt.setString(paramIdx++, status1);
		stmt.setString(paramIdx++, status2);
		stmt.setString(paramIdx++, status3);
		stmt.setBigDecimal(paramIdx++, usageValue);
		stmt.setBigDecimal(paramIdx++, highValueWorkKeyStart);
		stmt.setBigDecimal(paramIdx++, dacTrlKey);
		return stmt;
	}

	public PreparedStatement getInsertHeaderDataStmt() throws SQLException {
		return db.getBscConnection().prepareStatement(getInsertHeaderDataQuery());
	}

	public PreparedStatement getInsertWorkDataStmt() throws SQLException {
		return db.getBscConnection().prepareStatement(getInsertWorkDataQuery());
	}

	public BigDecimal getNextHdrSeqValue() throws SQLException {
		@Cleanup Statement stmt = db.getBscConnection().createStatement();
		@Cleanup ResultSet rs = stmt.executeQuery("VALUES NEXTVAL FOR BSCHDR_SEQ");
		if(rs.next()) {
			return new BigDecimal(rs.getLong(1));
		}
		throw new SQLException("Sequence error");
	}

	private String getSourceQuery() {
		//TODO make sure we do not pass duplicates to cube / there are a lot of ducplicates
		return "SELECT DISTINCT DAC.DACTRLKEY, DAC.WORKKEY, DAC.STATUS, DAC.USAGETERR, DAC.USGDTE, DAC.DISTDTE, DAC.TYPEOFUSE1, DAC.TYPEOFUSE2, DAC.IPIRIGHTPR, DAC.IPIRIGHTMR, DAC.DISTSOCCDE "
				+ "FROM IACDAC DAC "
				+ "JOIN IACWSV WSV ON DAC.WORKKEY = WSV.WORKKEY AND DAC.USAGETERR = WSV.TERRITORY AND (DAC.TYPEOFUSE1 = WSV.TYPEOFUSE OR DAC.TYPEOFUSE2 = WSV.TYPEOFUSE) "
				+ "AND (DAC.IPIRIGHTPR = WSV.TYPEOFRGHT OR DAC.IPIRIGHTMR = WSV.TYPEOFRGHT) AND DAC.DISTSOCCDE = WSV.DISTSOCCDE "
				+ "WHERE DAC.STATUS IN (?, ?, ?) AND DAC.USAGEVALUE >= ? AND DAC.WORKKEY < ? AND DAC.DACTRLKEY > ? "
				+ "AND WSV.REFRESHRQ = 'Y' AND DAC.USGDTE BETWEEN WSV.VALIDFR AND WSV.VALIDTO "
				+ "ORDER BY DAC.USAGETERR, DAC.USGDTE, DAC.DISTDTE, DAC.TYPEOFUSE1, DAC.TYPEOFUSE2, DAC.IPIRIGHTPR, DAC.IPIRIGHTMR, DAC.DISTSOCCDE ASC "
				//	optimized by MacRus / getCurrentHeaderParameters must be adapted
				// + "ORDER BY DAC.USAGETERR, DAC.USGDTE, DAC.DACTRLKEY, DAC.DISTDTE, DAC.TYPEOFUSE1, DAC.TYPEOFUSE2, DAC.IPIRIGHTPR, DAC.IPIRIGHTMR, DAC.DISTSOCCDE ASC "
				+ "FETCH FIRST "+ (fetchDataLimit+2) +" ROWS ONLY OPTIMIZE FOR "+ (fetchDataLimit+2) +" ROWS FOR READ ONLY"
				;
	}

	private String getInsertHeaderDataQuery() {
		return "INSERT INTO BSCHDR "
				+ "(BSCHDRKEY, USAGETERR, USGDTE, DISTDTE, TYPEOFUSE1, TYPEOFUSE2, IPIRIGHTPR, IPIRIGHTMR, DISTSOCCDE, INSDATE, INSNUMBER, INSUSER, UPDDATE, UPDNUMBER, UPDUSER) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	private String getInsertWorkDataQuery() {
		return "INSERT INTO BSCWRK (DACTRLKEY, BSCHDRKEY, WORKKEY) VALUES (?, ?, ?)";
	}
}
