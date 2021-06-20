package owlrepo.database;

import java.net.URL;
import java.util.Collection;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResult;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.event.NotifyingRepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;
import org.openrdf.repository.event.base.NotifyingRepositoryWrapper;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.rio.RioRenderer;

import com.complexible.common.openrdf.query.BooleanQueryResult;
import com.complexible.common.openrdf.query.BooleanQueryResultImpl;
import com.complexible.common.protocols.server.Server;
import com.complexible.common.protocols.server.ServerException;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.protocols.snarl.SNARLProtocolConstants;
import com.complexible.stardog.sesame.StardogRepository;


public class RDFRepositoryDatabase {
	private static Server server = null;
	private Repository repo;
	private RepositoryConnection conn;
	private NotifyingRepositoryConnection nconn;
	private NotifyingRepositoryWrapper nrepo ;
	//private String baseURI;
	
    //private OWLOntologyDatabase ontology;
	private  URL repo_url ;//= "http://localhost:8080/openrdf-sesame/tradr";
	private  String username = "admin";
	private  String password = "admin";
	
	private boolean SHARED_MODE = false;
	
	public static Server getServerInstance(){
		try {
				if (server == null){
				server = Stardog
				         .buildServer()
				         .bind(SNARLProtocolConstants.EMBEDDED_ADDRESS)
				         .start();
				}
				if (!server.isRunning())
					server.start();
			
		} catch (ServerException e) {
			e.printStackTrace();
		}
		
		return server;
	}
	public RDFRepositoryDatabase(String name, OWLOntology ontology, String baseURI, URL url, RepositoryConnectionListener listener) throws KRDatabaseException {
		this.repo_url = url;

		try{
	/*		//AllegroGraph server and repo setup
			AGServer server = new AGServer(repo_url, username, password);
		    System.out.println("Available catalogs: " + server.listCatalogs());	
	        AGCatalog catalog = server.getRootCatalog();
		     repo = catalog.createRepository("tradr");
		    System.out.println("Got repository "+ "tradr");
	    //    AGValueFactory vf = conn.getRepository().getValueFactory();
	*/


			 // Create a Sesame Repository from a Stardog ConnectionConfiguration. 
			//The configuration will be used  when creating new RepositoryConnections
			//StarDog repo
		if (repo_url == null || !name.equals("BELIEFBASE")){
			//start local server
			
			
			server = getServerInstance();
			
			 // first create a temporary database to use (if there is one already, drop it first)
			 AdminConnection aAdminConnection = AdminConnectionConfiguration.toEmbeddedServer().credentials(username, password).connect();
			 if (aAdminConnection.list().contains(name)) {
				 aAdminConnection.drop(name);
			 }
			 //create repo databse with given name in memory
			 aAdminConnection.createMemory(name);
			 aAdminConnection.close();
			 
			 //create Stardog Sesame Repository with SL reasoning
			 repo = new StardogRepository(ConnectionConfiguration
	                                         .to(name)
	                                         .reasoning(true)
	                                         .credentials(username, password));
			 
		} else{
			  //create Stardog Sesame Repository with reasoning
				String repoURL = repo_url.toString();
				String dbname = repo_url.getPath();
				repoURL = repoURL.substring(0, repoURL.indexOf(dbname));
			repo = new StardogRepository(ConnectionConfiguration
					.to(dbname)
					.reasoning(true)
					.server(repoURL)
					);
				System.out.println("Set up shared repo: " + name + " at "
						+ repo_url + " db: " + dbname);
			this.SHARED_MODE = true;
		}
	        
		//start the repo up
		repo.initialize();
		conn = repo.getConnection();
		
		//wrap it in notifying connection
		 nrepo = new NotifyingRepositoryWrapper(repo);
		 if (listener!=null)
			 nrepo.addRepositoryConnectionListener(listener);
		 nconn = nrepo.getConnection();

		//add contents of file (ontology) to local repo
		 if (!SHARED_MODE){		
			 StatementCollector stc = new StatementCollector();
			 RioRenderer render = new RioRenderer(ontology, stc, ontology.getOWLOntologyManager().getOntologyFormat(ontology),  (Resource) repo.getValueFactory().createURI(baseURI));
			 render.render();
			 nconn.begin();
			 nconn.add(stc.getStatements()); 
			 nconn.commit();
		 }
		}catch(Exception e){
			e.printStackTrace();
			throw new KRDatabaseException(e.getMessage());
			// shutdown();
		}

	}
	
	public boolean isOpen(){
		try {
			if (conn != null)
			return conn.isOpen();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public RepositoryResult<Statement> getTriples(Resource... context) throws RepositoryException{
		return nconn.getStatements(null, null, null, false, context);
	}
	
	public RepositoryResult<Statement> getTriples(Resource subj, URI pred, Value obj, Resource... context) throws RepositoryException{
		return nconn.getStatements(subj, pred, obj, false, context);
	}
	
	public ValueFactory getValueFactory(){
		return nrepo.getValueFactory();
	}
	
	public QueryResult query(String queryString) throws KRQueryFailedException {
		if (queryString.contains("ASK")) {
			return queryBoolean(queryString);
		} else if (queryString.contains("SELECT")) {
			return queryTuple(queryString);
		} else {
			throw new KRQueryFailedException("Did not recognize SPARQL query: "
					+ queryString);
		}
	}

	public TupleQueryResult queryTuple(String queryString) throws KRQueryFailedException {
		 TupleQueryResult result = null;
		 try { 
			// System.out.println("QUERYING Tuple Query...");
		  TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
           result = tupleQuery.evaluate();
         // System.out.println("RESULT: "+result.hasNext());
          
          }catch(Exception e){
        	  throw new KRQueryFailedException(e.getMessage());
          }
		 return result;
	}
	
	public BooleanQueryResult queryBoolean(String queryString) throws KRQueryFailedException {
		boolean result = false;
		try {
			// System.out.println("QUERYING Boolean Query...");
			BooleanQuery booleanQuery = conn.prepareBooleanQuery(
					QueryLanguage.SPARQL, queryString);
			result = booleanQuery.evaluate();
		} catch (Exception e) {
      	   throw new KRQueryFailedException(e.getMessage());
		}
		return new BooleanQueryResultImpl(result);
	}

	public void insert(Collection<Statement> stms, Resource resource){
		try {
			//nconn.begin();
			if (!conn.isOpen())
				conn.begin();
			conn.add(stms, resource);
			conn.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public void delete(Collection<Statement> stms, Resource resource){
		
		try {
			//nconn.begin();
			if (!conn.isOpen())
				conn.begin();
			conn.remove(stms, resource);
			conn.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public boolean ask(String queryString){
	     boolean truth= false;
	     try {
	    	 if (!conn.isOpen())
					conn.begin();
			 BooleanQuery booleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
			 truth = booleanQuery.evaluate(); 
		} catch (Exception e) {
			e.printStackTrace();
		} 
	     return truth;
	}
	
	public GraphQueryResult construct(String queryString){
		 GraphQuery describeQuery = null;
		try {
			if (!conn.isOpen())
				conn.begin();
			describeQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		    GraphQueryResult gresult = describeQuery.evaluate(); 
		    return gresult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void update(String updateString){
		Update update;
		try {
			System.out.println("QUERYING Update...");
			if (!conn.isOpen())
				conn.begin();
			update = conn.prepareUpdate(QueryLanguage.SPARQL, updateString);
			update.execute();
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}
	
	public void shutdown(){
		try {
		//	conn.close();
			conn.close();
		//	repo.shutDown();
			repo.shutDown();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
}
