package org.example.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BusinessUtils implements ApplicationConstants {

    protected static final Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();


    public static JSONObject readJson(String filename) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(new FileReader(filename));  //path to the JSON file.
        String json = data.toJSONString();
        return data;
    }

    public static String randomAlphaNumeric(int count) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJ3456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
    public static String randomString(int count){
        // create a string of all characters
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        // create random string builder
        StringBuilder sb = new StringBuilder();
        // create an object of Random class
        Random random = new Random();
        // specify length of random string
        int length = count;
        for(int i = 0; i < length; i++) {
            // generate random index number
            int index = random.nextInt(alphabet.length());
            // get character specified by index
            // from the string
            char randomChar = alphabet.charAt(index);
            // append the character to string builder
            sb.append(randomChar);
        }
        String randomString = sb.toString();
        return randomString;
    }
    public static String randomNumber(int count) {
        String ALPHA_NUMERIC_STRING = "1234567890";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String modifyJson(Map<String, String> map, String fileName) throws Exception {
        JSONObject payload = BusinessUtils.readJson(fileName);
        String originalJson = payload.toString();
        for (Map.Entry m : map.entrySet()) {
            JsonNode updatedJson = JsonPath.using(configuration).parse(originalJson).set("$." + m.getKey(), m.getValue()).json();
            originalJson = updatedJson.toString();
        }
        return originalJson;
    }

    public static String getJsonValueOfAObject(Object object) throws Exception {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);

    }

    public static String readConfigurationFile(String key) throws NoSuchFieldException {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(System.getProperty("user.dir") + "/src/test/resources/Config.properties"));
            String value = properties.getProperty(key).trim();

            return value;
        } catch (Exception e) {
            throw new NoSuchFieldException("Cannot find key: " + key + " in Config file.");
        }
    }

    public static String encodeToBase64(String text) {
        byte[] encoded = Base64.getEncoder().encode(text.getBytes());
        return new String(encoded);
    }

    public static String decodeFromBase64(String text) {
        byte[] decoded = Base64.getDecoder().decode(text.getBytes());
        return new String(decoded);
    }

    public String getDateTimeStamp() {
        // TODO: Add the UTC date format
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DDzHH:MM:SS");
        String strDate = dateFormat.format(new Date());
        return strDate;
    }
}
