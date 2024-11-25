package oz.rest.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import oz.rest.MongoProducer;
import oz.rest.models.Pet;
import jakarta.validation.Validator;
import com.mongodb.client.*;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.*;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

@WeldSetup
class PetServiceTest {
    static MongoDBContainer mongoContainer;
    @Inject
    PetService petService;

    MongoProducer mp;
    MongoDatabase db;
    PetService ps;
    MongoClient mongoClient;
    Validator validator;

    @BeforeEach
    void setUp() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        db = mongoClient.getDatabase("test");
        ps = new PetService();
        ps.db = db;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Inject the validator into your service (if required)
        ps.setValidator(validator);
    }
    @AfterEach
    void tearDown() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Test
    void add() {
        Pet p = new Pet();
        p.setName("test");
        p.setAge(1);
        p.setDescription("fluff fluff");
        p.setColor("Brown");
        p.setId(new ObjectId());
        ps.add(p);
//        MongoCollection<Document> pets = db.getCollection("pets");
//        long count = pets.countDocuments(new Document("name", "Fluffy"));
//        assertEquals(1, count, "The pet was not added to the database");
        System.out.println();
        MongoCollection<Pet> pets = db.getCollection("pets", Pet.class);
        long count = pets.countDocuments(new Document("name", "test")); // Update the name to match the inserted name
        assertEquals(1, count, "The pet was not added to the database");
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
}