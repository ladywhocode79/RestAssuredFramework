package org.example;


import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


import static io.restassured.RestAssured.given;

public class AuthToken {
    public  String authToken;
    public static void main(String[] args) {

        String payload="{\n" +
                "    \"username\" : \"admin\",\n" +
                "    \"password\" : \"password123\"\n" +
                "}";
       Response response = given()
        .baseUri("https://restful-booker.herokuapp.com")
               .contentType("application/json")
               .body(payload)
                .when()
               .post("/auth")
                .then().log().all()
               .extract().response();
       String token = response.jsonPath().getString("token");
        System.out.println("Token: "+token);
    }
}
