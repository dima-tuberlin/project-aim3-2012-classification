package de.tuberlin.dima.aim3.oc.input.type;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WikiRevision {

  WikiPage page;

  String id;

  String timestamp;

  WikiUser user;

  Boolean minorChange;

  String comment;

  Integer textLength;

  public WikiRevision() {
    super();
  }

  public WikiRevision(String pageId, String pageTitle, String id,
      String timestamp, WikiUser user, Boolean minorChange, String comment,
      String text) {
    this(pageId, pageTitle, id, timestamp, user, minorChange, comment,
        text == null ? 0 : text.length());
  }

  public WikiRevision(String pageId, String pageTitle, String id,
      String timestamp, WikiUser user, Boolean minorChange, String comment,
      Integer textLength) {
    super();
    page = new WikiPage(pageTitle, pageId);
    this.id = id;
    this.timestamp = timestamp;
    this.user = user;
    this.minorChange = minorChange;
    this.comment = comment;
    this.textLength = textLength;
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

  public WikiPage getPage() {
    return page;
  }

  public String getId() {
    return id;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public WikiUser getUser() {
    return user;
  }

  public Boolean getMinorChange() {
    return minorChange;
  }

  public String getComment() {
    return comment;
  }

  public Integer getTextLength() {
    return textLength;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public void setUser(WikiUser user) {
    this.user = user;
  }

  public void setMinorChange(Boolean minorChange) {
    this.minorChange = minorChange;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setTextLength(Integer textLength) {
    this.textLength = textLength;
  }

}
