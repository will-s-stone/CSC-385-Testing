package oz.rest.services;

import com.mongodb.MongoWriteException;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import oz.rest.models.Adopter;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdopterServiceOthersTest extends AdopterServiceTest {

    // **** add() ****

    @Test
    void tca_1_1and2() {
        // TC#a.1.1
        Adopter tca11DbEntry;
        try (Response response = adopterService.add(copy(ANominal))) {
            // ensure add was ok
            assertEquals(200, response.getStatus());

            tca11DbEntry = getAdopterWithId(readBody(response).getId());
            // ensure *something* was added to db
            if (tca11DbEntry == null)
                fail();
            // ensure the returned entry is correct
            assertEquals(ANominal.getName(), tca11DbEntry.getName());
            assertEquals(ANominal.getEmailAddress(), tca11DbEntry.getEmailAddress());
        }

        // TC#a.1.2, because the database already contains an entry with "greg@gmail.com" now
        // want to add a distinct entry to detect if the database is modified
        try (Response response = adopterService.add(copy(ANominalPrime))) {
            // ensure everything is not peachy
            assertNotEquals(200, response.getStatus());

            List<Adopter> dbEntries = iterableToList(
                    getAdopters().find(eq("email_address", ANominal.getEmailAddress())));
            // ensure there is still only one entry
            assertEquals(1, dbEntries.size());
            // ensure no fields were overwritten
            assertEquals(ANominal.getName(), dbEntries.get(0).getName());
        } catch (MongoWriteException e) {
            fail();
        }
    }

    @Test
    void tca_1_3() {
        expectStatus(() -> adopterService.add(copy(AEmptyField)), 400);
        assertDbEmpty();
    }

    @Test
    void tca_1_4() {
        expectStatus(() -> adopterService.add(copy(ANullField)), 400);
        assertDbEmpty();
    }

    @Test
    void tca_1_5() {
        shouldFail(() -> adopterService.add(null));
        assertDbEmpty();
    }

/*
    untestable because java is a sane language
    @Test
    void tca_1_6() {
        shouldFail(() -> adopterService.add((Adopter) "Hello World"));
        assertDbEmpty();
    }
*/

    @Test
    void tca_1_7() {
        expectStatus(() -> adopterService.add(copy(AInvalidEmail)), 400);
        assertDbEmpty();
    }

    // **** retrieve() ****

    @Test
    void tca_2_1() {
        // add an adopter with a given id
        ANominal.setId(validAdopterObjectId);
        adopterService.add(copy(ANominal)).close(); // close to make the warning go away

        try (Response response = adopterService.retrieve(validAdopterId)) {
            assertEquals(200, response.getStatus());
            Adopter returned = readBody(response);
            assertEquals(ANominal.getName(), returned.getName());
            assertEquals(ANominal.getEmailAddress(), returned.getEmailAddress());
        }
    }

    @Test
    void tca_2_2() {
        expectStatus(() -> adopterService.retrieve(validAdopterId), 404);
    }

    @Test
    void tca_2_3() {
        expectStatus(() -> adopterService.retrieve(invalidAdopterId), 400);
    }

    @Test
    void tca_2_4() {
        shouldFail(() -> adopterService.retrieve(""));
    }


    @Test
    void tca_2_5() {
        shouldFail(() -> adopterService.retrieve(null));
    }

    // **** update ****

    @Test
    void tca_4_1() {
        ANominalPrime.setId(validAdopterObjectId);
        adopterService.add(copy(ANominalPrime)).close();

        try (Response response = adopterService.update(copy(ANominal), validAdopterId)) {
            assertEquals(200, response.getStatus());
            Adopter fromDb = getAdopterWithId(validAdopterObjectId);
            assertEquals(ANominal.getName(), fromDb.getName());
            assertEquals(ANominal.getEmailAddress(), fromDb.getEmailAddress());
        }
    }

    @Test
    void tca_4_2() {
        expectStatus(() -> adopterService.update(copy(ANominal), validAdopterId), 404);
        assertDbEmpty();
    }

    @Test
    void tca_4_3() {
        expectStatus(() -> adopterService.update(copy(AEmptyField), validAdopterId), 400);
        assertDbEmpty();
    }

    @Test
    void tca_4_4() {
        expectStatus(() -> adopterService.update(copy(ANullField), validAdopterId), 400);
        assertDbEmpty();
    }

    @Test
    void tca_4_5() {
        shouldFail(() -> adopterService.update(null, validAdopterId));
        assertDbEmpty();
    }

    // TC#a.4.6 omitted for the same reason as TC#a.1.6

    @Test
    void tca_4_7() {
        expectStatus(() -> adopterService.update(copy(ANominal), invalidAdopterId), 400);
        assertDbEmpty();
    }

    @Test
    void tca_4_8() {
        shouldFail(() -> adopterService.update(copy(ANominal), null));
        assertDbEmpty();
    }

    @Test
    void tca_4_9() {
        expectStatus(() -> adopterService.update(copy(AInvalidEmail), validAdopterId), 400);
        assertDbEmpty();
    }

    // **** login ****

    @Test
    void tca_6_1() {
        adopterService.add(copy(ANominal)).close();

        expectStatus(() -> adopterService.login(copy(LoginEntry)), 200);
        // spec doesn't give me anything about the return value of login aside from Response 200
    }

    @Test
    void tca_6_2() {
        expectStatus(() -> adopterService.login(copy(LoginEntry)), 400);
    }

    @Test
    void tca_6_3() {
        expectStatus(() -> adopterService.login(copy(AEmptyField)), 400);
    }

    @Test
    void tca_6_4() {
        expectStatus(() -> adopterService.login(copy(NullEmail)), 400);
    }

    @Test
    void tca_6_5() {
        shouldFail(() -> adopterService.login(null));
    }

    // TC#a.6.6 omitted for the same reason as TC#a.1.1
}
