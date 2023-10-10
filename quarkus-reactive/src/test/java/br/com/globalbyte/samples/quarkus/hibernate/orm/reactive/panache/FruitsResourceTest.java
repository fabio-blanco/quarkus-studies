package br.com.globalbyte.samples.quarkus.hibernate.orm.reactive.panache;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.text.IsEmptyString.emptyString;

//NOTE: I had to force the order of tests because I didn't find a way to force a test method to rollback
//      It was causing interferences on other tests running after the tests that changed the database

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FruitsResourceTest {

    @Test
    @Order(1)
    @DisplayName("Test list all fruits")
    public void testListAllFruits() {

        //List all, should have all 3 fruits the database has initially:
        Response response = given()
                .when()
                .get("/fruits")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().response();
        assertThat(response.jsonPath().getList("name")).containsExactlyInAnyOrder("Cherry", "Apple", "Banana");

    }

    @Test
    @Order(2)
    @DisplayName("Test get fruit by its id")
    public void testGetFruit() {
        given().when()
                .get("/fruits/2")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body(containsString("\"id\":2"),
                        containsString("\"name\":\"Apple\""));
    }

    @Test
    @Order(3)
    @DisplayName("Test create new fruit")
    public void testCreatNewFruit() {
        given().when()
                .body("{\"name\" : \"Avocado\"}")
                .contentType("application/json")
                .post("/fruits")
                .then()
                .statusCode(201)
                .body(containsString("\"id\":"),
                        containsString("\"name\":\"Avocado\""));
    }

    @Test
    @Order(4)
    @DisplayName("When trying to create new fruit, if duplicate then should return 422")
    public void testCreateDuplicateFruit() {
        given().when()
                .body("{\"name\" : \"Banana\"}")
                .contentType("application/json")
                .post("/fruits")
                .then()
                .statusCode(422)
                .body(containsString("duplicate key value violates unique constraint"));
    }

    @Test
    @Order(4)
    @DisplayName("When trying to create new fruit with id of existing fruit then should return 422")
    public void testCreateFruitWithExistingId() {
        given().when()
                .body("{\"id\":1, \"name\" : \"Mango\"}")
                .contentType("application/json")
                .post("/fruits")
                .then()
                .statusCode(422)
                .body(containsString("detached entity passed to persist"));
    }

    @Test
    @Order(6)
    @DisplayName("Test update a fruit")
    public void testUpdateFruit() {
        given().when()
                .body("{\"name\" : \"Pineapple\"}")
                .contentType("application/json")
                .put("/fruits/1")
                .then()
                .statusCode(200)
                .body(containsString("\"id\":"),
                        containsString("\"name\":\"Pineapple\""));

        given().when()
                .get("/fruits/1")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body(containsString("\"id\":1"),
                        containsString("\"name\":\"Pineapple\""));
    }

    @Test
    @Order(7)
    @DisplayName("Test entity not found for update")
    public void testEntityNotFoundForUpdate() {
        given().when()
                .body("{\"name\" : \"Watermelon\"}")
                .contentType("application/json")
                .put("/fruits/32432")
                .then()
                .statusCode(404)
                .body(emptyString());
    }

    @Test
    @Order(8)
    @DisplayName("When empty body on update then should return 422")
    public void testEmptyBodyUpdate() {
        given().when()
                .put("/fruits/1")
                .then()
                .statusCode(422)
                .body(containsString("Fruit name was not set on request."));
    }

    @Test
    @Order(9)
    @DisplayName("Test delete one fruit")
    public void testDeleteFruit() {
        given().when()
                .delete("/fruits/4").then()
                .statusCode(204)
                .body(emptyString());
    }

    @Test
    @Order(10)
    @DisplayName("Test entity not fount for delete")
    public void testEntityNotFoundForDelete() {
        given().when()
                .delete("/fruits/9236")
                .then()
                .statusCode(404)
                .body(emptyString());
    }

}
