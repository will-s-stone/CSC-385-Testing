package oz.rest.services;

import com.ibm.websphere.security.jwt.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import oz.rest.models.Adopter;
import oz.rest.models.Shelter;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.*;

class ShelterServiceTest {
    ShelterService shelterService;
    MongoDatabase db;
    MongoClient mongoClient;
    MongoCollection<Shelter> collection;

    @BeforeEach
    void setUp() {
        // declare mongo client and set up codec provider for serialization.
        // this is similar to what is done in MongoProducer but due to it not
        // being a part of our set of services under test, logic is internally
        // held here in the testing environment.
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        CodecProvider pojoCodecProvider = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        // creates local mongo db named 'test'
        // if you have any issues with this, you can utilize mongo compass(gui) to verify creation and population.
        db = mongoClient.getDatabase("test").withCodecRegistry(pojoCodecRegistry);
        shelterService = new ShelterService();
        shelterService.db = db;
        // mock validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        shelterService.setValidator(factory.getValidator());
        initializeCollections(db);
        collection = db.getCollection("Shelters", Shelter.class);
    }
    // as per MongoProducer
    private void initializeCollections(MongoDatabase database) {
        createUniqueEmailAdopter(database.getCollection("Adopters", Adopter.class));
        createUniqueEmailShelter(database.getCollection("Shelters", Shelter.class));
    }
    private void createUniqueEmailAdopter(MongoCollection<Adopter> adopterCollection) {
        IndexOptions indexOptions = new IndexOptions().unique(true);
        adopterCollection.createIndex(new Document("emailAddress", 1), indexOptions);
    }
    private void createUniqueEmailShelter(MongoCollection<Shelter> shelterCollection) {
        IndexOptions indexOptions = new IndexOptions().unique(true);
        shelterCollection.createIndex(new Document("emailAddress", 1), indexOptions);
    }

    @AfterEach
    void tearDown() {
        // there is an issue if you add a unique shelter, then when it attempts to create a jwt, it will always throw an error.
        // butttttt it still add the shelter, so if you run again, assuming you don't drop the database, the test will pass because
        // add() for example will return when it hits line 95 (ShelterService.java).
        // above was remedied by the manual declaration of the jwt
        db.drop();
        mongoClient.close();
    }

    @Test
    void getViolations() {

    }

    @Test
    void setValidator() {
    }

    @Test
    void add() {
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        Response r = shelterService.add(shelter);
        long count = collection.countDocuments();
        assertEquals(1, count);
    }
    @Test
    void addTwo(){
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Shelter");
        shelter.setPassword("password");

        Shelter shelter2 = new Shelter();
        shelter2.setEmailAddress("Shelter2@gmail.com");
        shelter2.setId(new ObjectId());
        shelter2.setName("Shelter2");
        shelter2.setPassword("password2");


        shelterService.add(shelter2);
        shelterService.add(shelter);
        long count = collection.countDocuments();
        assertEquals(2, count);
    }

    @Test
    void retrieve() {
    }

    @Test
    void find() {
    }

    @Test
    void update() {
    }

    @Test
    void remove() {
    }

    @Test
    void login() {
    }

    @Test
    void authenticateJWT() {
    }
}