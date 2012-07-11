package de.tuberlin.dima.aim3.oc;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tuberlin.dima.aim3.oc.WikipediaDumpParser.RevisionCSVOutFormat;
import de.tuberlin.dima.aim3.oc.input.type.PactWikiRevision;
import de.tuberlin.dima.aim3.oc.input.type.PactWikiRevisionSummary;
import de.tuberlin.dima.aim3.oc.input.type.WikiPage;
import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;
import de.tuberlin.dima.aim3.oc.input.type.WikiUser;
import de.tuberlin.dima.aim3.oc.util.Constants;
import de.tuberlin.dima.aim3.oc.util.ParserUtil;
import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.FileDataSource;
import eu.stratosphere.pact.common.contract.ReduceContract;
import eu.stratosphere.pact.common.io.DelimitedInputFormat;
import eu.stratosphere.pact.common.io.FileOutputFormat;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.plan.PlanAssembler;
import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.ReduceStub;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;

/**
 * PACT plan that creates revision statistics for every Wikipedia page.
 * 
 * It operates on top of CSV parsed Wikipedia XML dumps.
 * 
 * @see WikipediaDumpParser
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class WikipediaRevisionStatisticsCreator implements PlanAssembler,
    PlanAssemblerDescription {

  private static final Log LOG = LogFactory
      .getLog(WikipediaRevisionStatisticsCreator.class);

  @Override
  public String getDescription() {
    return "[noSubTasks] [pathToWikipediaCSVFile] [pathToOutputFile]";
  }

  @Override
  public Plan getPlan(String... args) {

    // parse job parameters
    int noSubTasks = (args.length > 0 ? ParserUtil.parseInt(args[0]) : 1);
    String csvInputFile = (args.length > 1 ? args[1] : "");
    String outputFile = (args.length > 2 ? args[2] : "");

    FileDataSource csvInput = new FileDataSource(RevisionCSVInFormat.class,
        csvInputFile, "Wikipedia Revision CSV Input");
    csvInput.setDegreeOfParallelism(noSubTasks);

    ReduceContract revisionReducer = new ReduceContract(
        PageRevisionCounter.class, PactInteger.class, 0,
        "Page Revisions Counter");
    revisionReducer.setDegreeOfParallelism(noSubTasks);
    revisionReducer.setInput(csvInput);

    FileDataSink output = new FileDataSink(RevisionSummaryOutFormat.class,
        outputFile, "Output File");
    output.setDegreeOfParallelism(noSubTasks);
    output.setInput(revisionReducer);

    Plan plan = new Plan(output, "Wikipedia Dump Analyzer");
    return plan;
  }

  public static class RevisionCSVInFormat extends DelimitedInputFormat {

    @Override
    public boolean readRecord(PactRecord target, byte[] bytes, int numBytes) {
      String inputLine = new String(bytes, 0, numBytes, Constants.CHARSET);

      StringTokenizer tokenizer = new StringTokenizer(inputLine,
          RevisionCSVOutFormat.SEPARATOR_CHAR);
      // see number of tokens created in
      // WikipediaDumpParser$RevisionCSVOutFormat#writeRecord
      int numberOfExpectedTokens = 10;
      if (tokenizer.countTokens() < numberOfExpectedTokens - 1) {
        LOG.debug("Ignoring line with unsufficient number of tokens ("
            + tokenizer.countTokens() + "): '" + inputLine + "'");
        return false;
      }

      Integer pageId;
      try {
        pageId = ParserUtil.parseInt(nullSafeToken(tokenizer));
      } catch (Throwable e) {
        LOG.debug("Ignoring line because it does not start with a number (pageId): '"
            + inputLine + "'");
        return false;
      }
      String pageTitle = nullSafeToken(tokenizer);
      String revisionId = nullSafeToken(tokenizer);
      String revisionTimestamp = nullSafeToken(tokenizer);
      String userid = nullSafeToken(tokenizer);
      String username = nullSafeToken(tokenizer);
      String userip = nullSafeToken(tokenizer);
      Integer textLength;
      try {
        textLength = ParserUtil.parseInt(nullSafeToken(tokenizer));
      } catch (Throwable e) {
        LOG.debug("Ignoring line because textLength cannot be parsed: '"
            + inputLine + "'");
        return false;
      }

      String comment;
      Boolean minorChange;
      if (tokenizer.countTokens() > numberOfExpectedTokens) {
        LOG.debug("Trying to parse line with unexpected number of tokens ("
            + tokenizer.countTokens()
            + ") as using a separator char in its comment: '" + inputLine + "'");
        comment = "";
        for (int i = 0; i <= tokenizer.countTokens() - numberOfExpectedTokens; i++) {
          comment += nullSafeToken(tokenizer);
        }
        LOG.info("Read comment with separator char: '" + comment + "'");

        minorChange = parseMinorChange(inputLine, tokenizer);
      } else if (tokenizer.countTokens() == numberOfExpectedTokens - 1) {
        // last element is missing - guess minorChange to be false
        comment = nullSafeToken(tokenizer);
        minorChange = false;
        LOG.debug("Guessed minorChange to be " + minorChange);
      } else {
        comment = nullSafeToken(tokenizer);
        minorChange = parseMinorChange(inputLine, tokenizer);
      }

      WikiUser user = new WikiUser(username, userid, userip);
      WikiRevision revision = new WikiRevision(pageId.toString(), pageTitle,
          revisionId, revisionTimestamp, user, minorChange, comment, textLength);

      target.setField(0, new PactInteger(pageId));
      target.setField(1, new PactWikiRevision(revision));

      return true;
    }

    private Boolean parseMinorChange(String inputLine, StringTokenizer tokenizer) {
      Boolean minorChange;
      try {
        minorChange = ParserUtil.parseBoolean(nullSafeToken(tokenizer));
      } catch (Throwable e) {
        LOG.debug("Used default value of false for minorChange: '" + inputLine
            + "'");
        minorChange = false;
      }
      return minorChange;
    }

    private String nullSafeToken(StringTokenizer tokenizer) {
      String token = tokenizer.nextToken();
      if (token.equals("null")) {
        return null;
      }
      return token;
    }
  }

  public static class PageRevisionCounter extends ReduceStub {

    @Override
    public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out)
        throws Exception {
      int numberOfRevisions = 0;
      double averageTextLength = 0;
      double averageTextLengthChange = 0;
      int lastTextLength = 0;
      int minTextLengthChange = Integer.MAX_VALUE;
      int minTextLengthChangeRevisionId = 0;
      int maxTextLengthChange = Integer.MIN_VALUE;
      int maxTextLengthChangeRevisionId = 0;
      WikiPage page = null;

      while (records.hasNext()) {
        PactRecord record = records.next();
        WikiRevision revision = record.getField(1, PactWikiRevision.class)
            .getRevision();

        if (page == null) {
          page = new WikiPage(revision.getPage().getTitle(), revision.getPage()
              .getId());
        }

        int textLengthChange = getUnsignedTextLengthChange(lastTextLength,
            revision.getTextLength());
        if (textLengthChange < minTextLengthChange) {
          minTextLengthChange = textLengthChange;
          minTextLengthChangeRevisionId = ParserUtil.parseInt(revision.getId());
        }
        if (textLengthChange > maxTextLengthChange) {
          maxTextLengthChange = textLengthChange;
          maxTextLengthChangeRevisionId = ParserUtil.parseInt(revision.getId());
        }
        lastTextLength = revision.getTextLength();

        averageTextLengthChange = calculateAvgTextLengthChange(
            numberOfRevisions, averageTextLengthChange, textLengthChange);

        averageTextLength = calculateAvgTextLength(numberOfRevisions,
            averageTextLength, revision.getTextLength());

        numberOfRevisions++;
      }

      PactWikiRevisionSummary wikiRevisionSummary = new PactWikiRevisionSummary(
          page, numberOfRevisions, averageTextLength, averageTextLengthChange,
          minTextLengthChange, minTextLengthChangeRevisionId,
          maxTextLengthChange, maxTextLengthChangeRevisionId);
      PactRecord record = new PactRecord(wikiRevisionSummary);
      out.collect(record);
    }

    int getUnsignedTextLengthChange(int lastTextLength, int revisionTextLength) {
      int textLengthChange = revisionTextLength - lastTextLength;
      if (textLengthChange < 0) {
        textLengthChange = -1 * textLengthChange;
      }
      return textLengthChange;
    }

    double calculateAvgTextLength(double numberOfRevisions,
        double averageTextLength, double revisionTextLength) {
      return averageTextLength * (numberOfRevisions / (numberOfRevisions + 1))
          + (revisionTextLength / (numberOfRevisions + 1));
    }

    double calculateAvgTextLengthChange(double numberOfRevisions,
        double averageTextLengthChange, double textLengthChange) {
      return averageTextLengthChange
          * (numberOfRevisions / (numberOfRevisions + 1))
          + (textLengthChange / (numberOfRevisions + 1));
    }
  }

  public static class RevisionSummaryOutFormat extends FileOutputFormat {

    private StringBuilder buffer;

    @Override
    public void writeRecord(PactRecord record) throws IOException {
      PactWikiRevisionSummary summary = record.getField(0,
          PactWikiRevisionSummary.class);

      this.buffer = new StringBuilder();
      this.buffer.setLength(0);
      this.buffer.append(summary.getPageId());
      this.buffer.append(';');
      this.buffer.append(summary.getPageTitle());
      this.buffer.append(';');
      this.buffer.append(summary.getNumberOfRevisions());
      this.buffer.append(';');
      this.buffer.append(summary.getAverageTextLength());
      this.buffer.append(';');
      this.buffer.append(summary.getAverageTextLengthChange());
      this.buffer.append(';');
      this.buffer.append(summary.getMinTextLengthChange());
      this.buffer.append(';');
      this.buffer.append(summary.getMinTextLengthChangeRevisionId());
      this.buffer.append(';');
      this.buffer.append(summary.getMaxTextLengthChange());
      this.buffer.append(';');
      this.buffer.append(summary.getMaxTextLengthChangeRevisionId());
      this.buffer.append('\n');

      byte[] bytes = this.buffer.toString().getBytes();
      this.stream.write(bytes);
    }
  }

}
