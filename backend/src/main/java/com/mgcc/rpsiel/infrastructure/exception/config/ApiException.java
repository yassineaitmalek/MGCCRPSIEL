package com.mgcc.rpsiel.infrastructure.exception.config;

public class ApiException extends RuntimeException {

  /**
   * @param message
   */
  public ApiException(String message) {
    super(message);

  }

  /**
   * @param message
   * @param cause
   */
  public ApiException(String message, Throwable cause) {
    super(message, cause);

  }

  public static void reThrow(Throwable throwable) {
    throw new ApiException(throwable.getMessage(), throwable);
  }

  public static ApiException of(Throwable throwable) {
    return new ApiException(throwable.getMessage(), throwable);
  }

  public static ApiException of(String message, Throwable cause) {
    return new ApiException(message, cause);
  }

  public static ApiException of(String message) {
    return new ApiException(message);
  }

}
