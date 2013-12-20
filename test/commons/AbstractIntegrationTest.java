package commons;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.junit.After;
import org.junit.Before;

import java.net.UnknownHostException;

/**
 * Created by adelegue on 20/12/2013.
 */
public abstract class AbstractIntegrationTest {

    protected DB currentDataBase = null;

    @Before
    public void initDb() throws UnknownHostException{
        Mongo mongoClient = new Mongo("localhost", 27017);
        currentDataBase = mongoClient.getDB("covoiturage-test");
    }

    @After
    public void removeDb(){
        currentDataBase.dropDatabase();
    }

}
