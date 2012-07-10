package de.tuberlin.dima.aim3.oc;

import java.io.IOException;

import de.tuberlin.dima.aim3.oc.input.JaxbBasedWikipediaDumpInFormat;
import de.tuberlin.dima.aim3.oc.input.type.PactWikiRevision;
import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;
import de.tuberlin.dima.aim3.oc.util.ParserUtil;
import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.FileDataSource;
import eu.stratosphere.pact.common.io.FileOutputFormat;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.plan.PlanAssembler;
import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
import eu.stratosphere.pact.common.type.PactRecord;

/**
 * PACT plan that parses Wikipedia XML dumps into tuples of revisions which each
 * contains tiny details about its page element.
 * 
 * Parsing a page with all its revisions is unluckily not possible on most
 * systems due to the amount of available heap space.
 * 
 * The results of this parsing process can e.g. be processed by joining by
 * page.id.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class WikipediaDumpParser implements PlanAssembler,
    PlanAssemblerDescription {

  @Override
  public String getDescription() {
    return "[noSubTasks] [pathToWikipediaDumpFile] [pathToOutputFile]";
  }

  @Override
  public Plan getPlan(String... args) {

    // parse job parameters
    int noSubTasks = (args.length > 0 ? ParserUtil.parseInt(args[0]) : 1);
    String xmlInputFile = (args.length > 1 ? args[1] : "");
    String outputFile = (args.length > 2 ? args[2] : "");

    // Create DataSourceContract for documents relation
    FileDataSource xmlInput = new FileDataSource(
        JaxbBasedWikipediaDumpInFormat.class, xmlInputFile,
        "Wikipedia Edit History Input");
    xmlInput.setDegreeOfParallelism(noSubTasks);

    FileDataSink output = new FileDataSink(RevisionCSVOutFormat.class,
        outputFile, "Output File");
    output.setDegreeOfParallelism(noSubTasks);
    output.setInput(xmlInput);

    Plan plan = new Plan(output, "Wikipedia Dump Parser");
    return plan;
  }

  public static class RevisionCSVOutFormat extends FileOutputFormat {

    public static final String SEPARATOR_CHAR = ";";

    private StringBuilder buffer;

    @Override
    public void writeRecord(PactRecord record) throws IOException {
      WikiRevision wikiRevision;
      try {
        wikiRevision = record.getField(1, PactWikiRevision.class).getRevision();
      } catch (Throwable e) {
        throw new RuntimeException(
            "Cannot read PactWikiRevision from record with "
                + record.getNumFields() + " fields", e);
      }

      this.buffer = new StringBuilder();
      this.buffer.setLength(0);
      this.buffer.append(wikiRevision.getPage().getId());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getPage().getTitle());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getId());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getTimestamp());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getUser().getUserid());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getUser().getUsername());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getUser().getUserip());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getTextLength());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getComment());
      this.buffer.append(SEPARATOR_CHAR);
      this.buffer.append(wikiRevision.getMinorChange());
      this.buffer.append('\n');

      byte[] bytes = this.buffer.toString().getBytes();
      this.stream.write(bytes);
    }
  }

}
