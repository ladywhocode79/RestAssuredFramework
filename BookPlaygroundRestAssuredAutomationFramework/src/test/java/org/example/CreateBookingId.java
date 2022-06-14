package org.example;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import static org.example.framework.utils.BusinessUtils.randomString;
import static org.hamcrest.Matchers.equalTo;

public class CreateBookingId  extends BaseTest{
    String bookingId;
    @Test(priority = 1, description = "create booking id using string payload")
    public void createBookingId() {
        String firstName = randomString(10);
        String lastName = randomString(10);
        setUp();
        String payload = "{\n" +
                "    \"firstname\" : \""+firstName+"\",\n" +
                "    \"lastname\" : \""+lastName +"\",\n" +
                "    \"totalprice\" : 111,\n" +
                "    \"depositpaid\" : true,\n" +
                "    \"bookingdates\" : {\n" +
                "        \"checkin\" : \"2018-01-01\",\n" +
                "        \"checkout\" : \"2019-01-01\"\n" +
                "    },\n" +
                "    \"additionalneeds\" : \"Breakfast\"\n" +
                "}";
        Response response = RestAssured.given().spec(requestSpecification)
                .body(payload)
                .when().post("/booking")
                .then().spec(responseSpecification).extract().response();
         bookingId = response.jsonPath().getString("bookingid");
    }

    @Test(priority = 2, description = "create booking id using json file payload")
    public void createBookingIdWithJsonFile() {
        setUp();
       File payloadFile = new File("src/test/java/org/example/data/createbooking.json");
        Response response = RestAssured.given().spec(requestSpecification)
                .body(payloadFile)
                .when().post("/booking")
                .then().spec(responseSpecification).extract().response();
        bookingId = response.jsonPath().getString("bookingid");
    }

    @Test(priority = 3, description = "update booking id using hashmaps and patch(partial update) payload",
    dependsOnMethods = "createBookingIdWithJsonFile")
    public void updateBookingWithJsonObjectHashMap() {
        setUp();
        String firstName = randomString(10);
        String lastName = randomString(10);

        HashMap <String,String> payload = new HashMap<String,String>();
        payload.put("firstname",firstName);
        payload.put("lastname",lastName);
        Response response = RestAssured.given().spec(requestSpecification)
                .header("Cookie","token="+loginAndCreateToken())
                .body(payload)
                .when().patch("/booking/"+bookingId)
                .then().log().all()
                .body("lastname",equalTo(lastName),
                        "depositpaid",equalTo(true))
                .extract().response();

    }

}
