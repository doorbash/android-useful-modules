--
-- Table structure for table `apps`
--

CREATE TABLE IF NOT EXISTS `apps` (
  `name` varchar(100) NOT NULL,
  `expired` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `versions`
--

CREATE TABLE IF NOT EXISTS `versions` (
  `package` varchar(100) NOT NULL,
  `code` int(11) NOT NULL,
  `forceInstall` tinyint(4) NOT NULL DEFAULT '0',
  `lastChanges` varchar(500) CHARACTER SET utf8 NOT NULL,
  `filesize` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `md5` varchar(32) NOT NULL,
  PRIMARY KEY (`package`,`code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;