package com.acertainbank;

public class Response {

  private Exception error = null;
  private String content;

  public Response() {
    content = "";
  }

  public Response(String content) {
    this.content = content;
  }

  public boolean wasSuccessful() {
    return error != null;
  }

  public String getContent() {
    return content;
  }

  public Exception getError() {
    return error;
  }

  public void setContent(String content) {
    this.content = content;
  }
  
  public void setError(Exception error) {
    this.error = error;
  }

}
