package oz.rest.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import jakarta.ws.rs.core.Response;
import oz.rest.models.Adopter;

import jakarta.validation.Validator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdopterServiceTest {
    @Mock
    private MongoDatabase mockDb;

    @Mock
    private MongoCollection<Adopter> mockCollection;

    @Mock
    private Validator validator;

    @InjectMocks
    private AdopterService adopterService;

    @BeforeEach
    void setup() {
        adopterService.setValidator(validator);
        when(mockDb.getCollection("Adopters", Adopter.class)).thenReturn(mockCollection);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testAddValidAdopter() {
        Adopter newAdopter = new Adopter();
        newAdopter.setName("John Doe");
        newAdopter.setEmailAddress("johndoe@example.com");

        InsertOneResult mockResult = mock(InsertOneResult.class);
        ObjectId mockObjectId = new ObjectId();

        when(mockResult.getInsertedId()).thenReturn(new org.bson.BsonObjectId(mockObjectId));
        when(mockCollection.insertOne(any(Adopter.class))).thenReturn(mockResult);
        adopterService.db = mock(MongoDatabase.class);
        Response response = adopterService.add(newAdopter);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("John Doe"));
    }

    @Test
    void getViolations() {
    }

    @Test
    void setValidator() {
    }

    @Test
    void add() {
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
}