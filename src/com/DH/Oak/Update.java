package com.DH.Oak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import org.apache.jackrabbit.oak.plugins.nodetype.NodeTypeConstants;

public class Update {
private static Session session = null;
private static VersionManager vm = null;
	public Update(Session S,VersionManager VM) {
		Update.session = S;
		Update.vm = VM;
	}
	public String updateMetaData(String dmsId, InputStream is)
			throws Exception {
		String newVersion = null;
		final long startTime = System.currentTimeMillis();
		Node dmsNode = getDMSNode(dmsId, session);
		if (session != null && dmsNode != null) {
			if (vm != null && !vm.isCheckedOut(dmsNode.getPath())) {
				Version checkPoint = null;
				try {
					vm.checkout(dmsNode.getPath());
					Node fileNode = null;
					// if file node already present then update the same
					if (dmsNode.hasNode(DMSConstants.JCR_CONTENT)) {
						System.out.println("fetching " + DMSConstants.JCR_CONTENT+ " to dmsNode:" + dmsNode);
						fileNode = dmsNode.getNode(DMSConstants.JCR_CONTENT);

					}
					// if file node not present then add the file node
					else if (!dmsNode.hasNode(DMSConstants.JCR_CONTENT)) {
						fileNode = dmsNode.addNode(DMSConstants.JCR_CONTENT,
								DMSConstants.NT_RESOURCE);
					}
					if (fileNode != null) {
						addFileData(session, fileNode, is, null);
						session.save();
					}
					Version version = vm.checkin(dmsNode.getPath());
					newVersion = version.getIdentifier();

				} catch (Exception e) {
					if (checkPoint != null) {
						session.save();
						vm.restore(checkPoint, true);
					}
					throw e;
				} finally {
					closeStream(is);
					session.logout();
				}

				final long endTime = System.currentTimeMillis();
				System.out.println("Time(in ms) taken to update file content Metadata for DMSid ("
						+ dmsId + ") is:" + (endTime - startTime));
			}
			return newVersion;
		} else {
			return null;
		}

	}
	
	private Node getDMSNode(String dmsId, Session session)
			throws RepositoryException {
		// dmsId="1441168081403";
		Node dmsNode = null;
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		if (queryManager != null) {
			String expression = "select * from [" + NodeTypeConstants.NT_UNSTRUCTURED + "] as b where b.["
					+ DMSConstants.DMS_ID + "]='" + dmsId + "'";
			System.out.println("Query expression:" + expression);
			javax.jcr.query.Query query = queryManager.createQuery(expression,
					javax.jcr.query.Query.JCR_SQL2);
			query.setLimit(1); // To improve performance.Not to traverse entire
								// repository for equality checks
			javax.jcr.query.QueryResult result = query.execute();
			NodeIterator nodeIterator = result.getNodes();
			while (nodeIterator.hasNext()) {
				dmsNode = nodeIterator.nextNode();
			}
		}
		return dmsNode;
	}
	
	private void closeStream(InputStream is) throws IOException {
		if (is != null) {
			is.close();
		}
	}
	
	private void addFileData(Session session, Node fileNode, InputStream is,
			String fileName) throws IOException, RepositoryException {

		Binary binary = session.getValueFactory().createBinary(is);
		fileNode.setProperty(DMSConstants.JCR_DATA, binary);
		String mimeType = getMimeTypeByFileName(fileName);
		System.out.println("mimeType of Input stream" + mimeType);
		fileNode.setProperty(DMSConstants.JCR_MIME_TYPE, mimeType);
		System.out.println("Setting Mime type to fileNode" + fileNode);

	}
	
