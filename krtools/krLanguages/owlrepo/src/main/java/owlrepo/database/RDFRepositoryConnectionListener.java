package owlrepo.database;

import java.util.ArrayList;
import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;

public class RDFRepositoryConnectionListener implements RepositoryConnectionListener{
	private String name="listener";
	private OWLOntologyDatabase db;

	public  RDFRepositoryConnectionListener(OWLOntologyDatabase db, String name) {
		this.db = db;
		this.name += name;
	}
	
	@Override
	public void close(RepositoryConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAutoCommit(RepositoryConnection conn, boolean autoCommit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void begin(RepositoryConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commit(RepositoryConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollback(RepositoryConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(RepositoryConnection conn, Resource subject, URI predicate,
			Value object, Resource... contexts) {
		//System.out.println("adding "+subject+predicate+object);	
		Statement s = ValueFactoryImpl.getInstance().createStatement(subject, predicate, object);
		Collection<Statement> sts = new ArrayList<Statement>(); 
		sts.add(s);
		//System.out.println("Insert "+s);
		//db.insert(sts);
	}

	@Override
	public void remove(RepositoryConnection conn, Resource subject,
			URI predicate, Value object, Resource... contexts) {
		System.out.println("removing "+subject+predicate+object);		
	}

	@Override
	public void clear(RepositoryConnection conn, Resource... contexts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNamespace(RepositoryConnection conn, String prefix,
			String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNamespace(RepositoryConnection conn, String prefix) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearNamespaces(RepositoryConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(RepositoryConnection conn, QueryLanguage ql,
			String update, String baseURI, Update operation) {
		System.out.println("executing update "+operation);		
	}

}
