package org.example.framework.utils;

import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class BaseTest<tenantId> extends BusinessUtils {

    protected static String BASE_URL;
    protected static String BASE_DOMAIN_URL;
    protected static String BASE_CN_URL;
    protected static String AUTH_HEADER_TOKEN;
    protected static HashMap<String, String> headers;
    protected static HashMap<String, String> headers_CN;
    protected static Properties properties;
    protected static RequestSpecification requestSpec;
    protected static RequestSpecification requestSpecCN;
    protected static String PROFILE_SERVICE_BASE_URI;
    protected static String FEED_SERVICE_BASE_URI;
    protected static String SUBSCRIPTION_SERVICE_BASE_URI;
    protected static String THEMES_SERVICE_BASE_URI;
    protected static String INTERACTION_SERVICE_BASE_URI;
    protected static List<String> validTenants = Arrays.asList("beaute");
    protected static List<String> invalidTenants = Arrays.asList("luxury3", "beautee");
    protected static String invalidTenant = "beautee";

    protected static List<String> valid_pin_codes = Arrays.asList("110030", "844100", "713100");
    protected static List<String> blank_pin_code = Arrays.asList("");
    protected static List<String> similar_products_recommendations = Arrays.asList("MP000000007281680");
    protected static List<String> cross_category_products_recommendations = Arrays.asList("MP111000007903434");
    protected static String TENANT_ID;

    public static void setup(String service) throws NoSuchFieldException {
        TENANT_ID = "beaute";
        String env = System.getProperty("env");
        setEnvironment(env, service);
        System.out.println("environment: " + env);
    }
    public static void setupCN() throws NoSuchFieldException {
        TENANT_ID = "beaute";
        String env = System.getProperty("env");
        if (env.equalsIgnoreCase("Dev")) {
            setCNRequestSpec(CN_DEV);
            BASE_CN_URL = CN_DEV;
        }
        else if(env.equalsIgnoreCase("QA")){
            setCNRequestSpec(CN_QA);
            BASE_CN_URL=CN_QA;
        }
        System.out.println("environment: " + env);
    }

    public static void setEnvironment(String env, String service) throws NoSuchFieldException {
        String[] domain = {"subscription", "interaction", "feed", "theme", "profile", "recommendation", "search"};
        List<String> domainList = new ArrayList<>(Arrays.asList(domain));
        if (env.equalsIgnoreCase("Local")) {
            BASE_URL = "http://localhost:3000";
            PROFILE_SERVICE_BASE_URI = "http://localhost:8080";
            THEMES_SERVICE_BASE_URI = "http://localhost:8082";
            FEED_SERVICE_BASE_URI = "http://localhost:8084";
            SUBSCRIPTION_SERVICE_BASE_URI = "http://localhost:8080";
            INTERACTION_SERVICE_BASE_URI = "http://localhost:8083";
        } else if (env.equalsIgnoreCase("Dev")) {
            setRequestSpec(DEV_URL);
            BASE_URL = DEV_URL;
            BASE_DOMAIN_URL = DEV_URL;
        } else if (env.equalsIgnoreCase("QA") && domainList.contains(service)) {
            setRequestSpec(QA_DOMAIN_URL);
            BASE_DOMAIN_URL = QA_DOMAIN_URL;
        } else if (env.equalsIgnoreCase("QA") && !domainList.contains(service)) {
            setRequestSpec(QA_AGGREGATOR_URL);
            BASE_DOMAIN_URL = QA_DOMAIN_URL;
        }
        System.out.println("Domain URL:: " + BASE_DOMAIN_URL + " Aggregator URL:: " + BASE_URL);

    }
    protected static void setRequestSpec(String url) throws NoSuchFieldException {
        BASE_URL = url;
        headers = getHeadersWithoutAuthWithTenant();
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.socket.timeout", 70000)
                        .setParam("http.connection.timeout", 50000)
                        .setParam("https.protocols", "TLSv1.3")
                        .setParam("https.protocols", "SSLv3")
                        .setParam("jdk.tls.client.cipherSuites", "TLS_RSA_WITH_AES_128_CBC_SHA256"));
        System.setProperty("https.protocols", "TLSv1.3");
        System.setProperty("https.protocols", "SSLv3");
        System.setProperty("jdk.tls.client.cipherSuites", "TLS_RSA_WITH_AES_128_CBC_SHA256");
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL).addHeaders(headers).setContentType(ContentType.JSON).setAccept(ContentType.JSON)
//                    .setConfig(RestAssuredConfig.config().connectionConfig(
//                            ConnectionConfig.connectionConfig().closeIdleConnectionsAfterEachResponse()))
                .build();
        System.out.println("Request spec " + requestSpec);
    }
    protected static void setCNRequestSpec(String url) throws NoSuchFieldException{
        BASE_CN_URL = url;
        headers_CN = getHeadersForCN();
        requestSpecCN = new RequestSpecBuilder()
                .setBaseUri(BASE_CN_URL).addHeaders(headers_CN).setContentType(ContentType.JSON).setAccept(ContentType.JSON).build();
        System.out.println("Request spec " + requestSpecCN);
    }
    public static HashMap<String,String> getHeadersForCN(){
        HashMap<String, String> map = new HashMap<>();
        String accessToken = System.getProperty("accessToken");
        map.put("X-Contentful-Webhook-Name","publish-post-devenv");
        map.put("access-token",accessToken);
        map.put("Content-Type","application/json");
        map.put("x-tenant-id", TENANT_ID);
        return map;
    }
    public static HashMap<String, String> getHeaders() throws NoSuchFieldException {
        HashMap<String, String> map = new HashMap<>();
        map.put("x-tenant-id", TENANT_ID);
        return map;
    }

    public static HashMap<String, String> getHeadersWithTenantId(String tenant) throws NoSuchFieldException {
        HashMap<String, String> map = new HashMap<>();
        map.put("x-tenant-id", tenant);
        return map;
    }

    public static HashMap<String, String> getHeadersWithoutAuth() throws NoSuchFieldException {
        HashMap<String, String> map = new HashMap<>();
        map.put("x-beaute-request-id", UUID.randomUUID().toString());
        return map;
    }

    public static HashMap<String, String> getHeadersWithoutAuthWithTenant() throws NoSuchFieldException {
        HashMap<String, String> map = new HashMap<>();
        map.put("x-beaute-request-id", UUID.randomUUID().toString());
        map.put("x-tenant-id", TENANT_ID);
        return map;
    }

    /**
     * generates global token
     *
     * @returns global token to generate access token
     */

    public static String generateGlobalToken() {
        String clientId = "gauravj@dewsolutions.in";
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("client_secret", "secret");
        queryParams.put("client_id", clientId);
        queryParams.put("grant_type", "client_credentials");
        queryParams.put("isPwa", "true");
        Response response = given().log().all()
                .contentType(ContentType.JSON)
                .when()
                .queryParams(queryParams)
                .post(GLOBAL_ACCESS_TOKEN_URI)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_OK).and().extract().response();
        return response.jsonPath().get("access_token");
    }

    /**
     * generates access token
     *
     * @return accesstoken
     */
    public static String generateAccessToken() {
  String userName = System.getProperty("qaUser");
  String clientId = System.getProperty("testClient");
  String password = System.getProperty("qaPassword");
        HashMap<String, String> queryParams = new HashMap<>();
        HashMap<String, String> formParams = new HashMap<>();
        queryParams.put("client_secret", "secret");
        queryParams.put("client_id", clientId);
        queryParams.put("grant_type", "password");
        queryParams.put("access_token", generateGlobalToken());
        formParams.put("username", userName);
        formParams.put("password", password);
        Response response = given().log().all()
                .config(RestAssured.config().encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.MULTIPART)))
                .formParams(formParams)
                .when()
                .queryParams(queryParams)
                .post(USER_ACCESS_TOKEN_URI)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_OK).and().extract().response();
        return response.jsonPath().get("access_token");
    }
    public static String generateAccessToken(String email) {
        String userName = email;
        String clientId = System.getProperty("testClient");
        String password = System.getProperty("qaPassword");
        HashMap<String, String> queryParams = new HashMap<>();
        HashMap<String, String> formParams = new HashMap<>();
        queryParams.put("client_secret", "secret");
        queryParams.put("client_id", clientId);
        queryParams.put("grant_type", "password");
        queryParams.put("access_token", generateGlobalToken());
        formParams.put("username", userName);
        formParams.put("password", password);
        Response response = given().log().all()
                .config(RestAssured.config().encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.MULTIPART)))
                .formParams(formParams)
                .when()
                .queryParams(queryParams)
                .post(USER_ACCESS_TOKEN_URI)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_OK).and().extract().response();
        return response.jsonPath().get("access_token");
    }
    public static HashMap<String, String> getAuthTokenWithTUL(String email) {
        HashMap<String, String> accessToken = new HashMap<>();
        //accessToken.put("x-tenant-id",BEAUTE_TENANT);
        accessToken.put("Authorization", "Bearer " + generateAccessToken(email));
        return accessToken;
    }

    public static HashMap<String, String> getAuthTokenWithTUL() {
        HashMap<String, String> accessToken = new HashMap<>();
        //accessToken.put("x-tenant-id",BEAUTE_TENANT);
        accessToken.put("Authorization", "Bearer " + generateAccessToken());
        return accessToken;
    }

    public static HashMap<String, String> getAuthTokenWithTULAndTenantId(String tenant) {
        HashMap<String, String> accessToken = new HashMap<>();
        accessToken.put("Authorization", "Bearer " + generateAccessToken());
        accessToken.put("x-tenant-id", tenant);
        return accessToken;
    }

    public static HashMap<String, String> getAuthTokenWithTULTenantId() {
        HashMap<String, String> accessToken = new HashMap<>();
        accessToken.put("Authorization", "Bearer " + generateAccessToken());
        accessToken.put("x-tenant-id", TENANT_ID);
        return accessToken;
    }

    public static HashMap<String, String> getInvalidAuthToken() {
        HashMap<String, String> accessToken = new HashMap<>();
//        accessToken.put("x-tenant-id",BEAUTE_TENANT);
        accessToken.put("Authorization", "Bearer " + UUID.randomUUID());
        return accessToken;
    }

    public static HashMap<String, String> getInvalidAuthTokenWithTenant() {
        HashMap<String, String> accessToken = new HashMap<>();
        accessToken.put("x-tenant-id", BEAUTE_TENANT);
        accessToken.put("Authorization", "Bearer " + UUID.randomUUID());
        return accessToken;
    }
    public static HashMap<String, String> getInvalidToken() {
        HashMap<String, String> accessToken = new HashMap<>();
        accessToken.put("x-tenant-id", BEAUTE_TENANT);
        accessToken.put("Authorization", "abchd");
        return accessToken;
    }

    public static String getProfileIdWithHeader(HashMap<String, String> accessToken) {
        Response response = given().log().all()
                .headers(accessToken)
                .contentType(ContentType.JSON)
                .when()
                .get(GET_USER_ID)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_OK).and().extract().response();
        return response.jsonPath().get("uid");
    }

    public void callGet(String URL, Map headers, int statusCode) {
        System.out.println("-----------------------***GET CALL***-----------------------");
        Response response = given().log().all()
                .headers(headers).spec(requestSpec)
                .contentType(ContentType.JSON)
                .when()
                .get(URL)
                .then().log().all()
                .assertThat().statusCode(statusCode).and().extract().response();
    }


    public void callGetWithQueryParam(String URL, Map headers, int statusCode, String queryKey, String queryValue) {
        System.out.println("-----------------------***GET CALL***-----------------------");
        Response response = given().log().all()
                .headers(headers)
                .contentType(ContentType.JSON)
                .queryParam(queryKey, queryValue)
                .when()
                .get(URL)
                .then().log().all()
                .assertThat().statusCode(statusCode).and().extract().response();
    }

    public void callGetWithQueryParams(String URL, Map headers, int statusCode, Map<String, String> map) {
        System.out.println("-----------------------***GET CALL***-----------------------");
        Response response = given().log().all()
                .headers(headers)
                .contentType(ContentType.JSON)
                .queryParams(map)
                .when()
                .get(URL)
                .then().log().all()
                .assertThat().statusCode(statusCode).and().extract().response();
    }

    public String callPostAndGetResponse(String URL, Map headers, String payload, int statusCode) {
        System.out.println("-----------------------***POST CALL***-----------------------");
        Response response = (Response) given().log().all()
                .headers(headers)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(URL).then().log().all().assertThat().statusCode(statusCode).extract().response().body();
        System.out.println(response.getContentType());
        System.out.println("Headers :: " + response.getHeaders());

        return response.jsonPath().get("id");
    }

    public Response callPost(String URL, Map headers, String payload, int statusCode) {
        System.out.println("-----------------------***POST CALL***-----------------------");
        Response response = (Response) given().log().all()
                .headers(headers).spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(URL).then().log().all().assertThat().statusCode(statusCode).extract().response().body();
        return response;
    }

    public String callPostWithQueryParam(String URL, Map headers, String payload, int statusCode, Map<String, String> queryparam) {
        System.out.println("-----------------------***POST CALL***-----------------------");
        Response response = (Response) given().log().all()
                .headers(headers)
                .contentType(ContentType.JSON)
                .body(payload)
                .queryParams(queryparam)
                .when()
                .post(URL).then().log().all().assertThat().statusCode(statusCode).extract().response().body();
        String responsebody = response.getBody().toString();
        return responsebody;
    }

    public void callPut(String URL, Map headers, String payload, int statusCode) {
        System.out.println("-----------------------***PUT CALL***-----------------------");
        Response response = (Response) given().log().all()
                .headers(headers)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(URL).then().log().all().assertThat().statusCode(statusCode).extract().response().body();
    }

    public void callDelete(String URL, Map headers, int statusCode) {
        System.out.println("-----------------------***DELETE CALL***-----------------------");
        Response response = (Response) given().log().all()
                .headers(headers)
                .contentType(ContentType.JSON)
                .when()
                .delete(URL).then().log().all().assertThat().statusCode(statusCode).extract().response().body();
    }

    public void callPutWithQueryParams(String URL, Map headers, String payload, Map queryParams, int statusCode) {
        System.out.println("-----------------------***PUT CALL WITH QUERY PARAMS***-----------------------");
        Response response = (Response) given().log().all()
                .headers(headers)
                .contentType(ContentType.JSON).queryParams(queryParams)
                .when().body(payload)
                .put(URL).then().log().all().assertThat().statusCode(statusCode).extract().response().body();
    }

    protected String getParameter(Response response, String parameter, String arrayName) {
        //convert response body to string
        String strResponse = response.asString();
        //JSON Representation from Response Body
        JsonPath jsonPath = response.jsonPath();
        //Get value of Location Key
        ArrayList<Map<String, String>> results = jsonPath.get(arrayName);
        //System.out.println(l);
        // verify the value of key
        String result = results.get(0).get(parameter);
        return result;
    }

    protected String getResultFromArray(Response response, String arrayName) {
        //convert response body to string
        String strResponse = response.asString();
        //JSON Representation from Response Body
        JsonPath jsonPath = response.jsonPath();
        //Get value of Location Key
        ArrayList<String> results = jsonPath.get(arrayName);
        //System.out.println(l);
        // verify the value of key
        String result = results.get(0);
        return result;
    }

    protected String getPostIdAndProfileIds() {
        //to be used for profile ids and post ids
        String ids = BusinessUtils.randomAlphaNumeric(4);
        return ids;
    }
    protected void assertForArrayValues(Response response, String key,String val, String arrayName) {
        String result = getParameter(response, key, arrayName);
        Assert.assertTrue(result.equalsIgnoreCase(val));
    }

    protected void assertForInvalidErrorResponse(Response response, String s, String arrayName) {
        String result = getParameter(response, "message", arrayName);
        Assert.assertTrue(result.equalsIgnoreCase(s));
    }

    protected void assertForValidResponse(Response response, String s, String arrayName) {
        String result = getResultFromArray(response, arrayName);
        Assert.assertTrue(result.startsWith(s));
    }

    protected static void assertForValidSimpleArrayResponse(Response response, String key, String val) {
        //JSON Representation from Response Body
        JsonPath jsonPath = response.jsonPath();
        //Get value of Location Key
        String result = jsonPath.get(key);
        Assert.assertTrue(result.equalsIgnoreCase(val));
    }

   /* protected static String createFollowId(String url, String profileId, String resourceId) throws NoSuchFieldException {
        JsonObject payload = new JsonObject();
        payload.addProperty(PROFILE_ID, profileId);
        payload.addProperty(RESOURCE_ID, resourceId);
        if (url == "profile")
            url = URI_FOLLOW_PROFILE;
        else url = URI_FOLLOW_HASHTAG;
        given().log().all()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(payload.toString())
                .post(url)
                .then().log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESULT, equalTo(FOLLOWED));
        ;
        return profileId + ":" + resourceId;
    }*/

    protected JSONArray parseArrayOfJsonObjectsFromResults(String response, String arrayName) {
        // creating JSONObject
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonarray = jsonObject.getJSONArray(arrayName);
        return jsonarray;

    }
    protected static String getRandomString(int length) {
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

}
