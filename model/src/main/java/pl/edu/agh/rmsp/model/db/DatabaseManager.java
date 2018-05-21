package pl.edu.agh.rmsp.model.db;

import java.util.Date;
import java.util.LinkedList;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import pl.edu.agh.rmsp.model.commons.Measurement;

public class DatabaseManager {
	
	private MongoDatabase db;
	
	public DatabaseManager() {
		DatabaseConfigurator dbConfig = new DatabaseConfigurator();
		
		//Lets try to connect
		@SuppressWarnings("resource")
		MongoClient mongoClient = new MongoClient(dbConfig.getHost(), dbConfig.getPort());
		db = mongoClient.getDatabase(dbConfig.getName());
	}
	
	public LinkedList<Measurement> fetchData(String resource, Date dateStart, Date dateEnd) {
		final LinkedList<Measurement> result = new LinkedList<Measurement>();
		
		FindIterable<Document> iterable = db.getCollection(DatabaseConfigurator.MEASUREMENTS_DB_NAME).find(
		        new Document("resource", resource)
		        .append("date",new Document("$gte", dateStart).append("$lt", dateEnd)));
		
		iterable.forEach(
				new Block<Document>() {
		    public void apply(final Document document) {
		        result.addLast(new Measurement(document.getDate("date"), document.getDouble("val")));
		    }
		});
		
		return result;
	}
	
	public LinkedList<String> getResources() {
		DistinctIterable<String> resources = db.getCollection(DatabaseConfigurator.MEASUREMENTS_DB_NAME).distinct("resource", String.class);
		final LinkedList<String> result = new LinkedList<String>();
		
		resources.forEach(
				new Block<String>() {
		    public void apply(final String document) {
		        result.add(document);
		    }
		});
		return result;
	}
}
