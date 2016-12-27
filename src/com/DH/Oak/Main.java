package com.DH.Oak;


import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.AuthInfo;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.index.IndexConstants;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexEditorProvider;
import org.apache.jackrabbit.oak.plugins.nodetype.NodeTypeConstants;
import org.apache.jackrabbit.oak.spi.security.authentication.AuthInfoImpl;
import org.apache.jackrabbit.oak.spi.security.principal.SystemPrincipal;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class Main {
	private JFrame mainframe;
	private static String keyword = "";
	private static String IsDeleted = "";
	private static String PFS_RELNAME = "";
	private static String fileContent = "";
	private static Map<String, Object> propertyValue = new HashMap<>();
	private static Map<String, Object> Attributes = new HashMap<>();
	private Repository repo = null;
	private Session session = null;
	private Scanner scan = null;
	private VersionManager vm = null;
	
	public Main() throws LoginException, FileNotFoundException, RepositoryException, IOException {
		init();
	}
	
	public static void main(String[] args) throws LoginException, FileNotFoundException, RepositoryException, IOException {
		new Main();
	}
	
@SuppressWarnings("deprecation")
private void init() throws LoginException, RepositoryException, FileNotFoundException, IOException {
	
	List<String> attributeList = new ArrayList<String>();
	attributeList.add("keyword");
	attributeList.add("FileName");
	attributeList.add("IsDeleted");
	attributeList.add("PFS_RELNAME");
	
	try {
		 repo = createMongoConnection();
		if(repo !=null){
			System.out.println("Connected");
		}
	} catch (UnknownHostException e) {
		e.printStackTrace();
	}
	
	 session = getSession(repo);
	  vm = session.getWorkspace().getVersionManager();
	 try{
		 scan = new Scanner(System.in);
			System.out.println("Press 1 for Node Creation");
			System.out.println("Press 2 for Query for Node");
			System.out.println("Press 3 for Download File");
			System.out.println("Press 4 for Update");
			System.out.println("Press 5 for Version Checking");
			System.out.println("Press 6 for Downloading the version");
			int in = scan.nextInt();
			//switch-case begins from here
			
	 switch (in) {
	 
	 
	case 1:
		String DMSID = DMSIDGenerator.createDMSUniqueID();
		System.out.println("DMS ID= "+DMSID);
		String fpath = "",fname = "";
		JFileChooser chooser = new JFileChooser();
		mainframe = new JFrame("Please Choose the File");
		mainframe.show();
		int ret_val = chooser.showOpenDialog(mainframe);
		if(ret_val == JFileChooser.APPROVE_OPTION){
			File f = chooser.getSelectedFile();
			 fpath = f.getAbsolutePath();
			 fname = f.getName();
			System.out.println("path= "+fpath+"\nname= "+fname);
		}
		else if(ret_val == JFileChooser.CANCEL_OPTION || ret_val == JFileChooser.ABORT){
			System.err.println("No File Selected!!!");
			break;
		}
		
		fileContent = fpath;
		System.out.println("Please provide the keywords:-");
		 keyword = scan.next();
		 System.out.println(keyword);
		
		 	IsDeleted = "N";
		 	PFS_RELNAME = "DMSContent";
		
		 	propertyValue.put("SAMLToken", "NON-SAML");
			propertyValue.put("UPDATE_EXISTING", false);
			propertyValue.put("DMSID", DMSID);
			
			Attributes.put("keyword", keyword);
			Attributes.put("DMSID", DMSID);
			Attributes.put("FileName", fname);
			Attributes.put("IsDeleted", IsDeleted);
			Attributes.put("PFS_RELNAME", PFS_RELNAME);
			
			propertyValue.put("ATTRIBUTES", Attributes);
			propertyValue.put("exception", null);
			propertyValue.put("result", null);
			propertyValue.put("actionType", "DMS_UPLOAD");
			propertyValue.put("fileContent", fileContent);
			
		NodeCreation(propertyValue, vm, session);
		break;
		
		
	case 2:
		System.out.println("Press 1 for query with dmsid");
		System.out.println("Press 2 for query with Version Id");
		System.out.println("press 3 to search with only query");
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		resultList = Collections.synchronizedList(resultList);
		QueryManager Qm = session.getWorkspace().getQueryManager();
		com.DH.Oak.Query q = new com.DH.Oak.Query(Qm, session);
		int _v = scan.nextInt();
		if(_v == 1){
			System.out.println("Enter the dmsId here");
			String id = scan.next();
			resultList = q.searchUsingDMSID(id,attributeList);
			System.out.println("Result List = "+resultList);
		}
		else if(_v == 2){
			System.out.println("Enter the Version Id here");
			String v_id = scan.next();
			System.out.println("Enter the DMS ID:-");
			String dms_id = scan.next();
		}
		else if(_v==3){
			System.out.println("Enter the query here");
			String query = scan.next();
			String dmsID = scan.next();
			resultList = q.searchUsingXPath(query, dmsID, attributeList);
			System.out.println(resultList);
		}
		break;
	
		
	case 3:
		System.out.println("Enter the dmsId of the file to be downloaded");
		String id = scan.next();
		InputStream down_in = null;
		FileOutputStream fout = null;
		try{
		Download download = new Download(session);
		
		down_in = download.getDMSFile(id);
		
		int read = 0;
		StringBuffer s_buf = new StringBuffer();
		while((read = down_in.read())!=-1){
			System.out.println(read);
			char c = (char) read;
			System.out.println(c);
			s_buf.append(c);
		}
		System.out.println(s_buf.toString());
		fout = new FileOutputStream(new File("F:"+File.separator+"abc.txt"));
		fout.write(s_buf.toString().getBytes());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			down_in.close();
			fout.close();
		}
		break;
		
		
	case 4:
		Update update = new Update(session, vm);
		System.out.println("Press 1 for Property Update");
		System.out.println("Press 2 for Content Update");
		int inp = scan.nextInt();
		System.out.println("Provide the DMS ID:-\n");
		String dms_id = scan.next();
		if(inp == 1){
			Map<String, Object> pValue = new HashMap<>();
			
			System.out.println("Provide the Keyword to be updated");
			String keyword = scan.next();
			pValue.put("keyword", keyword);
			String new_version = update.updateMetaData(dms_id, pValue);
			System.out.println("new Verrsion= "+new_version);
		}
		else if(inp == 2){
			JFileChooser ch = new JFileChooser();
			mainframe.setSize(new Dimension(50, 50));
			mainframe.show();
			int vl = ch.showOpenDialog(mainframe);
			File file = null;
			if(vl == JFileChooser.APPROVE_OPTION){
				file = ch.getSelectedFile();
			}
			InputStream input = new FileInputStream(file);
			update.updateMetaData(dms_id, input);
		}
		else{
			System.err.println("INVALID INPUT!!!");
		}
		break;
		
		
	case 5:
		System.out.println("Version Checking");
		System.out.println("Please Provide the DMS ID:-\n");
		String Did = scan.next();
		System.out.println("Please Provide the Version ID:-");
		String ver_id = scan.next();
		DMS_Version version = new DMS_Version(session, vm);
		List<String> versions = version.getAllVersions(Did);
		boolean sta = true;
		Iterator<String> itr = versions.iterator();
		while(itr.hasNext()){
			String temp = itr.next();
			if(temp.equalsIgnoreCase(ver_id)){
				System.out.println("FOUND!!!");
				sta = false;
				break;
			}
		}
		if(sta){
			System.out.println("NOT FOUND!!!");
		}
		break;
		
		
	case 6:
		System.out.println("Downloading Version");
		System.out.println("Please Provide the DMSID:-");
		String D = scan.next();
		DMS_Version _version = new DMS_Version(session, vm);
		List<String> _versions = _version.getAllVersions(D);
		System.out.println("All the Version for the DMs ID "+D);
		for(int i=0;i<_versions.size();i++){
			System.out.println(_versions.get(i));
		}
		break;
		
		
	default:
		System.out.println("Invalid Input");
		break;
		
		
	}
	 }catch(Exception e){
		 e.printStackTrace();
	 }
	 finally {
		scan.close();
		System.exit(0);
	}
	 
}
//Node Creation
private static void NodeCreation(Map<String, Object> propertyValue,VersionManager vm,Session session) throws RepositoryException, FileNotFoundException{
	String dmsId = "-1";
	Map attributes = (Map) propertyValue.get("ATTRIBUTES");
	 attributes.put(DMSConstants.DMS_RELATION_SHIP_NAME,DMSConstants.DMS_CONTENT);
	 Node RootNode = CreateNode.getActualNode(attributes, session);
	 System.out.println(">>"+RootNode);
	 Map<String, Object> nodeMap = CreateNode.getDMSIDbyAddingNodeProperties(attributes, session,vm,false);
	 
	 if (!nodeMap.isEmpty()&& nodeMap.containsKey(DMSConstants.DMS_NODE)&& nodeMap.containsKey(DMSConstants.DMS_ID)) {
		 Node dmsNode = (Node) nodeMap.get(DMSConstants.DMS_NODE);
			Node fileNode = dmsNode.addNode(DMSConstants.JCR_CONTENT, DMSConstants.NT_RESOURCE);
			fileNode.setProperty(DMSConstants.JCR_ENCODING, "UTF-8");
			String f_path = (String) propertyValue.get("fileContent");
			System.out.println("path= "+f_path);
			InputStream in = new FileInputStream(new File(f_path));
			Binary bina = session.getValueFactory().createBinary(in);
			fileNode.setProperty(DMSConstants.JCR_DATA, bina);
			
			String mimeType = "text/plain";
			fileNode.setProperty(DMSConstants.JCR_MIME_TYPE, mimeType);
			session.save();
			dmsId = (String) nodeMap.get(DMSConstants.DMS_ID);
			System.out.println("dmsId= "+dmsId);
			Version ver = vm.checkin(dmsNode.getPath());
			System.out.println("version= "+ver.getIdentifier());
	 }
}

//generate random Dms Id
private static String generate_RandomId(){
	UUID uid = UUID.randomUUID();
	StringBuffer sbuf = new StringBuffer();
	sbuf.append(uid.toString());
	return sbuf.toString();
}

//get Session from Repository
public static Session getSession(Repository repo) throws FileNotFoundException, IOException,RepositoryException {
Session session=null;

javax.security.auth.login.LoginException e = null;
// for checking authentication purposes
for(int i = 0; i < 9; i++) {
try{
	
	session=login("oakadmin",repo);			
	
}catch(javax.security.auth.login.LoginException ex){
	e = ex;
}

if(session != null) break;

try {
	Thread.sleep(1000);
} catch (InterruptedException e1) {
}
}

if(session == null && e != null) {
	System.out.println("Exception");
throw new RepositoryException("Repository Start Exception", e);
}

return session;
}

//login method based on the parameters passed
public static Session login(String userID,Repository repo) throws LoginException {
	
	Set<Principal> principals =new HashSet<Principal>();
	SystemPrincipal principal=SystemPrincipal.INSTANCE;
	principals.add(principal);
	
	AuthInfo authInfo = new AuthInfoImpl(userID, Collections.<String, Object>emptyMap(), principals);
	Subject subject = new Subject(true, principals, Collections.singleton(authInfo), Collections.<Object>emptySet());
	Session session;
	try {					
		session =Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<Session>(){
		@Override
		public Session run() throws Exception {
		
				return repo.login(null,null);		
			}

		},null);
	
	} catch (PrivilegedActionException e) {
	    throw new LoginException(e.toString());
	}
			
	return session;
	
}

