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

  public PactWikiRevisionSummary() {
    super();
  }

  public PactWikiRevisionSummary(WikiPage page, int numberOfRevisions,
      double averageTextLength) {
    super();
    this.pageId = ParserUtil.parseInt(page.getId());
    this.pageTitle = page.getTitle();
    this.numberOfRevisions = numberOfRevisions;
    this.averageTextLength = averageTextLength;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(pageId);
    writeUTFNullSafe(out, pageTitle);
    out.writeInt(numberOfRevisions);
    out.writeDouble(averageTextLength);
  }

  @Override
  public void read(DataInput in) throws IOException {
    pageId = in.readInt();
    pageTitle = readUTFNullSafe(in);
    numberOfRevisions = in.readInt();
    averageTextLength = in.readDouble();
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

}
