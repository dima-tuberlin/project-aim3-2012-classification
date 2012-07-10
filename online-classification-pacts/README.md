TBD: Add some text


Job Arguments for WikipediaDumpParser: 

1) Small Test File:
	1 file:///home/flo/workspace/edu-tub-aim3-oc/src/test/resources/testDump_multiplePages.xml file:///tmp/edu-tub-aim3-oc.output

2) Original Wikipedia Dump (Full):
	1 file:///mnt/aim3/Online-Classification/data/enwiki-20120502-pages-meta-history1.xml-p000009940p000010000 file:///mnt/wikipedia/stratosphere-output/enwiki-20120502-pages-meta-history1-p000009940p000010000.csv
	
3) Original Wikipedia Dump (20120702, EN, with text metadata): 
  1 file:///mnt/wikipedia/en/20120702/enwiki-20120702-stub-meta-history.xml.gz file:///mnt/wikipedia/stratosphere-output/enwiki-20120702-stub-meta-history.csv


---------------

Job Arguments for WikipediaRevisionAnalyzer: 

  1 file:///mnt/wikipedia/stratosphere-output/enwiki-20120702-stub-meta-history.csv file:///mnt/wikipedia/stratosphere-output/enwiki-20120702-page-revision-summary.csv

	
===============
	
	
Metadata from Dumps:
* Page ID
* Page Title
* Number revisions
* Number characters in last revision
* Editing users

Live Stream:
* Page ID (long?)
*	Editing User (Hashed or int?)
* Minor change (boolean)
* Number changed characters (int)
==> HOW MUCH MEMORY REQUIRED FOR A SINGLE ENTRY?
--> Maybe restrict to certain pages?

Live Calculation:
-> Ratio total vs. changes characters
-> When other edits found in memory:
--> Has user already edited latetly?
--> Was similar number of characters (+/-) changed before?