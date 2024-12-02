package oz.rest.services;

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

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class ZipCodeServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    void testcase_d_1_1_1(){
        //search for existing shelter with the existing properly formated zip code object
        assert true;
    }

    void testcase_d_1_1_2(){
        //search for existing shelter with the existing in-properly formated zip code object
        assert true;
    }

    void testcase_d_1_1_a(){
        //search for existing shelter with the existing null object
        assert true;
    }

    void testcase_d_1_1_b(){
        //search for existing shelter with a non-zip code object
        assert true;
    }

    void testcase_d_2_1_1(){
        //properly formatted zipcode object
        assert true;
    }

    void testcase_d_2_1_a(){
        //in-properly formatted zipcode object
        assert true;
    }

    void testcase_d_2_1_b(){
        //null object
        assert true;
    }

    void testcase_d_2_1_c(){
        //not a zipcode object
        assert true;
    }

    void testcase_d_2_1_d(){
        //a zip code object with a fake zipcode
        assert true;
    }

    void testcase_d5_1_1_1(){
        //Properly formatted Zip Code object and no shelters exist in the database with that zipcode
        assert true;
    }

    void testcase_d5_1_1_2(){
        //Properly formatted Zip Code object and one shelters exist in the database with that zipcode
        assert true;
    }

    void testcase_d5_1_1_3(){
        //Properly formatted Zip Code object and two shelters exist in the database with that zipcode
        assert true;
    }

    void testcase_d5_1_1_4(){
        //Properly formatted Zip Code object and three shelters exist in the database with that zipcode
        assert true;
    }

    void testcase_d8_1_1_1(){
        //null db
        assert true;
    }
    void testcase_d8_1_1_2(){
        //null validator
        assert true;
    }

    void testcase_d8_2_1_1(){
        //null db
        assert true;
    }
    void testcase_d8_2_1_2(){
        //null validator
        assert true;
    }




}