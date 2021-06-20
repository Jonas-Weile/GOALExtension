package owlrepo;

import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;



public class SetupRepoTest {
	
	public static void main(String[] args){
		RepositoryManager repositoryManager = null;
		try {
			//connect to remote repo url
			 repositoryManager =
					new RemoteRepositoryManager("http://127.0.0.1:8080/openrdf-sesame");
			repositoryManager.initialize();
			
			//get specific repo and its connection
			String repoName = "tradr";
			Repository repo =  repositoryManager.getRepository(repoName);
			RepositoryConnection repoConnection = repo.getConnection();
			System.out.println("Repo:"+repo.toString()+"\nConn:"+repoConnection.toString());

			//start connection
			repoConnection.begin();
			
			//cast to owlim repo
			//Repository myrepo = (Repository) repositoryManager.getRepository(repoName).;
				
			//do stuff with repo
			String queryString = "PREFIX :<http://www.w3.org/2002/07/owl#>"
					+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
					+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
					+ "PREFIX tradr:<http://www.semanticweb.org/timi/ontologies/2014/3/untitled-ontology-10#>"
					+ "select ?x where "
					+ "{?x rdfs:subClassOf tradr:Team}";
			
			TupleQuery query = repoConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = query.evaluate();
			List<String> bindings = result.getBindingNames();
			for (String binding: bindings){
				System.out.println(binding);
			}
			while(result.hasNext()){
				BindingSet bset = result.next();
				for (int i=0; i<bindings.size(); i++)
					System.out.println(bset.getValue(bindings.get(i)));
			}
			
			//close connection and shutdown repo
			repoConnection.close();
			repo.shutDown();
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			repositoryManager.shutDown();
		}
		
	}

}
