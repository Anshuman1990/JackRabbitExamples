package com.DH.Oak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;

import org.apache.jackrabbit.oak.plugins.nodetype.NodeTypeConstants;


public class Download {
	private QueryManager queryManager = null;
	private Session session = null;
	public Download(Session s) {
		this.session = s;
	}
	
	
	public InputStream getDMSFile(String dmsId) throws RepositoryException,
	FileNotFoundException, IOException {
System.out.println("DmsId= "+dmsId);
if (dmsId == null) {
	System.out.println("DmsId is null");
	return null;
}
final long startTime = System.currentTimeMillis();
InputStream is = null;
Property streamProperty = null;
try {
	queryManager = session.getWorkspace().getQueryManager();
	if (queryManager != null) {
		String expression = "select * from ["+NodeTypeConstants.NT_UNSTRUCTURED+"] as b where b.["
				+ DMSConstants.DMS_ID + "]='" + dmsId + "'";
		System.out.println("Query expression:" + expression);
		javax.jcr.query.Query query = queryManager.createQuery(
				expression, javax.jcr.query.Query.JCR_SQL2);
		query.setLimit(1); // To improve performance.Not to traverse
							// entire repository for equality checks
		javax.jcr.query.QueryResult result = query.execute();
		NodeIterator nodeIterator = result.getNodes();
		session.save();
		while (nodeIterator.hasNext()) {
			Node node = nodeIterator.nextNode();
			if(node.hasNode(DMSConstants.JCR_CONTENT))
			{
				Node fileContent = node.getNode(DMSConstants.JCR_CONTENT);
				streamProperty = fileContent.getProperty(DMSConstants.JCR_DATA);
				is = streamProperty.getBinary().getStream();
				break;
			}
/*					PropertyIterator pitr = node.getProperties();
			while (pitr.hasNext()) {
				Property property = pitr.nextProperty();
				if (property.getName().equals(DMSConstants.DMS_ID)) {
					logger.debug("File content node matched..Returning file conent..");
					if (node.hasNode(DMSConstants.JCR_CONTENT)) {
						Node fileContent = node.getNode(DMSConstants.JCR_CONTENT);
						streamProperty = fileContent.getProperty(DMSConstants.JCR_DATA);
						is = streamProperty.getBinary().getStream();
						break;
					}
				}
			}*/

		}
		final long endTime = System.currentTimeMillis();
		System.out.println("Time(in ms) taken  to download a file for DMS ID ("
				+ dmsId + ") :" + (endTime - startTime));
	}
} finally {
	session.logout();

}

return is;
}
}
