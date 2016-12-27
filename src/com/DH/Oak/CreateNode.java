package com.DH.Oak;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;

import org.apache.jackrabbit.oak.plugins.nodetype.NodeTypeConstants;

public class CreateNode {
	private static Map<String, Object>PropertyValue = null;
	private static Repository repo = null;
	private static Session session = null;

	//Create a node based on the Node Properties
	public static Map<String, Object> getDMSIDbyAddingNodeProperties(
			Map<String, Object> propertyValue, Session session,VersionManager vm, boolean checkoutReqd)
			throws ItemExistsException, PathNotFoundException,
			VersionException, ConstraintViolationException, LockException,
			RepositoryException {
		Map<String, Object> returnNodeMap = new ConcurrentHashMap<String, Object>();
		String strNodeName = (String) propertyValue.get(DMSConstants.DMS_ID);// DMSIDGenerator.INSTANCE.createDMSUniqueID();

		Node rootNode = getActualNode(propertyValue, session);
		

		String fileName = ensureFileName(propertyValue);
		
		if(checkoutReqd){			
			vm.checkout(rootNode.getPath());
		}		
		Node dmsNode = rootNode.addNode(strNodeName, NodeTypeConstants.NT_UNSTRUCTURED);
		if (dmsNode != null) {


			for (Map.Entry<String, Object> entry : propertyValue.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (key != null && value != null) {
					dmsNode.setProperty(key, value.toString());
				}
			}

			/* setting the strNodeName as DMS_ID for query purpose */
			dmsNode.setProperty(DMSConstants.DMS_ID, strNodeName);
			dmsNode.addMixin(DMSConstants.MIX_VERSIONABLE);
			dmsNode.addMixin(DMSConstants.MIX_LOCKABLE);
			dmsNode.setProperty(DMSConstants.JCR_LAST_MODIFIED_BY, "admin");
			dmsNode.setProperty(DMSConstants.JCR_CREATED_BY,Calendar.getInstance());
			dmsNode.setProperty("PFS_RELNAME", "DMSContent");
			dmsNode.setProperty(DMSConstants.DMS_DELETE_ATTRIBUTE, "N");

			returnNodeMap.put(DMSConstants.DMS_NODE, dmsNode);
			returnNodeMap.put(DMSConstants.DMS_ID, strNodeName);
		}

		return returnNodeMap;
	}

	//Create the actual Node based on the property and session
	public static Node getActualNode(Map<String, Object> propertyValue,Session session) throws RepositoryException {
		Node rootNode = session.getRootNode();
		QueryManager queryManager = session.getWorkspace().getQueryManager();

		// change root node based on folder attribute
		Object currentFolderObj = propertyValue.get(DMSConstants.DMS_CONTENT_FOLDER_ATTRIBUTE);
		if (currentFolderObj != null) {
			String currentFolder = (String) currentFolderObj;
			if (currentFolder.trim().length() > 0) {
				rootNode = getJCRNodePath(session,
						(String) currentFolder, false, queryManager);
				if (rootNode == null) {
					throw new PathNotFoundException("Path '" + currentFolder
							+ "' is not found in repository");
				}
			}
		}
		return rootNode;
	}

