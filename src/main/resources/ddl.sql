DROP TABLE BSCHDR;

CREATE TABLE BSCHDR (
	BSCHDRKEY NUMERIC(11,0) DEFAULT 0 NOT NULL,
	USAGETERR NUMERIC(4,0) NOT NULL,
	USGDTE DATE DEFAULT '0001-01-01' NOT NULL,
	DISTDTE DATE DEFAULT '0001-01-01' NOT NULL,
	TYPEOFUSE1 CHAR(3) DEFAULT ' ' NOT NULL,
	TYPEOFUSE2 CHAR(3) DEFAULT ' ' NOT NULL,
	IPIRIGHTPR CHAR(2) DEFAULT ' ' NOT NULL,
	IPIRIGHTMR CHAR(2) DEFAULT ' ' NOT NULL,
	DISTSOCCDE CHAR(3) DEFAULT ' ' NOT NULL,
	INSDATE DATE DEFAULT '0001-01-01' NOT NULL,
	INSNUMBER NUMERIC(11,0) DEFAULT 0 NOT NULL,
	INSUSER CHAR(90) DEFAULT ' ' NOT NULL,
	UPDDATE DATE DEFAULT '0001-01-01' NOT NULL,
	UPDNUMBER NUMERIC(11,0) DEFAULT 0 NOT NULL,
	UPDUSER CHAR(90) DEFAULT ' ' NOT NULL,
	--
	CONSTRAINT Q_BSCDTATE_BSCHDR_BSCHDRKEY_00001 PRIMARY KEY (BSCHDRKEY)
);

DROP TABLE BSCWRK;

CREATE TABLE BSCWRK (
    DACTRLKEY NUMERIC(11,0) DEFAULT 0 NOT NULL,
	BSCHDRKEY NUMERIC(11,0) DEFAULT 0 NOT NULL,
	WORKKEY NUMERIC(11,0) DEFAULT 0 NOT NULL,
    CONSTRAINT Q_BSCDTATE_BSCWRK_BSCWRKKEY_00001 PRIMARY KEY (DACTRLKEY) --ASKed later by workkey so probably PK workkey
);

DROP TABLE BSCBCH;

CREATE TABLE BSCBCH (
    BSCHDRKEY NUMERIC(11,0) DEFAULT 0 NOT NULL,
	REPORTID CHAR(36) DEFAULT ' ' NOT NULL,
);

CREATE SEQUENCE BSCHDR_SEQ START WITH 1
                          INCREMENT BY 1
                          NO MAXVALUE
                          NO CYCLE
                          CACHE 24;

