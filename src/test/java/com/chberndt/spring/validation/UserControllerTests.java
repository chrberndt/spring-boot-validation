package com.chberndt.spring.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.util.LinkedHashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author Christian Berndt
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

  private static final JSONObject emptyUser = new JSONObject();
  private static final JSONObject invalidUser = new JSONObject();
  private static final JSONObject validUser = new JSONObject();

  @Autowired
  TestRestTemplate restTemplate;

  @BeforeAll
  public static void setup() {

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

    assertThat((String) documentContext.read("$.detail")).isEqualTo("Failed to read request");
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

    assertThat((String) documentContext.read("$.detail")).isEqualTo(
        "Unique index or primary key violation");
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

    assertThat((String) documentContext.read("$.detail")).isEqualTo(
        "Unique index or primary key violation");
  }

  @Test
  public void shouldNotCreateUserWithInvalidFields() throws Exception {

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

    HttpEntity<String> httpEntity = new HttpEntity<>(invalidUser.toString(), headers);
    ResponseEntity<String> response = restTemplate.postForEntity("/users", httpEntity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    DocumentContext documentContext = JsonPath.parse(response.getBody());

    assertThat((String) documentContext.read("$.error")).isEqualTo(
        HttpStatus.BAD_REQUEST.getReasonPhrase());

    JSONArray errors = documentContext.read("$.errors");

    assertThat(errors.size()).isEqualTo(2);

    LinkedHashMap<String, Object> emailFieldError = getFieldError(errors, "email");

    assertThat(emailFieldError).isNotNull();
    assertThat(emailFieldError.get("defaultMessage")).isEqualTo(
        "must be a well-formed email address");

    LinkedHashMap<String, Object> userNameFieldError = getFieldError(errors, "userName");

    assertThat(userNameFieldError).isNotNull();
    assertThat(userNameFieldError.get("defaultMessage")).isEqualTo("must match \"^[a-zA-Z0-9]*$\"");
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

    JSONArray errors = documentContext.read("$.errors");

    assertThat(errors.size()).isEqualTo(2);

    LinkedHashMap<String, Object> emailFieldError = getFieldError(errors, "email");

    assertThat(emailFieldError).isNotNull();
    assertThat(emailFieldError.get("defaultMessage")).isEqualTo("must not be empty");

    LinkedHashMap<String, Object> userNameFieldError = getFieldError(errors, "userName");

    assertThat(userNameFieldError).isNotNull();
    assertThat(userNameFieldError.get("defaultMessage")).isEqualTo("must not be empty");
  }

  private LinkedHashMap<String, Object> getFieldError(JSONArray errors, String field) {
    return errors.stream().filter(LinkedHashMap.class::isInstance).map(LinkedHashMap.class::cast)
        .filter(map -> map.get("field").equals(field)).findFirst().orElse(null);
  }

}
