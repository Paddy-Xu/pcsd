package com.acertainbookstore.client;

class ServerWorkload implements Comparable<ServerWorkload> {

  private final String server;
  private volatile Integer load = 0;

  ServerWorkload(String server) {
    this.server = server;
  }

  String getServer() {
    return server;
  }

  Integer getLoad() {
    return load;
  }

  void increment() {
    ++load;
  }

  void decrement() {
    --load;
  }

  public int hashCode() {
    return server.hashCode();
  }

  public boolean equals(Object other) {
    return load.equals(((ServerWorkload)other).getLoad());
  }

  public int compareTo(ServerWorkload other) {
    return load.compareTo(other.getLoad());
  }

}