//create the mongoDB connection
public static Repository createMongoConnection() throws UnknownHostException{
	DB db = new MongoClient("192.168.4.13", 27017).getDB("dmslocal");
	
    DocumentNodeStore ns = new DocumentMK.Builder().setMongoDB(db).getNodeStore();
    
    Repository repo = new Jcr(new Oak(ns)).createRepository();
    return repo;
}

//create the property Index based on the session
private static void createPropertyIndex (Session session) throws PathNotFoundException, RepositoryException {
	
	Node oakIndexNode = session.getNode("/oak:index");
//	String[] enclosingNodeTypes = {DMSConstants.PIIE_FILE, DMSConstants.PIIE_FOLDER};
	String[] enclosingNodeTypes = {NodeTypeConstants.NT_UNSTRUCTURED};
	if(!oakIndexNode.hasNode("DMSIDIndex")) {  //Index node not exists.So create the new one
		String[] indexdmsidProp = {"DMSID"};
		propertyIndexDefinition(session, "DMSIDIndex", indexdmsidProp, true, enclosingNodeTypes);
	}
	
	if(!oakIndexNode.hasNode("PFSRELNAMEIndex")) {
		 String[] indexpfsrealnameProp = {"PFS_RELNAME","IsDeleted","Keyword","AbsolutePath","FOLDER_RELNAME"};
		 propertyIndexDefinition(session, "PFSRELNAMEIndex", indexpfsrealnameProp, false, enclosingNodeTypes);
	}
	System.out.println("Property Index Created Sucessfully");
}

