package org.gcube.contentmanager.storageserver.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.DB;
import com.mongodb.DBCollection;
//import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
//import com.mongodb.MongoOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class MongoDB {
	
	private MongoClient mongo;
	private DB db;
	private String[] server;
	private String collectionSSName;
	private String collectionFSName;
	private int port;
	private String pwd;
	private String user;
	private DBCollection ssCollection;
	private DBCollection fsCollection;
	Logger logger = LoggerFactory.getLogger(MongoDB.class);
	private static final String ACCOUNTING_DB="accounting";
	private static final String DEFAULT_SS_COLLECTION="storageStatus";
	private static final String DEFAULT_FS_COLLECTION="folderStatus";
	private FolderStatusOperationManager folderOperationManager;
	private StorageStatusOperationManager ssOperationManager;
	protected static ReadPreference READ_PREFERENCE=ReadPreference.secondaryPreferred();
	protected static final  WriteConcern WRITE_TYPE=WriteConcern.REPLICAS_SAFE;
	
	public MongoDB(String[] server, int port,  String user, String password){
		logger.debug("MongoDB init with "+user+" "+password);
		this.server=server;
		this.port=port;
		this.pwd=password;
		this.user=user;
		this.collectionSSName=DEFAULT_SS_COLLECTION;
		this.collectionFSName=DEFAULT_FS_COLLECTION;
		folderOperationManager=new FolderStatusOperationManager(getFolderStatusCollection());
		ssOperationManager=new StorageStatusOperationManager(getStorageStatusCollection());
	}
	
	public MongoDB(String[] server, String user, String password){
		logger.debug("MongoDB init with "+user+" "+password);
		this.server=server;
		this.pwd=password;
		this.user=user;
		this.collectionSSName=DEFAULT_SS_COLLECTION;
		this.collectionFSName=DEFAULT_FS_COLLECTION;
		folderOperationManager=new FolderStatusOperationManager(getFolderStatusCollection());
		ssOperationManager=new StorageStatusOperationManager(getStorageStatusCollection());

	}
	
	public MongoDB(String[] server, String user, String password, String ssCollection, String fsCollection){
		this.server=server;
		this.pwd=password;
		this.user=user;
		if(ssCollection!=null)
			this.collectionSSName=ssCollection;
		else
			this.collectionSSName=DEFAULT_SS_COLLECTION;
		if(fsCollection!=null)
			this.collectionFSName=fsCollection;
		else
			this.collectionFSName=DEFAULT_FS_COLLECTION;
		folderOperationManager=new FolderStatusOperationManager(getFolderStatusCollection());
		ssOperationManager=new StorageStatusOperationManager(getStorageStatusCollection());

	}
	

	public StorageStatusObject updateUserVolume(StorageStatusObject ssRecord, String  operation){
		logger.debug("updating user volume");
		ssRecord= ssOperationManager.updateUser(ssRecord, operation);
		close();
		return ssRecord;
	}
	
	public StorageStatusObject overwriteUser(String consumer, String count, String volume){
		StorageStatusObject ssRecord= ssOperationManager.overwriteUser(consumer,  count, volume);
		close();
		return ssRecord;
	}
	
	public FolderStatusObject updateFolderVolume(FolderStatusObject fsRecord, String operation){
		logger.debug("update folder recursively");
		fsRecord= folderOperationManager.updateFolder(fsRecord, operation);
		logger.debug("end update folder edge ");
		close();
		return fsRecord;
	}

	public StorageStatusObject getSSRecord(String consumer){
		StorageStatusObject record=ssOperationManager.getSSRecord(consumer);
//		close();
		return record;
	}
	
	public FolderStatusObject getFSRecord(String folder){
		FolderStatusObject record=folderOperationManager.getFSRecord(folder);
		close();
		return record;
	}
	
	
	protected DB getDB(){
		if(db==null){
			try{
			int i=-1;
			List<ServerAddress> srvList=new ArrayList<ServerAddress>();
			for(String srv : server){
				srvList.add(new ServerAddress(srv));
			}
			if(mongo==null){		
					logger.debug(" open mongo connection ");
					MongoClientOptions options=MongoClientOptions.builder().sslEnabled(true).connectionsPerHost(10).connectTimeout(30000).readPreference(READ_PREFERENCE).build();
//					MongoClientOptions options=MongoClientOptions.builder().connectionsPerHost(10).connectTimeout(30000).readPreference(READ_PREFERENCE).build();
					if(((pwd != null) && (pwd.length() >0))  && ((user != null) && (user.length() > 0))){
						MongoCredential credential = MongoCredential.createMongoCRCredential(user, ACCOUNTING_DB, pwd.toCharArray());
						mongo = new MongoClient(srvList, Arrays.asList(credential), options);
					}else{
						mongo = new MongoClient(srvList, options);
					}
					logger.debug("Istantiate MongoDB with options: "+mongo.getMongoClientOptions());
			}
			db = mongo.getDB(ACCOUNTING_DB);
			db.setWriteConcern(WRITE_TYPE);
			} catch (Exception e) {
				close();
				logger.error("Problem to open the DB connection for gridfs file ");
				e.printStackTrace();
			}
			logger.info("new mongo connection pool opened");
		}
		return db;
	}


	public DBCollection getStorageStatusCollection() {
		if(ssCollection==null)
			return getDB().getCollection(collectionSSName);
		else
			return ssCollection;
	}
	
	public DBCollection getFolderStatusCollection() {
		if(fsCollection==null)
			return getDB().getCollection(collectionFSName);
		else
			return fsCollection;
	}
	

	public void close(){
//		if(mongo!=null)
//			mongo.close();
	}


}
