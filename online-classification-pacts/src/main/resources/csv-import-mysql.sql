CREATE DATABASE `onlineclassification` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';

USE `onlineclassification`;

CREATE TABLE `revisionsummary` (
  `pageId` int(11) NOT NULL auto_increment,
  `pageTitle` varchar(255) default NULL,
  `numberOfRevisions` int(11) default NULL,
  `averageTextLength` double default NULL,
  `averageTextLengthChange` double default NULL,
  `minTextLengthChange` int(11) default NULL,
  `minTextLengthChangeRevisionId` int(11) default NULL,
  `maxTextLengthChange` int(11) default NULL,
  `maxTextLengthChangeRevisionId` int(11) default NULL,
  PRIMARY KEY  (`pageId`),
  UNIQUE KEY `pageId` (`pageId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

LOAD DATA INFILE '/PATH/TO/WIKIPEDIA_REVISION_ANALYZER_RESULT.CSV' 
  INTO TABLE `revisionsummary` 
  COLUMNS TERMINATED BY ';' 
  OPTIONALLY ENCLOSED BY '"'
  ESCAPED BY '"'
  LINES TERMINATED BY '\n'
  (pageId, pageTitle, numberOfRevisions, averageTextLength, averageTextLengthChange, minTextLengthChange, minTextLengthChangeRevisionId, maxTextLengthChange, maxTextLengthChangeRevisionId);


/* show 50 most relevant pages ordered by number of revisions */
select * from revisionsummary 
where pageTitle not like "Talk:%" 
and pageTitle not like "Wikipedia:%" 
and pageTitle not like "Wikipedia Talk:%" 
and pageTitle not like "User:%" 
and pageTitle not like "User Talk:%" 
and pageTitle not like "File:%" 
and pageTitle not like "Help:%" 
and pageTitle not like "Category:%" 
and pageTitle not like "Template:%" 
order by numberOfRevisions DESC limit 0,50;