	public String getMimeTypeByFileName(String fileName) {

		String mimetype = "application/octet-stream"; // Apart from the listed
														// files format, for any
														// other JCR will not do
														// the indexing.
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".txt"))
			mimetype = "text/plain";
		else if (fileName.endsWith(".doc"))
			mimetype = "application/msword";
		else if (fileName.endsWith(".docx"))
			mimetype = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		else if (fileName.endsWith(".xls"))
			mimetype = "application/vnd.ms-excel";
		else if (fileName.endsWith(".xlsx"))
			mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		else if (fileName.endsWith(".ppt"))
			mimetype = "application/mspowerpoint";
		else if (fileName.endsWith(".pptx"))
			mimetype = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
		else if (fileName.endsWith(".pdf"))
			mimetype = "application/pdf";
		else if (fileName.endsWith(".rtf"))
			mimetype = "application/rtf";
		else if (fileName.endsWith(".htm"))
			mimetype = "text/html";
		else if (fileName.endsWith(".html"))
			mimetype = "text/html";
		else if (fileName.endsWith(".mht"))
			mimetype = "text/html";
		else if (fileName.endsWith(".cfm"))
			mimetype = "text/html";
		else if (fileName.endsWith(".jsp"))
			mimetype = "text/html";
		else if (fileName.endsWith(".jspx"))
			mimetype = "text/html";
		else if (fileName.endsWith(".xml"))
			mimetype = "text/xml";
		else if (fileName.endsWith(".xsl"))
			mimetype = "text/xml";
		else if (fileName.endsWith(".java"))
			mimetype = "text/plain";
		else if (fileName.endsWith(".bat"))
			mimetype = "text/plain";
		else if (fileName.endsWith(".ods"))
			mimetype = "application/vnd.oasis.opendocument.spreadsheet";
		else if (fileName.endsWith(".odt"))
			mimetype = "application/vnd.oasis.opendocument.text";
		else if (fileName.endsWith(".odf"))
			mimetype = "application/vnd.oasis.opendocument.formula";
		else if (fileName.endsWith(".odg"))
			mimetype = "application/vnd.oasis.opendocument.graphics";
		else if (fileName.endsWith(".odd"))
			mimetype = "application/vnd.oasis.opendocument.database";
		else if (fileName.endsWith(".odp"))
			mimetype = "application/vnd.oasis.opendocument.presentation";
		else if (fileName.endsWith(".jpg"))
			mimetype = "image/jpeg";
		else if (fileName.endsWith(".jpeg"))
			mimetype = "image/jpeg";
		else if (fileName.endsWith(".gif"))
			mimetype = "image/gif";
		else if (fileName.endsWith(".png"))
			mimetype = "image/png";
		// application/octet-stream - Use this, if Indexing is not required.
		return mimetype;

	}
	
	public String updateMetaData(String dmsId, Map<String, Object> propertyValue)
			throws Exception {
		Node dmsNode = getDMSNode(dmsId, session);
		String newVersion = null;
		boolean isFolder = checkForDMSFolder(dmsNode);
		// don't check for version related if it is a folder.
		/*if (isFolder && vm != null && dmsNode != null
				&& !propertyValue.isEmpty()) {
			Version checkPoint = vm.checkpoint(dmsNode.getPath());
			try {
				if(!vm.isCheckedOut(dmsNode.getPath()))
				{
					vm.checkout(dmsNode.getPath());
				}
				for (Map.Entry<String, Object> entry : propertyValue.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (key != null
							&& value != null) {
						dmsNode.setProperty(key, value.toString());
						logger.debug("setting property key to node as  key="
								+ key + " value:" + value.toString());
						if (key.equalsIgnoreCase(DMSConstants.DMS_DELETE_ATTRIBUTE)) {
							deleteAllChildNodes(dmsNode, value.toString(),
									session);
						}
					}
				}
				session.save();
				//return dmsId;
				if(vm.isCheckedOut(dmsNode.getPath()))
				{
					vm.checkin(dmsNode.getPath());
				}

			}catch(Exception re){
				if (checkPoint != null)
				{
					session.save();
					vm.restore(checkPoint, true);
				}
				throw re;
			}
			finally {
				repoManager.sessionLogout(session);
			}
			return dmsId;
		}*/

		if (isFolder && vm != null && dmsNode != null
				&& !propertyValue.isEmpty()) {
			try {
				for (Map.Entry<String, Object> entry : propertyValue.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (key != null
							&& value != null) {
						dmsNode.setProperty(key, value.toString());
						System.out.println("setting property key to node as  key="
								+ key + " value:" + value.toString());
						if (key.equalsIgnoreCase(DMSConstants.DMS_DELETE_ATTRIBUTE)) {
							deleteAllChildNodes(dmsNode, value.toString(),
									session);
						}
					}

					session.save();
					return dmsId;
				}
			} finally {
				session.logout();

			}
		}
		
		if (vm != null && dmsNode != null && !propertyValue.isEmpty()
				&& !vm.isCheckedOut(dmsNode.getPath())) {
			Version checkPoint = vm.checkpoint(dmsNode.getPath());
			try {
				vm.checkout(dmsNode.getPath());
				// loop through all properties and update only existing property
				// of the node.
				for (Map.Entry<String, Object> entry : propertyValue.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (key != null 
							&& value != null) {
						dmsNode.setProperty(key, value.toString());
					}

				}
				session.save();
				Version version = vm.checkin(dmsNode.getPath());
				newVersion = version.getIdentifier();
			} catch (Exception re) {
				if (checkPoint != null)
				{
					session.save();
					vm.restore(checkPoint, true);
				}
				throw re;
			} finally {
				session.logout();
			}

		}

		return newVersion;
	}

	public static boolean checkForDMSFolder(Node node)
			throws RepositoryException {
		boolean isDMSFolder = false;
		String dmsFolder = getPropertyValue(node, DMSConstants.DMS_RELATION_SHIP_NAME);
		if(dmsFolder==null){
			dmsFolder=getPropertyValue(node, DMSConstants.DMS_FOLDER_REL_NAME);
		}					
		if (!dmsFolder.isEmpty()&& dmsFolder.equals(DMSConstants.DMS_FOLDER)) {
				isDMSFolder = true;
		}		
		return isDMSFolder;
	}
	
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
	
	private void deleteAllChildNodes(Node folderNode, String value,
			Session session) throws Exception {

		for (NodeIterator ni = folderNode.getNodes(); ni.hasNext();) {
			Node nextNode = (Node) ni.next();
			if (nextNode != null && value != null) {
				boolean isFolder = checkForDMSFolder(nextNode);
				if (isFolder) {

					if (vm != null && nextNode != null
							&& !vm.isCheckedOut(nextNode.getPath())) {
						session.save();
						Version checkPoint = vm.checkpoint(nextNode.getPath());
						try {
							vm.checkout(nextNode.getPath());
							if (nextNode
									.hasProperty(DMSConstants.DMS_DELETE_ATTRIBUTE)) {
								nextNode.setProperty(DMSConstants.DMS_DELETE_ATTRIBUTE,
										value);
							}
							session.save();
							Version version = vm.checkin(nextNode.getPath());
							String versionId = version.getIdentifier();
							System.out.println("versionId after node update: "
									+ versionId + " for node: "
									+ nextNode.getProperty(DMSConstants.DMS_ID));
						} catch (Exception re) {
							if (checkPoint != null)
								vm.restore(checkPoint, true);
							throw re;
						}
					}
					deleteAllChildNodes(nextNode, value, session);
				} else {

					if (vm != null && nextNode != null
							&& !vm.isCheckedOut(nextNode.getPath())) {
						session.save();
						Version checkPoint = vm.checkpoint(nextNode.getPath());
						try {
							vm.checkout(nextNode.getPath());
							if (nextNode
									.hasProperty(DMSConstants.DMS_DELETE_ATTRIBUTE)) {
								nextNode.setProperty(
										DMSConstants.DMS_DELETE_ATTRIBUTE,
										value.toString());
							}
							session.save();
							Version version = vm.checkin(nextNode.getPath());
							String versionId = version.getIdentifier();
							System.out.println("versionId after node update: "
									+ versionId + " for node: "
									+ nextNode.getProperty(DMSConstants.DMS_ID));
						} catch (Exception re) {
							if (checkPoint != null)
								vm.restore(checkPoint, true);
							throw re;
						}

					}

				}

			}

		}

	}
}
