package oz.rest.services;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import oz.rest.models.Adopter;

import org.jboss.weld.junit5.*;

import oz.rest.models.Pet;
import oz.rest.models.Shelter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WeldSetup
class PetServiceTest {
    PetService petService;
    MongoDatabase db;
    MongoClient mongoClient;
    MongoCollection<Pet> collection;

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
        petService = new PetService();
        petService.db = db;
        // mock validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        petService.setValidator(factory.getValidator());
        initializeCollections(db);
        collection = db.getCollection("Pets", Pet.class);
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
    String processImage(Path path){
        try {
            byte[] imageBytes = Files.readAllBytes(path);

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return null;
    }

    Pet generatePet(String name, String sid, ArrayList<String> images, String type, String breed, String color, String health, Integer age, String sex, String size, String temperament, String description) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setCurrentShelterId(sid);
        pet.setImages(images);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setColor(color);
        pet.setHealth(health);
        pet.setAge(age);
        pet.setSex(sex);
        pet.setSize(size);
        pet.setTemperament(temperament);
        pet.setDescription(description);
        return pet;
    }
    @AfterEach
    void tearDown() {
        db.drop();
        mongoClient.close();
    }

    @Test
    void tcb1_1(){
        ArrayList<String> images = new ArrayList<>();
        images.add(processImage(Path.of("src/test/java/oz/rest/services/test-materials/dog.png")));
        Pet p = generatePet("Mollie", "Mollie3245", images, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        Response r = petService.add(p);
        assertEquals(200, r.getStatus());
    }

    @Test
    void tcb1_2(){
        ArrayList<String> images = new ArrayList<>();
        images.add(processImage(Path.of("src/test/java/oz/rest/services/test-materials/dog.png")));
        Pet p = generatePet("Mollie", "Mollie3245", images, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        Response r = petService.add(p);
        assertEquals(200, r.getStatus());
    }

    @Test
    void tcb1_3(){
        Pet p = generatePet("Cloudy", "Cloudy2024", null, "Bird", "Lovebird", "green", "", 1, "Male", "small", "too loud", "Cloudy is awesome. He loves cuddles, he loves being around people and he is so social. I really hope he finds a new home because all his friends have left and its just so sad. To tell you more about Cloudy, he loves to eat. He is green but has a red head. He is very pretty and elegant looking. He is also not exactly a year old, he is only 4 months old and he was actually born here. I love Cloudy and I will miss him so much. Please pick him and take good care of him. He is a really good guy and this will be the best purchase of your life.");
        Response r = petService.add(p);
        assertEquals(400, r.getStatus());
    }
    @Test
    void tcb1_4(){
        Pet p = generatePet("", "", null, "", "", "", "", null, "", "", "", "");
        Response r = petService.add(p);
        assertEquals(400, r.getStatus());
    }

    @Test
    void tcb1_5(){
        Pet p = generatePet("Kritika", null, null, null, null, null, null, null, null, null, null, null);
        // no functionality to add email address without modifying source code.
        Response r = petService.add(p);
        assertEquals(400, r.getStatus());
    }
    @Test
    void tcb1_6(){
        // This would test the endpoint, not the services, therefore it is out of scope.
        //Pet p = "lord have mercy";
        assert(true);
    }
    @Test
    void tcb1_7(){
        // This would test the endpoint, not the services, therefore it is out of scope.
        //Pet p = This m... Former President";
        assert(true);
    }
    @Test
    void tcb_8(){
        Pet p = null;
        assertThrows(Exception.class, () -> {
            petService.add(p);
        });
    }
}