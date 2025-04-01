package com.chberndt.spring.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

  private static final JSONObject emptyUser = new JSONObject();
  private static final JSONObject invalidUser = new JSONObject();
  private static final JSONObject validUser = new JSONObject();
  @Autowired
  TestRestTemplate restTemplate;

  @BeforeAll
  public static void setup() throws JSONException {

    invalidUser.put("email", "invalid");
    invalidUser.put("userName", "inval!d");

    validUser.put("email", "alice@example.com");
    validUser.put("userName", "alice");
  }

  @Test
  public void shouldNotCreateUserWithEmptyRequestBody() {

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
    ResponseEntity<String> response = restTemplate.postForEntity("/users", httpEntity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    DocumentContext documentContext = JsonPath.parse(response.getBody());

    assertThat((String) documentContext.read("$.detail"))
        .isEqualTo("Failed to read request");
  }

  @Test
  public void shouldCreateUserWithValidFields() {

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(validUser.toString(), headers);
    ResponseEntity<String> response = restTemplate.postForEntity("/users", httpEntity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    URI location = response.getHeaders().getLocation();
    assertThat(location).isNotNull();
    assertThat(location.getPath()).isEqualTo("/users/1");

  }

  @Test
  public void shouldNotCreateUserWithExistingEmail() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(validUser.toString(), headers);
    restTemplate.postForEntity("/users", httpEntity, String.class);

    ResponseEntity<String> response = restTemplate.postForEntity("/users", httpEntity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    DocumentContext documentContext = JsonPath.parse(response.getBody());

    assertThat((String)documentContext.read("$.detail"))
        .isEqualTo("Unique index or primary key violation");
  }

  @Test
  public void shouldNotCreateUserWithExistingUserName() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(validUser.toString(), headers);
    restTemplate.postForEntity("/users", httpEntity, String.class);

    ResponseEntity<String> response = restTemplate.postForEntity("/users", httpEntity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    DocumentContext documentContext = JsonPath.parse(response.getBody());

    assertThat((String)documentContext.read("$.detail"))
        .isEqualTo("Unique index or primary key violation");
  }

  @Test
  public void shouldNotCreateUserWithInvalidFields() {

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(invalidUser.toString(), headers);
    ResponseEntity<String> response = restTemplate.postForEntity("/users", httpEntity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    DocumentContext documentContext = JsonPath.parse(response.getBody());

    assertThat((String) documentContext.read("$.error")).isEqualTo(
        HttpStatus.BAD_REQUEST.getReasonPhrase());

    assertThat((Integer) documentContext.read("$.errors.length()"))
        .isEqualTo(2);
    assertThat(documentContext.read("$.errors[?(@.field == 'email')].defaultMessage")
        .toString())
        .isEqualTo("[\"must be a well-formed email address\"]");
    assertThat(documentContext.read("$.errors[?(@.field == 'userName')].defaultMessage")
        .toString())
        .isEqualTo("[\"must match \\\"^[a-zA-Z0-9]*$\\\"\"]");
  }

  @Test
  public void shouldNotCreateUserWithoutRequiredFields() {

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(emptyUser.toString(), headers);
    ResponseEntity<String> response = restTemplate.postForEntity("/users", httpEntity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    DocumentContext documentContext = JsonPath.parse(response.getBody());

    assertThat((String) documentContext.read("$.error")).isEqualTo(
        HttpStatus.BAD_REQUEST.getReasonPhrase());

    assertThat((Integer) documentContext.read("$.errors.length()"))
        .isEqualTo(2);
    assertThat(documentContext.read("$.errors[?(@.field == 'email')].defaultMessage")
        .toString())
        .isEqualTo("[\"must not be empty\"]");
    assertThat(documentContext.read("$.errors[?(@.field == 'userName')].defaultMessage")
        .toString())
        .isEqualTo("[\"must not be empty\"]");
  }

}
