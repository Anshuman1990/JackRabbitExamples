/**
 * Copyright (c) 2015 by Digital Harbor Pvt.Ltd. All rights reserved.
 * This software is the confidential and proprietary information
 * of Digital Harbor. ("Confidential Information").
 */
package com.DH.Oak;

import java.io.File;

/**
 * Title: DMSConstants</p>
 * Description: DMSConstants.</p>
 * @author Diensh Hegde
 * @version 1.0
 */
public final class DMSConstants {

	
	private DMSConstants(){}
	
	/*DB related contsants */
	public static final String REP_DB_NAME="db_name";
	public static final String REP_DB_URL="mongo.connection.uri";
	public static final String REP_DB_USERNAME="db_username";
	public static final String REP_DB_PASSWORD="db_password";

	/*Repository related constants */
	public static final String REP_USERNAME="rep_username";
	public static final String REP_PASSWORD="rep_password";
	public static final String REP_TYPE="oak";
	
	/*JCR related constants */
	public static final String DMS_ID="DMSID";
	public static final String DMS_NODE = "NODE";
	public static final String JCR_DATA = "jcr:data";
	public static final String JCR_MIME_TYPE = "jcr:mimeType";
	public static final String UTF_8 = "UTF-8";
	public static final String JCR_ENCODING = "jcr:encoding";
	public static final String NT_RESOURCE = "nt:resource";
	public static final String JCR_CONTENT = "jcr:content";
	public static final String JCR_NODE_TYPE_NAME = "jcr:nodeTypeName";
	public static final String JCR_TITLE = "jcr:nodeTypeName";
	public static final String PROPERTY = "property";
	public static final String REINDEX = "reindex";
	public static final String UNIQUE = "unique";
	public static final String PROPERTY_NAMES = "propertyNames";
	public static final String REINDEX_ASYNC = "reindex-async";
	public static final String TYPE = "type";
	public static final String OAK_QUERY_INDEX_DEFINITION = "oak:QueryIndexDefinition";
	public static final String OAK_INDEX = "/oak:index";
	public static final String JCR_CREATED_BY = "jcr:createdBy";
	public static final String JCR_LAST_MODIFIED_BY = "jcr:lastModifiedBy";
	public static final String MIX_LOCKABLE = "mix:lockable";
	public static final String MIX_VERSIONABLE = "mix:versionable";
	public static final int DATE_FIELD_VALUE_IN_JACKRABBIT = 5;
	public static final String DMS_RELATION_SHIP_NAME="PFS_RELNAME";
	public static final String DMS_FOLDER_REL_NAME="FOLDER_RELNAME";
	public static final String DMS_KEYWORD_ATTRIBUTE="Keyword";
	public static final String DMS_DELETE_ATTRIBUTE="IsDeleted";
	public static final String XPATH_QUERY="XPATH";
	public static final String SQL2_QUERY="JCR_SQL2";
	public static final String JCR_PATH="jcr:path";
	public static final String DMSID_INDEX="DMSIDIndex";
	public static final String PFS_REALNAME_INDEX="PFSRELNAMEIndex";
	public static final String ISDELTED_INDEX="IsDeletedIndex";
	public static final String KEYWORD_INDEX="KeywordIndex";
	public static final String NT_UNSTRUCTURED="nt:unstructured";
	public static final String LUCENE_INDEX_PATH="/oak:index/lucene";
	public static final String INDEX_ID="INDEX_ID";
	public static final String INDEX_NAME="INDEX_NAME";
	public static final String INDEX_PROPERTY_NAMES="PROPERTY_NAMES";
	public static final String ISUNIQUE="ISUNIQUE";
	public static final String ENCLOSETYPE="ENCLOSETYPE";
	public static final String INDEXTYPE="INDEXTYPE";
	public static final String INDEX_PROP_SEPARATOR="|";
	public static final String DMS_CURRENT_FOLDER_NAME = "CurFolderName";
	public static final String BACK_SLASH="/";
	public static final String DMS_ABSOLUTE_PATH="AbsolutePath";
	public static final String IS_FOLDER="Y";
	public static final String DMS_FOLDER = "DMSFolder";
	public static final String DMS_CONTENT = "DMSContent";
	public static final String DMSFolder_INDEX="DMSFolderIndex";
	public static final String DMS_PREVIOUS_FOLDER_PATH="PreviousFolderPath";
	public static final String JCR_TYPE="jcr:";
	public static final String DMS_MOVE_FILE_ONLY="fileOnly";
	public static final String DMS_CONTENT_FOLDER_ATTRIBUTE="CurrentFolder";
	public static final String TARGET_PATH="TargetPath";
	public static final String CaseFolder="/CaseFolder";
	public static final String APP_SERVER_HOME=System.getProperty("AppServerHome");
	public static final String JCR_UUUD="jcr:uuid";
	public static final String FILE_NAME = "FileName";
	public static final String CONTENT = "Content";
	public static final String TEMP_CaseFolder="/CaseFolder";
	public static final String TEMP_JSON_FILE="/final.json";
	//public static final String USER_TEMP_FOLDER="C:/Temp/Zip";
	public static final String USER_TEMP_FOLDER=System.getProperty("user.home")+File.separator+"DMS";
	public static final String ZIP_EXTENSION=".zip";
	public static final String DMS_EXPORT_DIR="DMS_EXPORT_DIR";
	public static final String FOLDER_EXPORT="FolderExport";
	
	public static final String PIIE_ROOT="piieroot";
	
	/*Datasource related constants */
	public static final String JNDI_DS="java:jboss/oakDS";
	public static final String OAK_REPO_DS="java:jboss/repositoryDS";
	public static final String PARAMNAME="param_name";
	public static final String PARAMVALUE="param_value";
	public static final String FOLDER_ABSOULTE_PATH="FolderAbsolutePath";
	public static final String OAKSESSIONNAME="admin";
	public static final String OAKSESSIONPASSWD="admin";
	public static final String ENCRYPT_ALGORITHM="AES";
	public static final String ENCRYPT_SALT="DHarborEncryptor";
	public static final String MONGO_SWITCH="MongoSwitch";
	
	public static final String SUBJECT = "SUBJECT";
	public static final String BASE_VERSION = "BaseVersion";
	
//	public static final String PIIE_FILE = "piie:file";
//	public static final String PIIE_FOLDER = "piie:folder";
	
}
