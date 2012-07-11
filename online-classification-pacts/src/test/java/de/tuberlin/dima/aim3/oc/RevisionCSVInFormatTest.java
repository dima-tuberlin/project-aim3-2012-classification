package de.tuberlin.dima.aim3.oc;

import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.tuberlin.dima.aim3.oc.WikipediaDumpParser.RevisionCSVOutFormat;
import de.tuberlin.dima.aim3.oc.WikipediaRevisionStatisticsCreator.RevisionCSVInFormat;
import de.tuberlin.dima.aim3.oc.input.type.PactWikiRevision;
import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.fs.FileInputSplit;
import eu.stratosphere.pact.common.io.DelimitedInputFormat;
import eu.stratosphere.pact.common.io.FileInputFormat;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;

/**
 * Tests the class {@link RevisionCSVInFormat}.
 * 
 * Makes sure that files are parsed in the manner they are written by
 * {@link RevisionCSVOutFormat}.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class RevisionCSVInFormatTest {

  private RevisionCSVInFormat sut;

  @Before
  public void setup() {
    sut = new RevisionCSVInFormat();
  }

  @Test
  public void testReader() throws IOException {

    URL input = getClass().getClassLoader().getResource(
        "testRevisions_simple.csv");

    Configuration parameters = new Configuration();
    parameters.setString(FileInputFormat.FILE_PARAMETER_KEY, input.toString());
    parameters.setString(DelimitedInputFormat.RECORD_DELIMITER, "\n");
    sut.configure(parameters);

    FileInputSplit[] inputSplits = sut.createInputSplits(1);
    Assert.assertEquals(1, inputSplits.length);
    sut.open(inputSplits[0]);

    PactRecord record = new PactRecord();
    sut.nextRecord(record);

    int pageId = record.getField(0, PactInteger.class).getValue();
    WikiRevision revision = record.getField(1, PactWikiRevision.class)
        .getRevision();

    Assert.assertEquals(12, pageId);
    Assert.assertEquals("12", revision.getPage().getId());
    Assert.assertEquals("Anarchism", revision.getPage().getTitle());
    Assert.assertEquals("332077", revision.getId());
    Assert.assertEquals("2002-10-02T23:13:34+0200", revision.getTimestamp());
    Assert.assertEquals("4369", revision.getUser().getUserid());
    Assert.assertEquals("Lir", revision.getUser().getUsername());
    Assert.assertNull(revision.getUser().getUserip());
    Assert.assertEquals(false, revision.getMinorChange().booleanValue());
    Assert
        .assertEquals(
            "The Fascist Propaganda was posted again so I fixed it by noting that anarchism opposes "
                + "totalitarianism-something which somebody seems to disagree with for some reason..."
                + "hmm they must not be an anarchi", revision.getComment());
    Assert.assertEquals(23576, revision.getTextLength().intValue());

    // read until very end
    for (int i = 1; i < 9; i++) {
      sut.nextRecord(record);
    }

    pageId = record.getField(0, PactInteger.class).getValue();
    revision = record.getField(1, PactWikiRevision.class).getRevision();

    Assert.assertEquals(12, pageId);
    Assert.assertEquals("12", revision.getPage().getId());
    Assert.assertEquals("Anarchism", revision.getPage().getTitle());
    Assert.assertEquals("332141", revision.getId());

    sut.nextRecord(record);
    Assert.assertTrue(sut.reachedEnd());
  }
}
