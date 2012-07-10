package de.tuberlin.dima.aim3.oc.input.type;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WikiUser {

  String username;

  String userid;

  String userip;

  public WikiUser() {
    super();
  }

  public WikiUser(String username, String userid, String userip) {
    super();
    this.username = username;
    this.userid = userid;
    this.userip = userip;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public boolean isRegisteredUser() {
    return getUserid() != null;
  }

  public String getUsername() {
    return username;
  }

  public String getUserid() {
    return userid;
  }

  public String getUserip() {
    return userip;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  public void setUserip(String userip) {
    this.userip = userip;
  }

}
