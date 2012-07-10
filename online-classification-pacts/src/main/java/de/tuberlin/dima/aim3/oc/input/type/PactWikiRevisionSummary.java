package de.tuberlin.dima.aim3.oc.input.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.tuberlin.dima.aim3.oc.util.ParserUtil;

public class PactWikiRevisionSummary extends BaseValue {

  private int pageId;

  private String pageTitle;

  private int numberOfRevisions;

  private double averageTextLength;

  private double averageTextLengthChange;

  private int minTextLengthChange;

  private int minTextLengthChangeRevisionId;

  private int maxTextLengthChange;

  private int maxTextLengthChangeRevisionId;

  public PactWikiRevisionSummary() {
    super();
  }

  public PactWikiRevisionSummary(WikiPage page, int numberOfRevisions,
      double averageTextLength, double averageTextLengthChange,
      int minTextLengthChange, int minTextLengthChangeRevisionId,
      int maxTextLengthChange, int maxTextLengthChangeRevisionId) {
    super();
    this.pageId = ParserUtil.parseInt(page.getId());
    this.pageTitle = page.getTitle();
    this.numberOfRevisions = numberOfRevisions;
    this.averageTextLength = averageTextLength;
    this.averageTextLengthChange = averageTextLengthChange;
    this.minTextLengthChange = minTextLengthChange;
    this.minTextLengthChangeRevisionId = minTextLengthChangeRevisionId;
    this.maxTextLengthChange = maxTextLengthChange;
    this.maxTextLengthChangeRevisionId = maxTextLengthChangeRevisionId;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(pageId);
    writeUTFNullSafe(out, pageTitle);
    out.writeInt(numberOfRevisions);
    out.writeDouble(averageTextLength);
    out.writeDouble(averageTextLengthChange);
    out.writeInt(minTextLengthChange);
    out.writeInt(minTextLengthChangeRevisionId);
    out.writeInt(maxTextLengthChange);
    out.writeInt(maxTextLengthChangeRevisionId);
  }

  @Override
  public void read(DataInput in) throws IOException {
    pageId = in.readInt();
    pageTitle = readUTFNullSafe(in);
    numberOfRevisions = in.readInt();
    averageTextLength = in.readDouble();
    averageTextLengthChange = in.readDouble();
    minTextLengthChange = in.readInt();
    minTextLengthChangeRevisionId = in.readInt();
    maxTextLengthChange = in.readInt();
    maxTextLengthChangeRevisionId = in.readInt();
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }

  public int getPageId() {
    return pageId;
  }

  public String getPageTitle() {
    return pageTitle;
  }

  public int getNumberOfRevisions() {
    return numberOfRevisions;
  }

  public double getAverageTextLength() {
    return averageTextLength;
  }

  public double getAverageTextLengthChange() {
    return averageTextLengthChange;
  }

  public int getMinTextLengthChange() {
    return minTextLengthChange;
  }

  public int getMinTextLengthChangeRevisionId() {
    return minTextLengthChangeRevisionId;
  }

  public int getMaxTextLengthChange() {
    return maxTextLengthChange;
  }

  public int getMaxTextLengthChangeRevisionId() {
    return maxTextLengthChangeRevisionId;
  }

}
