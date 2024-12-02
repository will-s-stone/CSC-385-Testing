package oz.rest.services;

import jakarta.json.JsonArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import oz.rest.models.ZipCode;

import java.io.IOException;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class ZipCodeServiceTest {
    ZipCodeService zipCodeService;
    MongoDatabase db;
    MongoClient mongoClient;

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
        zipCodeService = new ZipCodeService();
        zipCodeService.db = db;
        // mock validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        zipCodeService.validator = factory.getValidator();
        initializeCollections(db);
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
    void testcase_d_1_1_1() throws IOException {
        ZipCode zipCode = new ZipCode();
        zipCode.setZipCode("12010");
        Response r = zipCodeService.processZipCode(zipCode);
        assertEquals(200, r.getStatus());
    }
    @Test
    void testcase_d_1_1_2() throws IOException {
        ZipCode zipCode = new ZipCode();
        zipCode.setZipCode("");
        Response r = zipCodeService.processZipCode(zipCode);
        assertEquals(200, r.getStatus());
    }
    @Test
    void testcase_d_1_1_a() throws IOException {
        ZipCode zipCode = null;
        Response r = zipCodeService.processZipCode(zipCode);
        //assertThrowsExactly(IllegalArgumentException.class, () ->{
            //zipCodeService.processZipCode(zipCode);
        //});
    }
    @Test
    void testcase_d_1_1_b(){
        //TEST CASE NOT POSSIBLE
        //process a non-zip code object
        assert false;
    }
    @Test
    void testcase_d_2_1_1(){
        //properly formatted zipcode object
        ZipCode zipCode = new ZipCode();
        zipCode.setZipCode("12010");
        JsonArray jsonArray = zipCodeService.getViolations(zipCode);
        assertTrue(jsonArray.isEmpty());
    }
    @Test
    void testcase_d_2_1_a(){
        //in-properly formatted zipcode object
        ZipCode zipCode = new ZipCode();
        zipCode.setZipCode("");
        assertThrows(Exception.class, () ->{
            zipCodeService.getViolations(zipCode);
        });
    }
    @Test
    void testcase_d_2_1_b(){
        //null object
        ZipCode zipCode = null;
        assertThrows(Exception.class, () ->{
            zipCodeService.getViolations(zipCode);
        });
    }
    @Test
    void testcase_d_2_1_c(){
        //TEST CASE NOT POSSIBLE
        //not a zipcode object
        assert false;
    }
    @Test
    void testcase_d_2_1_d(){
        //a zip code object with a fake zipcode
        ZipCode zipCode = new ZipCode();
        zipCode.setZipCode("111111111");
        assertThrows(Exception.class, () ->{
            zipCodeService.getViolations(zipCode);
        });
    }
}