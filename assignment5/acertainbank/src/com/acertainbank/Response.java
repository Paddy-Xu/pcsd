package com.acertainbank;

public class Response {

  private Exception error = null;

  public Response() {}

  public Response(Exception error) {
    this.error = error;
  }

  public boolean wasSuccessful() {
    return error != null;
  }

  public Exception getError() {
    return error;
  }

  public void setError(Exception error) {
    this.error = error;
  }

}
