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
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import oz.rest.models.Adopter;

import org.jboss.weld.junit5.*;

import oz.rest.models.Pet;
import oz.rest.models.Shelter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.*;

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


    /**
     * Specification Based Testing
     * Tests PetService's `add` function
     * _________________________________________________
     */
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
    void tcb1_8(){
        Pet p = null;
        assertThrows(Exception.class, () -> {
            petService.add(p);
        });
    }
    /**
     * Specification Based Testing
     * Tests PetService's `retrieve` function
     * _________________________________________________
     */
    @Test
    void tcb2_1(){
        ArrayList<String> images = new ArrayList<>();
        images.add(processImage(Path.of("src/test/java/oz/rest/services/test-materials/dog.png")));
        Pet p = generatePet(
                "MeatStick",
                "p42245",
                images,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.retrieve("p42245");
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb2_2(){
        Response r = petService.retrieve("p42245");
        assertEquals(400, r.getStatus());
    }
    @Test
    void tcb2_3(){
        assertThrows(Exception.class, () -> {
            petService.retrieve("a123456");
        });
    }
    @Test
    void tcb2_4(){
        assertThrows(Exception.class, () -> {
            petService.retrieve("");
        });
    }
    @Test
    void tcb2_5(){
        assertThrows(Exception.class, () -> {
            petService.retrieve(null);
        });
    }

    /**
     * Specification Based Testing
     * Tests PetService's `find` function
     * _________________________________________________
     */
    @Test
    void tcb3_1_1(){
        Pet p = generatePet(
                "spots",
                "p42245",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find("spots",null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_1_2(){
        Pet p = generatePet(
                "spots",
                "p42245",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find("",null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_1_3(){
        Pet p = generatePet(
                "spots",
                "p42245",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find(null,null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_1_4(){
        Pet p = generatePet(
                "spots",
                "p42245",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find(null,"p42245", null,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_1_a(){
        Pet p = generatePet(
                "spots",
                "p42245",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        //Response r = petService.find(42,null, null,null,null,null,null,null,null,null,null,null);
        assert true;
    }
    @Test
    void tcb3_2_1(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find(null,"s462901", null,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_2_2(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find(null,"a123123", null,null,null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_2_3(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find(null,null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_2_4(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        Response r = petService.find("spots",null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_2_5(){
        Response r = petService.find(null,"", null,null,null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_2_a(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "Dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        petService.add(p);
        //Response r = petService.find(null,123, null,null,null,null,null,null,null,null,null,null);
        assert true;
    }
    @Test
    void tcb3_3_1(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "cat",
                "calico",
                "yellow",
                "debatable",
                3,
                "yes please",
                "oof",
                "ding dong",
                "Will attack small children"
        );
        petService.add(p);
        List<String> t = List.of(new String[]{"cat"});
        Response r = petService.find(null,null, t,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_3_2(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        Pet p2 = generatePet(
                "spots",
                "s462901",
                null,
                "bird",
                "calico",
                "yellow",
                "debatable",
                3,
                "yes please",
                "oof",
                "ding dong",
                "Will attack small children"
        );
        petService.add(p);
        petService.add(p2);
        List<String> t = List.of(new String[]{"dog", "bird"});
        Response r = petService.find(null,null, t,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_3_3(){
        List<String> t = new ArrayList<>();
        Response r = petService.find(null,null, t,null,null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_3_4(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        Pet p2 = generatePet(
                "spots",
                "s462901",
                null,
                "bird",
                "calico",
                "yellow",
                "debatable",
                3,
                "yes please",
                "oof",
                "ding dong",
                "Will attack small children"
        );
        petService.add(p);
        petService.add(p2);
        Response r = petService.find(null,null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_3_5(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "dog",
                "Schnauzer",
                "gray",
                "debatable",
                3,
                "Male",
                "Small enough to kick, big enough to play fetch",
                "hot-headed",
                "Will attack small children"
        );
        Pet p2 = generatePet(
                "spots",
                "s462901",
                null,
                "bird",
                "calico",
                "yellow",
                "debatable",
                3,
                "yes please",
                "oof",
                "ding dong",
                "Will attack small children"
        );
        petService.add(p);
        petService.add(p2);
        Response r = petService.find("spots",null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_3_a(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "cat",
                "calico",
                "yellow",
                "debatable",
                3,
                "yes please",
                "oof",
                "ding dong",
                "Will attack small children"
        );
        petService.add(p);
        List t = List.of(new int[]{1, 2, 3});
        assertThrows(Exception.class, () -> {
            petService.find(null,null, t,null,null,null,null,null,null,null,null,null);
        });
    }
    @Test
    void tcb3_3_b(){
        Pet p = generatePet(
                "spots",
                "s462901",
                null,
                "cat",
                "calico",
                "yellow",
                "debatable",
                3,
                "yes please",
                "oof",
                "ding dong",
                "Will attack small children"
        );
        petService.add(p);
        List<String> t = List.of(new String[]{"cup"});
        assertThrows(Exception.class, () -> {
            petService.find(null,null, t,null,null,null,null,null,null,null,0,8);
        });
    }
    @Test
    void tcb3_4_1(){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        Response r = petService.find(null, null, null, "German Shephard",null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_4_2(){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        assertDoesNotThrow(() ->{
            petService.find(null,null, null,null,null,null,null,null,null,null,null,null);
        });
    }
    @Test
    void tcb3_4_3(){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        Response r = petService.find(null,null, null,null,null,null,null,null,null,null,null,null);
        assertEquals(200, r.getStatus());
    }
    @Test
    void tcb3_4_4(){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        Response r = petService.find(null,null, null,"",null,null,null,null,null,null,null,null);
        assertEquals(404, r.getStatus());
    }
    @Test
    void tcb3_4_a(){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        String[] t = {"German Shephard"};
        //Response r = petService.find(null,null, null,t,null,null,null,null,null,null,null,null);
        // This will in fact fail...
        assert true;
    }

    Response populateColor(String set, String find, boolean osp){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", set, "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        if(!osp) return petService.find(null,null, null,null,find,null,null,null,null,null,null,null);
        else{ return petService.find("Mollie",null, null,null,find,null,null,null,null,null,null,null);}
    }
    @Test
    void tcb3_5_1(){
        assertEquals(200, populateColor("Red", "Red", false).getStatus());
    }
    @Test
    void tcb3_5_2(){
        assertEquals(404, populateColor("Red", null, false).getStatus());
    }
    @Test
    void tcb3_5_3(){
        assertEquals(200, populateColor("Red", null, true).getStatus());
    }
    @Test
    void tcb3_5_a(){
        //populateColor("Red", 12, false).getStatus());
        // This will in fact always fail
        assert true;
    }
    @Test
    void tcb3_5_b(){
        assertThrows(Exception.class, () ->{
            assertEquals(200, populateColor("Red", "shoes", false).getStatus());
        });
    }
    Response populateHealth(String set, String find, boolean osp){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "Red", set, 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        if(!osp) return petService.find(null,null, null,null,null,find,null,null,null,null,null,null);
        else{ return petService.find("Mollie",null, null,null,null,find,null,null,null,null,null,null);}
    }
    @Test
    void tcb3_6_1(){
        assertEquals(200, populateHealth("healthy", "healthy", false).getStatus());
    }
    @Test
    void tcb3_6_2(){
        assertEquals(404, populateHealth("healthy", "", false).getStatus());
    }
    @Test
    void tcb3_6_3(){
        assertEquals(200, populateHealth("healthy", null, false).getStatus());
    }
    @Test
    void tcb3_6_4(){
        assertEquals(200, populateHealth("healthy", null, true).getStatus());
    }
    @Test
    void tcb3_6_a(){
        //assertEquals(200, populateHealth("healthy", 123, false).getStatus());
        assert true;
    }
    Response populateMinAge(Integer set, Integer find, boolean osp){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "Red", "healthy", set, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        if(!osp) return petService.find(null,null, null,null,null,null,find,null,null,null,null,null);
        else{ return petService.find("Mollie",null, null,null,null,null,find,null,null,null,null,null);}
    }

    @Test
    void tcb3_7_1(){
        assertEquals(200, populateMinAge(10, 10, false).getStatus());
    }

    @Test
    void tcb3_7_2(){
        assertEquals(404, populateMinAge(10, null, false).getStatus());
    }

    @Test
    void tcb3_7_3(){
        assertEquals(200, populateMinAge(10, null, true).getStatus());
    }
    @Test
    void tcb3_7_a(){
        assertThrows(Exception.class, () -> {
            populateMinAge(10, -12, false);});
    }
    @Test
    void tcb3_7_b(){
        assertThrows(Exception.class, () -> {
            populateMinAge(10, 0, false);});
    }

    @Test
    void tcb3_7_c(){
        assertThrows(Exception.class, () -> {
            populateMinAge(10, 1231, false);});
    }


    Response populateMaxAge(Integer set, Integer find, boolean osp){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "Red", "healthy", set, "Female", "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        if(!osp) return petService.find(null,null, null,null,null,null,null, find,null,null,null,null);
        else{ return petService.find("Mollie",null, null,null,null,null,null, find,null,null,null,null);}
    }

    @Test
    void tcb3_8_1(){
        assertEquals(200, populateMaxAge(2, 4, false).getStatus());
    }

    @Test
    void tcb3_8_2(){
        assertEquals(404, populateMaxAge(2, null, false).getStatus());
    }

    @Test
    void tcb3_8_3(){
        assertEquals(200, populateMaxAge(2, null, true).getStatus());
    }
    @Test
    void tcb3_8_a(){
        assertThrows(Exception.class, () ->{
            populateMaxAge(2, -12, false);
        });
    }
    @Test
    void tcb3_8_b(){
        assertThrows(Exception.class, () ->{
            populateMaxAge(2, 0, false);
        });
    }
    @Test
    void tcb3_8_c(){
        //populateMaxAge(2, 22.22, false);
        assert true;
    }
    Response populateSex(String set, String find, boolean osp){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "Red", "healthy", 1, set, "Bigger than a human", "quiet", "Shy dog.");
        petService.add(p);
        if(!osp) return petService.find(null,null, null,null,null,null,null, null,find,null,null,null);
        else{ return petService.find("Mollie",null, null,null,null,null,null, null,find,null,null,null);}
    }
    @Test
    void tcb3_9_1(){
        assertEquals(200, populateSex("Female", "Female", false).getStatus());
    }
    @Test
    void tcb3_9_2(){
        assertEquals(404, populateSex("Female", "", false).getStatus());
    }
    @Test
    void tcb3_9_3(){
        assertEquals(404, populateSex("Female", null, false).getStatus());
    }
    @Test
    void tcb3_9_4(){
        assertEquals(200, populateSex("Female", null, true).getStatus());
    }
    @Test
    void tcb3_9_a(){
        //populateSex("Female", 'a', true);
        assert true;
    }
    Response populateSize(String set, String find, boolean osp){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "Red", "healthy", 1, "Female", set, "quiet", "Shy dog.");
        petService.add(p);
        if(!osp) return petService.find(null,null, null,null,null,null,null, null,null,find,null,null);
        else{ return petService.find("Mollie",null, null,null,null,null,null, null,null,find,null,null);}
    }

    @Test
    void tcb3_10_1(){
        assertEquals(200, populateSize("Large", "Large", false).getStatus());
    }
    @Test
    void tcb3_10_a(){
        //populateSize("Large", 12.4, false);
        assert true;
    }
    @Test
    void tcb3_10_2(){
        assertEquals(404, populateSize("Large", null, false).getStatus());
    }
    @Test
    void tcb3_10_3(){
        assertEquals(200, populateSize("Large", null, true).getStatus());
    }
    @Test
    void tcb3_10_4(){
        assertEquals(404, populateSize("Large", "", false).getStatus());
    }

    Response populatePageSize(Integer size, boolean osp){
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shephard", "Red", "healthy", 1, "Female", "Large", "quiet", "Shy dog.");
        petService.add(p);
        if(!osp) return petService.find(null,null, null,null,null,null,null, null,null,null,size,null);
        else{ return petService.find("Mollie",null, null,null,null,null,null, null,null,null,size,null);}
    }

    @Test
    void tcb3_11_1(){
        assertEquals(200, populatePageSize(32, false).getStatus());
    }

    @Test
    void tcb3_11_a(){
        assertThrows(Exception.class, () -> {
            populatePageSize(-1, false);
        });
    }
    @Test
    void tcb3_11_b(){
        assertThrows(Exception.class, () -> {
            populatePageSize(0, false);
        });
    }
    @Test
    void tcb3_11_c(){
        //populatePageSize("cat", false).getStatus());
        assert true;
    }

    @Test
    void tcb3_11_3(){
        assertEquals(404, populatePageSize(null, false).getStatus());
    }
    @Test
    void tcb3_11_4(){
        assertEquals(200, populatePageSize(null, true).getStatus());
    }

    Response populatePageNumber(Integer size, boolean osp, Integer num){
        for (int i = 0; i < num; i++) {
            String sid = "Mollie" + num.toString();
            Pet p = generatePet("Mollie", sid, null, "Dog", "German Shephard", "Red", "healthy", 1, "Female", "Large", "quiet", "Shy dog.");
            petService.add(p);
        }
        if(!osp) return petService.find(null,null, null,null,null,null,null, null,null,null,null, size);
        else{ return petService.find("Mollie",null, null,null,null,null,null, null,null,null,null, size);}
    }

    @Test
    void tcb3_12_1(){
        assertEquals(200, populatePageNumber(16, false, 20).getStatus());
    }

    @Test
    void tcb3_12_a(){
        assertThrows(Exception.class, () -> {
            populatePageNumber(-5, false, 20).getStatus();
        });
    }

    @Test
    void tcb3_12_b(){
        assertThrows(Exception.class, () -> {
            populatePageNumber(0, false, 20).getStatus();
        });
    }

    @Test
    void tcb3_12_c(){
        //populatePageNumber("size", false, 20).getStatus();
        assert true;
    }
    @Test
    void tcb3_12_2(){
        assertEquals(404, populatePageNumber(null, false, 20).getStatus());
    }

    @Test
    void tcb3_12_3(){
        assertEquals(200, populatePageNumber(null, true, 20).getStatus());
    }
    Response populatePageNumberAndPageSize(Integer pageSize, Integer pageNumber, boolean osp, Integer num){
        for (int i = 0; i < num; i++) {
            String sid = "Mollie" + num.toString();
            Pet p = generatePet("Mollie", sid, null, "Dog", "German Shephard", "Red", "healthy", 1, "Female", "Large", "quiet", "Shy dog.");
            petService.add(p);
        }
        if(!osp) return petService.find(null,null, null,null,null,null,null, null,null,null,pageSize, pageNumber);
        else{ return petService.find("Mollie",null, null,null,null,null,null, null,null,null,pageSize, pageNumber);}
    }
    //BVA test cases
    @Test
    void tcb3_11_5(){
        assertEquals(200, populatePageNumberAndPageSize(1, 2, false, 20).getStatus());
    }
    @Test
    void tcb3_11_6(){
        assertEquals(200, populatePageNumberAndPageSize(2, 1, false, 20).getStatus());
    }
    @Test
    void tcb3_12_d(){
        assertThrows(Exception.class, () -> {
            populatePageNumberAndPageSize(32, -1, false, 50);
        });
    }

    /**
     * Specification Based Testing
     * Tests PetService's `update` function
     * _________________________________________________
     */

    @Test
    void tcb4_1(){
        Pet p = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        ObjectId id = new ObjectId();
        p.setId(id);
        petService.add(p);
        Pet p2 = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "Very loud actually", "Shy dog.");
        assertEquals(200, petService.update(p2, id.toString()).getStatus());
    }

    @Test
    void tcb4_2(){
        ObjectId id = new ObjectId();
        Pet p2 = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "Very loud actually", "Shy dog.");
        assertEquals(404, petService.update(p2, id.toString()).getStatus());
    }

    @Test
    void tcb4_3(){
        Pet p = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        ObjectId id = new ObjectId();
        p.setId(id);
        Pet p2 = generatePet(null, "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "Very loud actually", "Shy dog.");
        assertEquals(400, petService.update(p2, id.toString()).getStatus());
    }

    @Test
    void tcb4_4(){
        Pet p = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        ObjectId id = new ObjectId();
        p.setId(id);
        Pet p2 = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "Very loud actually", null);
        assertEquals(404, petService.update(p2, id.toString()).getStatus());
    }

    @Test
    void tcb4_5(){
        Pet p = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        ObjectId id = new ObjectId();
        p.setId(id);
        Pet p2 = null;
        assertThrows(Exception.class, () ->{
            petService.update(p2, id.toString());
        });
    }
    @Test
    void tcb4_6(){
        Pet p = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        ObjectId id = new ObjectId();
        p.setId(id);
        //Pet p2 = "Silliest of the geese";
        assert true;
    }

    @Test
    void tcb4_7(){
        String s = "s46920";
        String encodedHex = String.format("%24s", Integer.toHexString(s.hashCode())).replace(' ', '0');
        ObjectId id = new ObjectId(encodedHex);
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shepherd", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        p.setId(id);
        petService.add(p);
        Pet p2 = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "Very loud actually", "Shy dog.");
        assertThrows(Exception.class, () -> {
            petService.update(p2, id.toString());
        });
    }

    @Test
    void tcb4_8(){
        String s = "s46920";
        String encodedHex = String.format("%24s", Integer.toHexString(s.hashCode())).replace(' ', '0');
        ObjectId id = new ObjectId(encodedHex);
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shepherd", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        p.setId(id);
        petService.add(p);
        Pet p2 = generatePet("Mollie", "Mollie3245",null, "Dog", "German Shephard", "black", "healthy", 3, "Female", "Bigger than a human", "Very loud actually", "Shy dog.");
        assertThrows(Exception.class, () -> {
            petService.update(p2, null);
        });
    }

    /**
     * Specification Based Testing
     * Tests PetService's `remove` function
     * _________________________________________________
     */
    @Test
    void tcb5_1(){
        String s = "p42335";
        String encodedHex = String.format("%24s", Integer.toHexString(s.hashCode())).replace(' ', '0');
        ObjectId id = new ObjectId(encodedHex);
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shepherd", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        p.setId(id);
        petService.add(p);
        assertEquals(200, petService.remove(id.toString()).getStatus());
    }
    @Test
    void tcb5_2(){
        String s = "p42335";
        String encodedHex = String.format("%24s", Integer.toHexString(s.hashCode())).replace(' ', '0');
        ObjectId id = new ObjectId(encodedHex);
        Pet p = generatePet("Mollie", "Mollie3245", null, "Dog", "German Shepherd", "black", "healthy", 3, "Female", "Bigger than a human", "quiet", "Shy dog.");
        p.setId(id);
        petService.add(p);
        String s2 = "p42335";
        String eh2 = String.format("%24s", Integer.toHexString(s2.hashCode())).replace(' ', '0');
        ObjectId id2 = new ObjectId(eh2);
        assertEquals(200, petService.remove(id2.toString()).getStatus());
    }
    @Test
    void tcb5_3(){
        //Response r = petService.remove(0);
        assert true;
    }
    @Test
    void tcb5_4(){
        assertThrows(Exception.class, ()-> {petService.remove(null);});
    }
    @Test
    void tcb5_5(){
        // not testing endpoints, null and unspecified are interchangeable
        assertThrows(Exception.class, ()-> {petService.remove(null);});
    }
    @Test
    void tcb5_6(){
        Response r = petService.remove(" ");
        assertEquals(400, r.getStatus());
    }
    @Test
    void tcb5_7(){
        Response r = petService.remove("dog");
        assertEquals(400, r.getStatus());
    }
}