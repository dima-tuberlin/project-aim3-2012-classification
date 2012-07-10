package de.tuberlin.dima.aim3.oc.input;

import de.tuberlin.dima.aim3.oc.input.type.WikiRevision;

/**
 * Supports iterative processing of Wikipedia XML dumps.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public interface IterativeWikiDumpParser {

  public abstract WikiRevision readNextWikiDumpRevision();

  public abstract boolean reachedEnd();

}