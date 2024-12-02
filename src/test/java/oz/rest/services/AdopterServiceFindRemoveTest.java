package oz.rest.services;

import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import oz.rest.models.Adopter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static com.mongodb.client.model.Filters.eq;

public class AdopterServiceFindRemoveTest extends AdopterServiceTest {

    final Adopter[] dbEntries = {
        createAdopter("Greg", "greg@gmail.com"), createAdopter("Robert Baker", "rbaker@gmail.com"),
        createAdopter("Don Joe", "jdon35@outlook.com"), createAdopter("Patricia Clark", "pclark@gmail.com"),
        createAdopter("Greg", "notasmurf@gmail.com"), createAdopter("William Davis", "wdavis@gmail.com"),
        createAdopter("Jimothy", "jimbo@proton.me"), createAdopter("Linda Frank", "lfrank@gmail.com"),
        createAdopter("Jane Doe", "doej99@hotmail.com"), createAdopter("Elizabeth Jones", "ejones@gmail.com"),
        createAdopter("James Logan", "wolverine@xmen.aol.com"), createAdopter("Thomas Quinn", "tquinn@gmail.com")
    };

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();

        for (Adopter adopter : dbEntries)
            adopterService.add(copy(adopter)).close();
    }

    void assertFindResultsEqual(Supplier<Response> call, Adopter[] expected) {
        try (Response response = call.get()) {
            assertEquals(200, response.getStatus());

            List<Adopter> results = new ArrayList<>();
            List<Document> docs = jsonb.fromJson((String) response.getEntity(),
                    new ArrayList<Document>(){}.getClass().getGenericSuperclass());
            for (Document doc : docs) {
                results.add(fromDocument(doc));
            }

            assertContainsExactly(expected, results);
        }
    }

    void assertContainsExactly(Adopter[] expected, Collection<Adopter> actual) {
        // |A| = |B| ^ A c B => A = B
        assertEquals(expected.length, actual.size());
        // adopter doesn't implement equals() else we could use assertTrue(results.containsAll(expected));
        for (Adopter e : expected) {
            // technically this assumes there aren't any duplicates, but that's fine since we check the size
            // it wouldn't be fine if we swapped expected and results in the loop
            assertTrue(actual.stream().anyMatch(
                    r -> r.getName().equals(e.getName()) && r.getEmailAddress().equals(e.getEmailAddress())
            ));
        }
    }

    // **** find() (hoo boy my favorite 0_0) ****
    // discrepancy: in the qmr, i have the page numbers counting from 1, but i have them counting from 0 here
    // this is so the tests can be useful, else virtually all will fail. that being said it is still something to report

    @Test
    void tca_3_1() {
        assertFindResultsEqual(
                () -> adopterService.find(dbEntries[0].getName(), dbEntries[0].getEmailAddress(), 1, 0),
                new Adopter[]{ dbEntries[0] }
        );
    }

    @Test
    void tca_3_2() {
        assertFindResultsEqual(
                () -> adopterService.find("", "", 8, 0),
                new Adopter[]{}
        );
    }

    @Test
    void tca_3_3() {
        assertFindResultsEqual(
                () -> adopterService.find(dbEntries[0].getName(), null, 8, 0),
                new Adopter[]{ dbEntries[0], dbEntries[4] }
        );
    }

    @Test
    void tca_3_4() {
        assertFindResultsEqual(
                () -> adopterService.find(null, dbEntries[0].getEmailAddress(), 8, 0),
                new Adopter[]{ dbEntries[0] }
        );
    }

    @Test
    void tca_3_5() {
        assertFindResultsEqual(
                () -> adopterService.find(null, null, 8, 1),
                new Adopter[]{ dbEntries[8], dbEntries[9], dbEntries[10], dbEntries[11] }
        );
    }

    // omitted TC#a.3.6, TC#a.3.7 because java is a sane language and won't just allow random casts

    @Test
    void tca_3_8() {
        shouldFail(() -> adopterService.find(null, null, 0, 1));
    }

    @Test
    void tca_3_9() {
        shouldFail(() -> adopterService.find(null, null, -1, 1));
    }

    @Test
    void tca_3_10() {
        shouldFail(() -> adopterService.find(null, null, null, 1));
    }

    // omitted TC#a.3.11, TC#a.3.12 for the same reason as TC#a.3.6

//    @Test
//    void tca_3_13() {
//        shouldFail(() -> adopterService.find(null, null, 8, 0));
//    }

    @Test
    void tca_3_14() {
        shouldFail(() -> adopterService.find(null, null, 8, -1));
    }

    @Test
    void tca_3_15() {
        shouldFail(() -> adopterService.find(null, null, 8, null));
    }

    // omitted TC#a.3.16, TC#a.3.17 for the same reason as TC#a.3.6

    @Test
    void tca_3_18() {
        assertFindResultsEqual(
                () -> adopterService.find(null, null, 2, 0),
                new Adopter[]{ dbEntries[0], dbEntries[1] }
        );
    }

    @Test
    void tca_3_19() {
        assertFindResultsEqual(
                () -> adopterService.find(null, null, null, null),
                new Adopter[]{ dbEntries[0] }
        );
    }

    @Test
    void tca_3_20() {
        expectStatus(() -> adopterService.find("Nonsense", null, null, null), 404);
    }

    // **** remove() ****

    @Test
    void tca_5_1and2() {
        // not following the test exactly, instead of using "a123456" as the id, i will use the id of the first entry
        ObjectId id = getAdopters().find(eq("email_address", dbEntries[0].getEmailAddress())).first().getId();
        Adopter[] postOpEntries = Arrays.copyOfRange(dbEntries, 1, dbEntries.length);

        // TC#a.5.1
        expectStatus(() -> adopterService.remove(id.toString()), 200);
        assertContainsExactly(postOpEntries, iterableToList(getAdopters().find()));

        //TC#a.5.2
        try (Response response = adopterService.remove(id.toString())) {
            assertNotEquals(200, response.getStatus());
        }
        assertContainsExactly(postOpEntries, iterableToList(getAdopters().find()));
    }

    @Test
    void tca_5_3() {
        expectStatus(() -> adopterService.remove(invalidAdopterId), 400);
    }

    @Test
    void tca_5_4() {
        shouldFail(() -> adopterService.remove(""));
    }

    @Test
    void tca_5_5() {
        shouldFail(() -> adopterService.remove(null));
    }

}
