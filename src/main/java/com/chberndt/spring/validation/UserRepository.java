package com.chberndt.spring.validation;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Christian Berndt
 */
public interface UserRepository extends CrudRepository<User, Long> {
}
