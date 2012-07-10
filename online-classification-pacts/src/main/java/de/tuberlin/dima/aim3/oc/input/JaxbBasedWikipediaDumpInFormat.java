package de.tuberlin.dima.aim3.oc.input;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tuberlin.dima.aim3.oc.input.jaxb.WikiDumpStaxJaxbParser;
import de.tuberlin.dima.aim3.oc.input.type.PactWikiRevision;
import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;
import de.tuberlin.dima.aim3.oc.util.ParserUtil;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.fs.BlockLocation;
import eu.stratosphere.nephele.fs.FSDataInputStream;
import eu.stratosphere.nephele.fs.FileInputSplit;
import eu.stratosphere.nephele.fs.FileStatus;
import eu.stratosphere.nephele.fs.FileSystem;
import eu.stratosphere.nephele.fs.LineReader;
import eu.stratosphere.nephele.fs.Path;
import eu.stratosphere.pact.common.generic.io.InputFormat;
import eu.stratosphere.pact.common.io.FileInputFormat;
import eu.stratosphere.pact.common.io.statistics.BaseStatistics;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;

/**
 * Stratosphere {@link InputFormat} which relies on the {@link FileInputFormat}
 * and uses custom code to slice and process the input content.
 * 
 * Currently it only creates a single input slice for every XML file which
 * simplifies parsing but does not scale well when running on a cluster.
 * 
 * Internally it uses JAXB and StAX for XML processing and iterates through the
 * input file reading one <i><page></i> node simultaneously.
 * 
 * TODO Enable slicing of XML input file
 * 
 * TODO Currently runs into {@link OutOfMemoryError}s when processing large
 * files (~ 600MB).
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 */
public class JaxbBasedWikipediaDumpInFormat extends FileInputFormat {

  private static final Log LOG = LogFactory
      .getLog(JaxbBasedWikipediaDumpInFormat.class);

  private static final int NUMBER_OF_INPUT_SPLITS = 1;

  public static final String INPUT_GZIPPED = "input.gzip";

  private IterativeWikiDumpParser parser;

  public JaxbBasedWikipediaDumpInFormat() {
    super();
  }

  @Override
  public void configure(Configuration parameters) {
    super.configure(parameters);

    boolean isGzipInput = parameters.getBoolean(INPUT_GZIPPED,
        detectedGzipFileType());
    LOG.info("Reading input file '" + this.filePath.toUri().toString()
        + "' as GZIP: " + isGzipInput);

    Reader xmlReader;
    try {
      InputStream is;
      is = new FileInputStream(new File(this.filePath.toUri()));
      if (isGzipInput) {
        try {
          is = new GZIPInputStream(new BufferedInputStream(is));
        } catch (IOException e) {
          LOG.warn(
              "Cannot read input file as GZIP, trying to read as plain text", e);
        }
      }
      xmlReader = new InputStreamReader(is);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Cannot read input file", e);
    }
    parser = new WikiDumpStaxJaxbParser(xmlReader);
  }

  private boolean detectedGzipFileType() {
    if (filePath != null) {
      URI inputFileUri = this.filePath.toUri();
      return inputFileUri.toString().endsWith(".gz");
    }
    return false;
  }

  /**
   * Do NOT split Dump files into multiple splits.
   * 
   * Could be enhanced for scalability reasons but for a proof of concept this
   * is sufficient right now.
   */
  @Override
  public FileInputSplit[] createInputSplits(int minNumSplits)
      throws IOException {
    final Path path = this.filePath;
    final FileSystem fs = path.getFileSystem();

    final FileStatus pathFile = fs.getFileStatus(path);

    List<FileInputSplit> splits = new ArrayList<FileInputSplit>();
    if (pathFile.isDir()) {
      // input is directory. list all contained files
      final FileStatus[] dir = fs.listStatus(path);
      for (int i = 0; i < dir.length; i++) {
        final BlockLocation[] blocks = fs.getFileBlockLocations(dir[i], 0,
            dir[i].getLen());
        splits.add(new FileInputSplit(i, dir[i].getPath(), 0, dir[i].getLen(),
            blocks[0].getHosts()));
      }
    } else {
      // analogous for one file
      final BlockLocation[] blocks = fs.getFileBlockLocations(pathFile, 0,
          pathFile.getLen());
      splits.add(new FileInputSplit(0, path, 0, pathFile.getLen(), blocks[0]
          .getHosts()));
    }
    return splits.toArray(new FileInputSplit[splits.size()]);
  }