//
public static Node propertyIndexDefinition(Session session, String indexDefinitionName,
        String[] propertyNames, boolean unique,
        String[] enclosingNodeTypes) throws RepositoryException {
    
    Node root = session.getRootNode();
    Node indexDefRoot = JcrUtils.getOrAddNode(root, IndexConstants.INDEX_DEFINITIONS_NAME, NodeTypeConstants.NT_UNSTRUCTURED);
    Node indexDef = JcrUtils.getOrAddNode(indexDefRoot, indexDefinitionName, IndexConstants.INDEX_DEFINITIONS_NODE_TYPE);
    indexDef.setProperty(IndexConstants.TYPE_PROPERTY_NAME, PropertyIndexEditorProvider.TYPE);
    indexDef.setProperty(IndexConstants.REINDEX_PROPERTY_NAME, true);
    indexDef.setProperty(IndexConstants.PROPERTY_NAMES, propertyNames, PropertyType.NAME);
    indexDef.setProperty(IndexConstants.UNIQUE_PROPERTY_NAME, unique);

    if (enclosingNodeTypes != null && enclosingNodeTypes.length != 0) {
        indexDef.setProperty(IndexConstants.DECLARING_NODE_TYPES, enclosingNodeTypes, PropertyType.NAME);
    }
    session.save();
    
    return indexDef;
}
}
