package com.chberndt.spring.validation;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Christian Berndt
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException() {

    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problemDetail.setDetail("Unique index or primary key violation");

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problemDetail.setProperty("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
    problemDetail.setProperty("errors", ex.getBindingResult().getAllErrors());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

}
