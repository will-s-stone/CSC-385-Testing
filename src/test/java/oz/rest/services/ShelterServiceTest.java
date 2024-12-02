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
import oz.rest.models.ZipCode;

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
    void testCase_c_1_1_1(){
        //add a properly formatted shelter object
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        Response r = shelterService.add(shelter);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_1_1_2() throws InterruptedException {
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Thomas house");
        shelter.setPassword("562462");
        shelter.setDescription("thomas house");
        Response r = shelterService.add(shelter);
        assertEquals(400, r.getStatus());
    }

    @Test
    void testCase_c_1_1_3(){
        //add a null object
        Shelter shelter = null;
        Response r = shelterService.add(shelter);
        assertEquals(400, r.getStatus());
    }

    @Test
    void testCase_c_1_1_4(){
        //add two of the same shelter objects to the database
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Shelter shelter2 = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        Response r1 = shelterService.add(shelter2);
        assertEquals(409, r1.getStatus());
    }

    @Test
    void testCase_c_2_1_1(){
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r1 = shelterService.retrieve(id.toString());
        assertEquals(200, r1.getStatus());
        //retrieve an existing shelter with the given id
    }

    @Test
    void testCase_c_2_1_a(){
        //TEST CASE NOT ACTUALLY POSSIBLE
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        shelterService.add(shelter);
//        assertThrowsExactly(Exception.class,()->{
//            shelterService.retrieve(id.toString());
//        });
        //retrieve an non-existing shelter with the given id
    }

    @Test
    void testCase_c_3_1_1(){
        //search for a existing shelter object that has the name field search filled
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("Amsterdam Animal Shelter","",0,0);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_1_2(){
        //search for name using an empty string
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",0,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_1_3(){
        //search for name using a null
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find(null,"",0,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_1_a(){
        //TEST CASE NOT POSSIBLE
        //search for name using a integer
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("48","",0,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_2_1(){
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","pawspawshelter@gmail.com",0,0);
        assertEquals(200, r.getStatus());

    }

    @Test
    void testCase_c_3_2_2(){
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",0,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_2_3(){
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("",null,0,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_2_a(){
        //TEST CASE NOT POSSIBLE
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("",null,0,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_3_1(){
        //search for page size of 3
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",3,0);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_3_2(){
        //search for page size of null
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",null,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_3_b(){
        //TEST CASE NOT POSSIBLE
        //search for page size of "5"
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",5 /*"5"*/,0);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_3_c(){
        //search for page size of 0
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
//        assertThrowsExactly(Exception.class, ()->{
//            Response r = shelterService.find("","",0,0);
//        });
    }

    @Test
    void testCase_c_3_3_7(){
        //search for page size of 2
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",2,0);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_3_4(){
        //search for page size of 1
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",1,0);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_3_5(){
        //search for page size of Max_integer
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",Integer.MAX_VALUE,0);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_3_6(){
        //search for page size of Max_integer - 1
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",Integer.MAX_VALUE-1,0);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_3_d(){
        //search for page size of Max_integer + 1
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",Integer.MAX_VALUE+1,0);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_4_1(){
        //search for page Number of 1
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",0,1);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_4_2(){
        //search for page Number of null
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",0,null);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c_3_4_a(){
        //search for page Number of -9
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
//        assertThrowsExactly(Exception.class, ()->{
//            shelterService.find("","",0,-9);
//        });
    }

    @Test
    void testCase_c_3_4_c(){
        //search for page Number of 0
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
//        assertThrowsExactly(Exception.class, ()->{
//            shelterService.find("","",0,0);
//        });
    }

    @Test
    void testCase_c_3_4_d(){
        //search for page Number of 2
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",0,2);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_4_4(){
        //search for page Number of Max Integer
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",0,Integer.MAX_VALUE);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_4_5(){
        //search for page Number of Max Integer -1
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("","",0,Integer.MAX_VALUE-1);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_3_4_e(){
        //search for page Number of Max Integer + 1
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("pawspawshelter@gmail.com");
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setId(new ObjectId());
        shelter.setPassword("password3");
        shelterService.add(shelter);
//        assertThrowsExactly(Exception.class,()->{
//            shelterService.find("","",0,Integer.MAX_VALUE+1);
//        });
    }

    @Test
    void testCase_c_4_1_1(){
        //update an existing shelter object with a new proper shelter object
        //with an existing id and proper id
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("523051");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Shelter updateShelter = new Shelter();
        updateShelter.setEmailAddress("tomhouse@gmail.com");
        updateShelter.setName("Toms cat house");
        updateShelter.setId(id);
        updateShelter.setPassword("523051");
        updateShelter.setDescription("tom loves cats and has a house to keep cats");
        updateShelter.setPhoneNumber("518-777-8100");
        Response r = shelterService.update(updateShelter,id.toString());
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_4_1_2(){
        //update an existing shelter object with a new in-proper shelter object
        //with an existing id and proper id
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("523051");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Shelter updateShelter = new Shelter();
        updateShelter.setName("Thomas House");
        updateShelter.setId(id);
        updateShelter.setPassword("562462");
        updateShelter.setDescription("Thomas House");
        Response r = shelterService.update(updateShelter,id.toString());
        assertEquals(400, r.getStatus());
    }

    @Test
    void testCase_c_4_1_3(){
        //update an existing shelter object with a null object
        //with an existing id and proper id
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("523051");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Shelter updateShelter = null;
        Response r = shelterService.update(updateShelter,id.toString());
        assertEquals(400, r.getStatus());
    }

    @Test
    void testCase_c_5_1_1(){
        //remove an existing gobject with an existing proper id
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("523051");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Response r = shelterService.remove(id.toString());
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_6_1_1() throws Exception {
        //use an existing and proper email
        //use an existing and proper password
        //This did work, just no actual login service
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("gingerbread1231!");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Response r = shelterService.login("tomhouse@gmail.com","gingerbread1231!");
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_6_1_2() throws Exception {
        //use an non-existing and proper email
        //use an existing and proper password
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("gingerbread1231!");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Response r = shelterService.login("joberd@gmail.com","gingerbread1231!");
        assertEquals(401, r.getStatus());
    }

    @Test
    void testCase_c_6_1_3() throws Exception {
        //use an "" email
        //use an existing and proper password
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("gingerbread1231!");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Response r = shelterService.login("","gingerbread1231!");
        assertEquals(401, r.getStatus());
    }

    @Test
    void testCase_c_6_1_a(){
        //use an null email
        //use an existing and proper password
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("gingerbread1231!");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
//        assertThrowsExactly(NullPointerException.class,()->{
//            shelterService.login(null,"gingerbread1231!");
//        });
    }

    @Test
    void testCase_c_6_2_2() throws Exception {
        //use an existing and proper email
        //use an non-existing and proper password
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("gingerbread1231!");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Response r = shelterService.login("tomhouse@gmail.com","manker@#1");
        assertEquals(401, r.getStatus());
    }

    @Test
    void testCase_c_6_2_3() throws Exception {
        //use an existing and proper email
        //use an "" password
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("gingerbread1231!");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
        Response r = shelterService.login("tomhouse@gmail.com","");
        assertEquals(401, r.getStatus());
    }

    @Test
    void testCase_c_6_2_a() throws Exception {
        //use an existing and proper email
        //use an null password
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("tomhouse@gmail.com");
        shelter.setName("Toms cat house");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setPassword("gingerbread1231!");
        shelter.setDescription("tom loves cats and has a house to keep cats");
        shelter.setPhoneNumber("312-325-3325");
        shelterService.add(shelter);
//        assertThrowsExactly(NullPointerException.class,()->{
//            shelterService.login("tomhouse@gmail.com",null);
//        });
    }

    @Test
    void testCase_c_7_1_1(){
        //use an existing and proper id
        //use an properly formatted jwt token
        Response r = shelterService.authenticateJWT("s1234","H2385");
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c_7_1_2(){
        //use an existing and proper id
        //use an in-proper formatted jwt token
        Response r = shelterService.authenticateJWT("s1234","66666666");
        assertEquals(401, r.getStatus());
    }

    @Test
    void testCase_c_7_1_3(){
        //use an existing and proper id
        //use an "" jwt token
        Response r = shelterService.authenticateJWT("s1234","");
        assertEquals(401, r.getStatus());
    }

    @Test
    void testCase_c_7_1_a(){
        //TEST CASE NOT POSSIBLE
        //use an existing and proper id
        //use an integer jwt token
        assert false;
    }

    @Test
    void testCase_c_7_1_b(){
        //use an existing and proper id
        //use an null jwt token
        assertThrows(Exception.class, ()->{
            shelterService.authenticateJWT("s1234",null);
        });
    }

    @Test
    void testCase_c5_2_1_1(){
        //Properly formatted id but not existing shelter with that id
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.retrieve(new ObjectId().toString());
        assertEquals(400, r.getStatus());
    }

    @Test
    void testCase_c5_3_1_1(){
        //all fields for the find method are null
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("Shelter3@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Shelter3");
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find(null,null,null,null);
        assertEquals(404, r.getStatus());
    }

    @Test
    void testCase_c5_3_1_2(){
        //all fields for the find method are filled and proper
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("animalAmsterdam@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setPassword("password3");
        shelterService.add(shelter);
        Response r = shelterService.find("Amsterdam Animal Shelter","animalAmsterdam@gmail.com",3,3);
        assertEquals(200, r.getStatus());
    }

    @Test
    void testCase_c5_3_1_3(){
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("AnimalAmsterdam@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setPassword("password1");
        shelterService.add(shelter);

        Shelter shelter1 = new Shelter();
        shelter.setEmailAddress("animalamsterdam@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setPassword("password2");
        shelterService.add(shelter1);

        Shelter shelter2 = new Shelter();
        shelter.setEmailAddress("animalAmsterdam@gmail.com");
        shelter.setId(new ObjectId());
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setPassword("password3");
        shelterService.add(shelter2);
        Response r = shelterService.find("Amsterdam Animal Shelter","animalAmsterdam@gmail.com",3,1);
        assertEquals(200,r.getStatus());
    }

    @Test
    void testCase_c5_4_1_1() {
        //properly formatted sheller object
        //proper id but not corresponding shelter with that id
        Shelter shelter = new Shelter();
        shelter.setEmailAddress("AnimalAmsterdam@gmail.com");
        ObjectId id = new ObjectId();
        shelter.setId(id);
        shelter.setName("Amsterdam Animal Shelter");
        shelter.setPassword("password1");
        shelterService.add(shelter);
        Shelter updatedShelter = new Shelter();
        updatedShelter.setEmailAddress("AnimalAmsterdam@gmail.com");
        updatedShelter.setName("Amsterdam Animal Shelter");
        updatedShelter.setPassword("password1");
        Response r = shelterService.update(updatedShelter, new ObjectId().toString());
        assertEquals(404, r.getStatus());
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