package com.DH.Oak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.apache.jackrabbit.oak.plugins.nodetype.NodeTypeConstants;


public class DMS_Version {
	private Session session = null;
	private VersionManager vm = null;
public DMS_Version(Session S,VersionManager V) {
	this.session = S;
	this.vm = V;
}
	public List<String> getAllVersions(String dmsId)
			throws RepositoryException, FileNotFoundException, IOException {
		
		List<String> versionList = new ArrayList<String>();
		try {
			Node dmsNode = getDMSNode(dmsId, session);

			versionList = Collections.synchronizedList(versionList);
			VersionManager vm = session.getWorkspace().getVersionManager();
			if (vm != null && dmsNode != null) {
				VersionHistory vh = vm.getVersionHistory(dmsNode.getPath());
				for (VersionIterator vi = vh.getAllVersions(); vi.hasNext();) {
					Version version = vi.nextVersion();
					System.out.println("next version is: " + version.getIdentifier());
					versionList.add(version.getIdentifier());
				}
			}

		} finally {
			session.logout();
		}

		return versionList;
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
}
