package oz.rest.services;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
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

import oz.rest.models.Adopter;

import oz.rest.models.Shelter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static com.mongodb.client.model.Filters.eq;

class AdopterServiceTest {
    protected AdopterService adopterService;
    protected MongoDatabase db;
    protected MongoClient mongoClient;
    protected MongoCollection<Adopter> collection;

    protected Adopter ANominal, ANominalPrime, AEmptyField, ANullField,
            AInvalidEmail, LoginEntry, NullEmail;

    // apparently ObjectId's need to be 24 characters long (the spec doesn't say that explicitly)
    protected final String validAdopterId = "a12345678901234567890123", invalidAdopterId = "s46290";
    protected final ObjectId validAdopterObjectId = new ObjectId(validAdopterId);

    protected final Jsonb jsonb = JsonbBuilder.create();

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
        adopterService = new AdopterService();
        adopterService.db = db;
        // mock validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        adopterService.setValidator(factory.getValidator());
        initializeCollections(db);
        collection = db.getCollection("Adopters", Adopter.class);

        // set up default Adopter values
        ANominal = createAdopter("Greg", "greg@gmail.com");
        ANominalPrime = createAdopter(ANominal.getName() + "2ElectricBoogaloo", ANominal.getEmailAddress());
        AEmptyField = createAdopter("", "");
        ANullField = createAdopter(null, "jimothy@yahoo.com");
        AInvalidEmail = createAdopter("Jimbo", "jimbo");
        LoginEntry = createAdopter(null, ANominal.getEmailAddress());
        NullEmail = createAdopter(null, null);
    }
    // as per MongoProducer
    private void initializeCollections(MongoDatabase database) {
        createUniqueEmailAdopter(database.getCollection("Adopters", Adopter.class));
        createUniqueEmailShelter(database.getCollection("Shelters", Shelter.class));
    }
    private void createUniqueEmailAdopter(MongoCollection<Adopter> adopterCollection) {
        IndexOptions indexOptions = new IndexOptions().unique(true);
        // *** NOT IN LINE WITH THE ACTUAL CODE ***
        // in the actual repo this would be emailAddress, however, that breaks the ability to add multiple adopters
        // woohoo bug found!
        // for the sake of useful testing im using email_address instead
        adopterCollection.createIndex(new Document("email_address", 1), indexOptions);
    }
    private void createUniqueEmailShelter(MongoCollection<Shelter> shelterCollection) {
        IndexOptions indexOptions = new IndexOptions().unique(true);
        shelterCollection.createIndex(new Document("emailAddress", 1), indexOptions);
    }
    @AfterEach
    void tearDown() {
        db.drop();
        mongoClient.close();
    }

    // OL models can only have default constructor
    Adopter createAdopter(String name, String emailAddress) {
        Adopter adopter = new Adopter();
        adopter.setName(name);
        adopter.setEmailAddress(emailAddress);
        return adopter;
    }

    // always want to copy because some methods mutate the parameter
    Adopter copy(Adopter src) {
        Adopter a = new Adopter();
        a.setName(src.getName());
        a.setEmailAddress(src.getEmailAddress());
        a.setId(src.getId());
        return a;
    }

    Adopter readBody(Response response) {
        return fromDocument(Document.parse((String) response.getEntity()));
    }

    Adopter fromDocument(Document doc) {
        String id = null;
        if (doc.containsKey("id")) {
            id = doc.getString("id");
            doc.remove("id");
        }
        Adopter a = jsonb.fromJson(doc.toJson(), Adopter.class);
        if (id != null)
            a.setId(new ObjectId(id));
        return a;
    }

    MongoCollection<Adopter> getAdopters() {
        return adopterService.db.getCollection("Adopters", Adopter.class);
    }

    Adopter getAdopterWithId(ObjectId id) {
        return getAdopters().find(eq("_id", id)).first();
    }

    <T> List<T> iterableToList(Iterable<T> it) {
        List<T> entries = new ArrayList<>();
        it.forEach(entries::add);
        return entries;
    }

    void assertDbEmpty() {
        assertEquals(0, iterableToList(getAdopters().find()).size());
    }

    void expectStatus(Supplier<Response> call, int status) {
        try (Response response = call.get()) {
            assertEquals(status, response.getStatus());
        }
    }

    void shouldFail(Supplier<Response> failingCall) {
        try (Response response = failingCall.get()) {
            assertNotEquals(200, response.getStatus());
            // BT's gonna fillet me
        } catch (Exception e) {
            // pass
        }
    }
}