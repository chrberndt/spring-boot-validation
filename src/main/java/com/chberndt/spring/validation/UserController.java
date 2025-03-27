package com.chberndt.spring.validation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Christian Berndt
 */
@RestController
@RequestMapping(value = "/users")
public class UserController {

  private final UserRepository repository;

  public UserController(UserRepository userRepository) {
    this.repository = userRepository;
  }

  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody User user,
      UriComponentsBuilder uriComponentsBuilder) {

    User createdUser = repository.save(user);
    URI location = uriComponentsBuilder.path("/users/{id}").buildAndExpand(createdUser.getId())
        .toUri();

    return ResponseEntity.created(location).build();
  }

}
