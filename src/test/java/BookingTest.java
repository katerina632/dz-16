import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class BookingTest {
    private static String token;

    private RequestSpecification specification2;
    private RequestSpecification specification3;
    private RequestSpecification specification4;

    @BeforeMethod
    public void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader("Content-Type", "application/json")
                .build();

        createToken();

        specification2 = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .build();

        specification3 = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addCookie("token=" + token)
                .addHeader("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
                .build();

        specification4 = new RequestSpecBuilder()
                .addCookie("token=" + token)
                .addHeader("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
                .build();
    }

    public void createToken() {
        CreateTokenBody tokenBody = new CreateTokenBody()
                .builder()
                .username("admin")
                .password("password123")
                .build();

        Response response = RestAssured.given()
                .body(tokenBody)
                .post("/auth");
        token = response.then().extract().jsonPath().get("token");

    }

    @Test(description = "Checks creating a booking.")
    public void testCreateBookingPositive() {

        BookingDate bookingDate = new BookingDate()
                .builder()
                .checkin("2013-07-19")
                .checkout("2013-08-19")
                .build();

        CreateBookingBody bookingBody = new CreateBookingBody()
                .builder()
                .firstName("Bob")
                .lastName("Black")
                .totalPrice(1000)
                .depositPaid(true)
                .bookingDates(bookingDate)
                .additionalNeeds("Wi-Fi")
                .build();

        Response newBookingResponse = RestAssured.given()
                .spec(specification2)
                .log()
                .all()
                .body(bookingBody)
                .post("/booking");

        newBookingResponse.prettyPrint();

        newBookingResponse.as(ResponseBooking.class);
        newBookingResponse.then().statusCode(200);
        newBookingResponse.then().body("bookingid", notNullValue());
    }

    @Test(description = "Checks getting all booking Ids.")
    public void testGetBookingIdsPositive() {

        Response response = RestAssured.given()
                .log()
                .all()
                .get("/booking");

        response.then().statusCode(200).body("bookingid", notNullValue());
    }

    @Test(description = "Checks partial updating of the booking by Id.")
    public void testPartialUpdateBookingPositive() {
        Integer expectedNewTotalPrice = 150;
        Integer idBooking = RestAssured.given().get("/booking").then().extract().jsonPath().getInt("bookingid[0]");
        JSONObject newTotalPrice = new JSONObject() {{
            put("totalprice", expectedNewTotalPrice);
        }};

        Response response = RestAssured.given()
                .spec(specification3)
                .log()
                .all()
                .body(newTotalPrice.toString())
                .patch("booking/{id}", idBooking);

        response.then().statusCode(200).body("totalprice", equalTo(expectedNewTotalPrice));
    }

    @Test(description = "Checks updating of the booking by Id.")
    public void testUpdateBookingPositive() {
        Integer idBooking = RestAssured.given().get("/booking").then().extract().jsonPath().getInt("bookingid[1]");
        String newExpectedFirstName = "Ben";
        String newExpectedAdditionalNeeds = "None";
        String actualFirstName, actualAdditionalNeeds;
        SoftAssert softAssert = new SoftAssert();

        Response responseBooking = RestAssured.given()
                .spec(specification2)
                .get("booking/{id}", idBooking);

        CreateBookingBody body = responseBooking.then().extract().as(CreateBookingBody.class);
        body.setFirstName(newExpectedFirstName);
        body.setAdditionalNeeds(newExpectedAdditionalNeeds);

        Response response = RestAssured.given()
                .spec(specification3)
                .body(body)
                .put("booking/{id}", idBooking);

        response.then().statusCode(200);

        actualFirstName = response.then().extract().as(CreateBookingBody.class).getFirstName();
        actualAdditionalNeeds = response.then().extract().as(CreateBookingBody.class).getAdditionalNeeds();

        softAssert.assertEquals(actualFirstName, newExpectedFirstName);
        softAssert.assertEquals(actualAdditionalNeeds, newExpectedAdditionalNeeds);
    }

    @Test(description = "Checks deleting of the booking by Id.")
    public void testDeleteBookingPositive() {
        Integer idBooking = RestAssured.given().get("/booking").then().extract().jsonPath().getInt("bookingid[3]");

        Response response = RestAssured.given()
                .spec(specification4)
                .delete("/booking/{id}", idBooking);

        response.then().statusCode(201);
    }
}