  @Override
  public BaseStatistics getStatistics(BaseStatistics cachedStatistics) {

    FileBaseStatistics stats = null;

    // check the cache
    if (cachedStatistics != null
        && cachedStatistics instanceof FileBaseStatistics) {
      stats = (FileBaseStatistics) cachedStatistics;
    } else {
      stats = new FileBaseStatistics(-1, BaseStatistics.UNKNOWN,
          BaseStatistics.UNKNOWN);
    }

    try {
      final Path file = this.filePath;
      final URI uri = file.toUri();

      // get the filesystem
      final FileSystem fs = FileSystem.get(uri);
      List<FileStatus> files = null;

      // get the file info and check whether the cached statistics are still
      // valid.
      {
        FileStatus status = fs.getFileStatus(file);

        if (status.isDir()) {
          FileStatus[] fss = fs.listStatus(file);
          files = new ArrayList<FileStatus>(fss.length);
          boolean unmodified = true;

          for (FileStatus s : fss) {
            if (!s.isDir()) {
              files.add(s);
              if (s.getModificationTime() > stats.getLastModificationTime()) {
                stats.setLastModificationTime(s.getModificationTime());
                unmodified = false;
              }
            }
          }

          if (unmodified) {
            return stats;
          }
        } else {
          // check if the statistics are up to date
          long modTime = status.getModificationTime();
          if (stats.getLastModificationTime() == modTime) {
            return stats;
          }

          stats.setLastModificationTime(modTime);

          files = new ArrayList<FileStatus>(1);
          files.add(status);
        }
      }

      stats.setAverageRecordWidth(-1.0f);
      long fileSize = 0;
      // calculate the whole length
      for (FileStatus s : files) {
        fileSize += s.getLen();
      }
      stats.setTotalInputSize(fileSize);

      // sanity check
      if (stats.getTotalInputSize() <= 0) {
        stats.setTotalInputSize(BaseStatistics.UNKNOWN);
        return stats;
      }

      // currently, the sampling only works on line separated data
      // final byte[] delimiter = getDelimiter();
      // if (!((delimiter.length == 1 && delimiter[0] == '\n') ||
      // (delimiter.length == 2
      // && delimiter[0] == '\r' && delimiter[1] == '\n'))) {
      // return stats;
      // }

      // make the samples small for very small files
      int numSamples = Math.min(NUMBER_OF_INPUT_SPLITS,
          (int) (stats.getTotalInputSize() / 1024));
      if (numSamples < 2) {
        numSamples = 2;
      }

      long offset = 0;
      long bytes = 0; // one byte for the line-break
      long stepSize = stats.getTotalInputSize() / numSamples;

      int fileNum = 0;
      int samplesTaken = 0;

      // take the samples
      for (int sampleNum = 0; sampleNum < numSamples && fileNum < files.size(); sampleNum++) {
        FileStatus currentFile = files.get(fileNum);
        FSDataInputStream inStream = null;

        try {
          inStream = fs.open(currentFile.getPath());
          LineReader lineReader = new LineReader(inStream, offset,
              currentFile.getLen() - offset, 1024);
          byte[] line = lineReader.readLine();
          lineReader.close();

          if (line != null && line.length > 0) {
            samplesTaken++;
            bytes += line.length + 1; // one for the linebreak
          }
        } finally {
          // make a best effort to close
          if (inStream != null) {
            try {
              inStream.close();
            } catch (Throwable t) {
            }
          }
        }

        offset += stepSize;

        // skip to the next file, if necessary
        while (fileNum < files.size()
            && offset >= (currentFile = files.get(fileNum)).getLen()) {
          offset -= currentFile.getLen();
          fileNum++;
        }
      }

      stats.setAverageRecordWidth(bytes / (float) samplesTaken);
    } catch (IOException ioex) {
      if (LOG.isWarnEnabled())
        LOG.warn("Could not determine complete statistics for file '"
            + filePath + "' due to an io error: " + ioex.getMessage());
    } catch (Throwable t) {
      if (LOG.isErrorEnabled())
        LOG.error(
            "Unexpected problen while getting the file statistics for file '"
                + filePath + "': " + t.getMessage(), t);
    }

    return stats;
  }

  @Override
  public boolean reachedEnd() throws IOException {
    return parser.reachedEnd();
  }

  @Override
  public boolean nextRecord(PactRecord record) throws IOException {
    WikiRevision revision = parser.readNextWikiDumpRevision();
    if (revision != null) {
      int revisionId = ParserUtil.parseInt(revision.getId());
      record.setField(0, new PactInteger(revisionId));
      record.setField(1, new PactWikiRevision(revision));
      return true;
    }
    return false;
  }

}
