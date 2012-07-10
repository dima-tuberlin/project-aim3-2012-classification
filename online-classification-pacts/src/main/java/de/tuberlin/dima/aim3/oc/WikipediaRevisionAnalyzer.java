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
 * PACT plan that analyzes revisions of CSV parsed Wikipedia XML dumps.
 * 
 * @see WikipediaDumpParser
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class WikipediaRevisionAnalyzer implements PlanAssembler,
    PlanAssemblerDescription {

  private static final Log LOG = LogFactory
      .getLog(WikipediaRevisionAnalyzer.class);

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
      if (tokenizer.countTokens() != 10) {
        LOG.warn("Ignoring line with unexpected number of tokens ("
            + tokenizer.countTokens() + "): '" + inputLine + "'");
        return false;
      }

      String pageId = nullSafeToken(tokenizer);
      String pageTitle = nullSafeToken(tokenizer);
      String revisionId = nullSafeToken(tokenizer);
      String revisionTimestamp = nullSafeToken(tokenizer);
      String userid = nullSafeToken(tokenizer);
      String username = nullSafeToken(tokenizer);
      String userip = nullSafeToken(tokenizer);
      Integer textLength = ParserUtil.parseInt(nullSafeToken(tokenizer));
      String comment = nullSafeToken(tokenizer);
      Boolean minorChange = ParserUtil.parseBoolean(nullSafeToken(tokenizer));

      WikiUser user = new WikiUser(username, userid, userip);
      WikiRevision revision = new WikiRevision(pageId, pageTitle, revisionId,
          revisionTimestamp, user, minorChange, comment, textLength);

      target.setField(0, new PactInteger(ParserUtil.parseInt(pageId)));
      target.setField(1, new PactWikiRevision(revision));

      return true;
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
      WikiPage page = null;

      while (records.hasNext()) {
        PactRecord record = records.next();
        WikiRevision revision = record.getField(1, PactWikiRevision.class)
            .getRevision();

        if (page == null) {
          page = new WikiPage(revision.getPage().getId(), revision.getPage()
              .getTitle());
        }

        averageTextLength = averageTextLength
            * (numberOfRevisions / (numberOfRevisions + 1))
            + (revision.getTextLength() / (numberOfRevisions + 1));
        numberOfRevisions++;
      }

      PactWikiRevisionSummary wikiRevisionSummary = new PactWikiRevisionSummary(
          page, numberOfRevisions, averageTextLength);
      PactRecord record = new PactRecord(wikiRevisionSummary);
      out.collect(record);
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
      this.buffer.append('\n');

      byte[] bytes = this.buffer.toString().getBytes();
      this.stream.write(bytes);
    }
  }

}
