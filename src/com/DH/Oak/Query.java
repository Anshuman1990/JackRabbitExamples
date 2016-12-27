package com.DH.Oak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;

import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.apache.jackrabbit.oak.plugins.nodetype.NodeTypeConstants;



public class Query {
	private QueryManager queryManager = null;
	private Session session = null;
	
	public Query(QueryManager Qm,Session S) {
		this.queryManager = Qm;
		this.session = S;
	}
	
	//search using DMSID
	public List<Map<String, Object>> searchUsingDMSID(String dmsId,List<String> attributesList) throws InvalidQueryException,
			RepositoryException, FileNotFoundException, IOException {
//		logger.debug();
		System.out.println("dmsId:" + dmsId);
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		resultList = Collections.synchronizedList(resultList);
		if (dmsId == null) {
//			logger.warn();
			System.out.println("Input parameter null");
			return resultList;
		}

		try {
			String expression = "select * from [nt:base] as b where b.["+ DMSConstants.DMS_ID + "]='" + dmsId + "'";
			System.out.println("Expression= "+expression);
			if (queryManager != null) {
				resultList = getSearchResultList(expression,DMSConstants.SQL2_QUERY, dmsId,attributesList);
			}
		} finally {
			session.logout();
		}
		return resultList;
	}
	
	public List<Map<String, Object>> searchUsingXPath(String fullQuery,
			String dmsId,List<String> attributesList)
			throws InvalidQueryException, RepositoryException,
			FileNotFoundException, IOException {
		
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		resultList = Collections.synchronizedList(resultList);
		

		try {
			queryManager = session.getWorkspace().getQueryManager();
			if (queryManager != null) {
				resultList = getSearchResultList(fullQuery,DMSConstants.XPATH_QUERY, dmsId,attributesList);
			}

		} finally {
			session.logout();
		}
		return resultList;
	}
	
	//get the search result
	private List<Map<String, Object>> getSearchResultList(
			String queryExpression, String queryType, String dmsId,List<String> attributesList)
			throws InvalidQueryException, RepositoryException {

		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		
//		if(queryExpression.indexOf("@PFS_RELNAME,'DMSContent'") > 0) {
//			queryExpression= queryExpression.replaceAll(NodeTypeConstants.NT_OAK_UNSTRUCTURED, DMSConstants.PIIE_FILE);
//			queryExpression= queryExpression.replaceAll(NodeTypeConstants.NT_UNSTRUCTURED, NodeTypeConstants.NT_OAK_UNSTRUCTURED);
//		} else {
//			queryExpression= queryExpression.replaceAll(NodeTypeConstants.NT_OAK_UNSTRUCTURED, DMSConstants.PIIE_FOLDER);
			queryExpression= queryExpression.replaceAll(NodeTypeConstants.NT_UNSTRUCTURED, NodeTypeConstants.NT_UNSTRUCTURED);
//		}
		
//		queryExpression= queryExpression.replaceAll("jcr:content", ".");
		System.out.println("Query Expression= "+queryExpression);
		
		resultList = Collections.synchronizedList(resultList);
		if (queryManager != null) {
			javax.jcr.query.Query query = null;
			if (queryType.equalsIgnoreCase(DMSConstants.SQL2_QUERY)) {
				query = queryManager.createQuery(queryExpression,javax.jcr.query.Query.JCR_SQL2);
				query.setLimit(1); // To improve performance.Not to traverse
									// entire repository for equality checks
			} else if (queryType.equalsIgnoreCase(DMSConstants.XPATH_QUERY)) {
				queryExpression=improveFolderQuery(queryExpression);
				query = queryManager.createQuery(queryExpression, javax.jcr.query.Query.XPATH);
			}
			if (query == null) { // no further processing required
				return resultList;
			}
			QueryResult res = query.execute();
			NodeIterator ni = res.getNodes();
			while (ni.hasNext()) {
				Node nTemp = ni.nextNode();
				ConcurrentMap<String, Object> concurrentMap = new ConcurrentHashMap<String, Object>();
				for (int i = 0; i < attributesList.size(); i++) {
					String attributeBaseName = attributesList.get(i);
					String attributeBaseValue = getPropertyValueFromDMS(nTemp,attributeBaseName);
					System.out.println("attributeBaseName :" + attributeBaseName
							+ " attributeBaseValue : " + attributeBaseValue);
					
					concurrentMap.put(attributeBaseName, attributeBaseValue);
				}
				concurrentMap.put(dmsId, nTemp.getName());

				if (!concurrentMap.isEmpty()) {
					if (concurrentMap.get(dmsId) != null)
						resultList.add(concurrentMap);
				}
			}
		}
		return resultList;
	}
	
	private String getPropertyValueFromDMS(Node temp, String attributeBaseName)
			throws RepositoryException {
		if (!temp.hasProperty(attributeBaseName))
			return "";
		Property pFound = temp.getProperty(attributeBaseName);
		if (pFound.getValue().getType() == DMSConstants.DATE_FIELD_VALUE_IN_JACKRABBIT) {
			return pFound.getDate().getTime().toString();
		} else {
			return pFound.getString();
		}
	}
	
	private String improveFolderQuery(String queryExpression1) {
		
		String queryExpression=null;
		int index_Relname=queryExpression1.indexOf("jcr:like(@PFS_RELNAME,'DMSFolder')");
		int index_Foldername=queryExpression1.indexOf("jcr:like(@FOLDER_RELNAME,'DMSFolder')");
		if(index_Relname!=-1 && index_Foldername!=-1){
			queryExpression=queryExpression1.substring(0, index_Relname);
			queryExpression=queryExpression+"(";
			queryExpression=queryExpression+queryExpression1.substring(index_Relname);
			queryExpression1=queryExpression;
			int length="jcr:like(@FOLDER_RELNAME,'DMSFolder')".length();
			index_Foldername=queryExpression1.indexOf("jcr:like(@FOLDER_RELNAME,'DMSFolder')");
			queryExpression=queryExpression1.substring(0, index_Foldername+length);
			queryExpression=queryExpression+")";
			queryExpression=queryExpression+queryExpression1.substring(index_Foldername+length);
		}
		if(queryExpression==null){
			queryExpression=queryExpression1;
		}
		return queryExpression;
	}
}