	//extracts the JCRPath from the Node
	public static Node getJCRNodePath(Session session, String nodePath,
			boolean createSubFolder, QueryManager queryManager)
			throws RepositoryException {

		//return JcrUtils.getOrCreateByPath(DMSConstants.PIIE_ROOT + DMSConstants.BACK_SLASH + nodePath, NodeTypeConstants.NT_FOLDER, session);
		
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		//nodePath = nodePath.toLowerCase();
		String completePath = nodePath;
		StringTokenizer sTokenizer = new StringTokenizer(nodePath,DMSConstants.BACK_SLASH);
		
		// TODO need to add is deleted check also for the below query
		StringBuilder queryBuilder = new StringBuilder(
				"select * from [" + NodeTypeConstants.NT_UNSTRUCTURED + "] as b where ((b.["
						+ DMSConstants.DMS_RELATION_SHIP_NAME + "]='"
						+ DMSConstants.DMS_FOLDER + "')");		
		queryBuilder.append(" or (b.["
						+ DMSConstants.DMS_FOLDER_REL_NAME + "]='"
						+ DMSConstants.DMS_FOLDER + "'))");	
		queryBuilder.append(" and b.[" + DMSConstants.DMS_DELETE_ATTRIBUTE
				+ "] = 'N' and  (b.[" + DMSConstants.DMS_ABSOLUTE_PATH
				+ "]='" + nodePath + "'");
					
		if (createSubFolder) {
			while (sTokenizer.hasMoreElements()) {
				sTokenizer.nextElement();
				if (nodePath.lastIndexOf(DMSConstants.BACK_SLASH) > 0) {
					nodePath = nodePath.substring(0,
							nodePath.lastIndexOf(DMSConstants.BACK_SLASH));					
					queryBuilder.append(" or b.["
							+ DMSConstants.DMS_ABSOLUTE_PATH + "] ='"
							+ nodePath+ "'");					
				}
			}
		}
		queryBuilder.append(")");
		String queryTxt = queryBuilder.toString();
		queryTxt = queryTxt.replaceAll("//", "/");
		Query query = queryManager.createQuery(queryTxt,
				Query.JCR_SQL2);
		QueryResult result = query.execute();
		NodeIterator nodeIterator = result.getNodes();

		while (nodeIterator.hasNext()) {
			Node tmpNode = nodeIterator.nextNode();
			String strValue = getPropertyValue(tmpNode,
					DMSConstants.DMS_ABSOLUTE_PATH);
			String dmsRelationName = getPropertyValue(tmpNode,
					DMSConstants.DMS_RELATION_SHIP_NAME);
			String dmsFolderName = getPropertyValue(tmpNode,
					DMSConstants.DMS_FOLDER_REL_NAME);
			// Don't add the Node Under a file Node.
			if (strValue != null && dmsRelationName != null
					&& dmsRelationName.equals(DMSConstants.DMS_FOLDER)) {
				//nodeMap.put(strValue.toLowerCase(), tmpNode);
				nodeMap.put(strValue, tmpNode);
			}else if(strValue != null && dmsFolderName != null && dmsFolderName.equals(DMSConstants.DMS_FOLDER)){
				nodeMap.put(strValue, tmpNode);
			}
		}
		Node existingNode = getNode(completePath, nodeMap);
		
		return existingNode;
	}
	
	//extracts the property value based on the node and the attribute name
	public static String getPropertyValue(Node node, String attributeName)
			throws PathNotFoundException, RepositoryException {

		Property pFound;
		String strValue = null;
		if (node == null || !node.hasProperty(attributeName))
			return null;

		pFound = node.getProperty(attributeName);
		strValue = pFound.getString();

		return strValue;
	}
	
	//gets the Node
	public static Node getNode(String nodeName, Map<String, Node> nodeMap)
			throws RepositoryException {

		Node node = null;
		String strAbsPath = "";
		if (nodeMap.get(nodeName) != null) {
			node = (Node) nodeMap.get(nodeName);
		} else {
			StringTokenizer sTokenizer = new StringTokenizer(nodeName,
					DMSConstants.BACK_SLASH);

			while (sTokenizer.hasMoreElements()) {
				String strFolderName = sTokenizer.nextElement().toString();
				strAbsPath = strAbsPath + strFolderName;
				if (nodeMap.get(strAbsPath) != null) {
					node = (Node) nodeMap.get(strAbsPath);
				}
				strAbsPath = strAbsPath + DMSConstants.BACK_SLASH;
			}
		}
		return node;
	}
	
	//ensures the filename
	public static String ensureFileName(Map<String, Object> propertyValue) {
		Object fileName = propertyValue.get(DMSConstants.FILE_NAME);
		if (fileName == null || fileName.toString().trim().length() == 0) {
			String content = (String) propertyValue.get(DMSConstants.CONTENT);
			if (content.lastIndexOf('\\') >= 0) {
				fileName = content.substring(content.lastIndexOf('\\') + 1);
				propertyValue.put(DMSConstants.FILE_NAME, fileName);
			}else if (content.lastIndexOf('/') >=0) {
				fileName = content.substring(content.lastIndexOf('/') + 1);
				propertyValue.put(DMSConstants.FILE_NAME, fileName);
			}
		}
		return (String) fileName;
	}
	
	
}
