package de.tuberlin.dima.aim3.oc.stream.type;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class WikiEdit {

  public enum RevisionChange {
    SAMEUSER_CONTENTCHANGE, SAMEUSER_CONTENTREVERT, DIFFERENTUSER_CONTENTCHANGE, DIFFERENTUSER_CONTENTREVERT
  }

  private static final String SEPARATOR = "#";

  private String flag;

  private String page;

  private Integer delta;

  private String wikipedia;

  private String user;

  private String revision;

  public static WikiEdit parse(String key, String value) {
    String[] keySplit = key.split(SEPARATOR);
    String[] valueSplit = value.split(SEPARATOR);
    WikiEdit wikiEdit = new WikiEdit();
    wikiEdit.setWikipedia(keySplit[0]);
    wikiEdit.setPage(keySplit[1]);
    wikiEdit.setRevision(valueSplit[0]);
    wikiEdit.setFlag(valueSplit[1]);
    wikiEdit.setUser(valueSplit[2]);
    wikiEdit.setDelta(valueSplit[3]);
    return wikiEdit;
  }

  public WikiEdit() {
    super();
  }

  public WikiEdit(String page, String wikipedia, String flag, Integer delta,
      String user, String revision) {
    super();
    this.page = page;
    this.wikipedia = wikipedia;
    this.flag = flag;
    this.delta = delta;
    this.user = user;
    this.revision = revision;
  }

  public String asKey() {
    return getWikipedia() + SEPARATOR + getPage();
  }

  public String asValue() {
    return getRevision() + SEPARATOR + getFlag() + SEPARATOR + getUser()
        + SEPARATOR + getDelta();
  }

  public boolean isEditedBySameUser(WikiEdit wikiEdit) {
    return StringUtils.equals(this.getUser(), wikiEdit.getUser());
  }

  public RevisionChange analyzeChanges(WikiEdit wikiEdit) {
    if (isEditedBySameUser(wikiEdit)) {
      if (isRevert(wikiEdit)) {
        return RevisionChange.SAMEUSER_CONTENTREVERT;
      } else {
        return RevisionChange.SAMEUSER_CONTENTCHANGE;
      }
    } else {
      if (isRevert(wikiEdit)) {
        return RevisionChange.DIFFERENTUSER_CONTENTREVERT;
      }
      return RevisionChange.DIFFERENTUSER_CONTENTCHANGE;
    }
  }

  boolean isRevert(WikiEdit wikiEdit) {
    if (this.getDelta() < 0 && wikiEdit.getDelta() > 0 || this.getDelta() > 0
        && wikiEdit.getDelta() < 0) {

      // delta similar about 10%?
      int unsignedDeltaSum = this.getDelta() + wikiEdit.getDelta();
      unsignedDeltaSum = unsigned(unsignedDeltaSum);

      int unsignedDelta = this.getDelta();
      unsignedDelta = unsigned(unsignedDelta);

      if (unsignedDeltaSum <= unsignedDelta * 0.1) {
        return true;
      }

    }
    return false;
  }

  private int unsigned(int unsignedDelta) {
    if (unsignedDelta < 0) {
      unsignedDelta = -1 * unsignedDelta;
    }
    return unsignedDelta;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

  public String getFlag() {
    return flag;
  }

  public String getPage() {
    return page;
  }

  public Integer getDelta() {
    return delta;
  }

  public String getWikipedia() {
    return wikipedia;
  }

  public String getUser() {
    return user;
  }

  public void setFlag(String flag) {
    this.flag = flag;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public void setDelta(Integer delta) {
    this.delta = delta;
  }

  public void setDelta(String delta) {
    this.delta = Integer.parseInt(delta);
  }

  public void setWikipedia(String wikipedia) {
    this.wikipedia = wikipedia;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }
}
