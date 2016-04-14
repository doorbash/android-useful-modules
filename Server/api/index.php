<?php
/*
	Title: 	AppServices
	Auther: Milad Doorbash
	Date: 	3/04/2016 02:58
*/

require 'Slim/Slim.php';
require 'idiorm.php';

// Database config
$db_host = 'localhost';
$db_name = 'DATABASE_NAME';
$db_username = 'USERNAME';
$db_password = 'PASSWORD';

define("DB_TABLE_APPS", "apps");
define("DB_TABLE_VERSIONS", "versions");

ORM::configure('mysql:host='.$db_host.';dbname=' . $db_name . ';charset=utf8');
ORM::configure('username', $db_username);
ORM::configure('password', $db_password);
ORM::configure('driver_options', array(PDO::MYSQL_ATTR_INIT_COMMAND => 'SET NAMES utf8'));

\Slim\Slim::registerAutoloader();

$app = new \Slim\Slim(array('debug'=>true));

$app->get('/apps/license/:name/:version' . '/:rand/' , function($name,$version,$r) use($app)
{	
	$application = ORM::for_table(DB_TABLE_APPS)->where('name',$name)->find_one();
	if($application === FALSE)
	{
		$app->response->setStatus(200);
		$app->response->setBody(json_encode(array('result' => "error")));
		$app->response->headers->set('Content-Type', 'application/json');
		return;
	}
	$app->response->setStatus(200);
	$app->response->setBody(json_encode(array('result' => "ok" , 'ex' => $application->expired)));
	$app->response->headers->set('Content-Type', 'application/json');
});

$app->get('/apps/version/check/:package' . '/:rand/' , function($package,$r) use($app)
{	
	$versions = ORM::for_table(DB_TABLE_VERSIONS)->where('package',$package)->order_by_desc('code')->limit(1)->find_many();
	if(count($versions) == 0)
	{
		$app->response->setStatus(200);
		$app->response->setBody(json_encode(array('result' => "error")));
		$app->response->headers->set('Content-Type', 'application/json');
		return;
	}
	$version = $versions[0];
	$app->response->setStatus(200);
	$app->response->setBody(json_encode(array('result' => "ok" , 'code' => $version->code, 'forceInstall' => $version->forceInstall, 'lastChanges' => $version->lastChanges, 'filesize' => $version->filesize, 'name' => $version->name, 'md5' => $version->md5)));
	$app->response->headers->set('Content-Type', 'application/json');
});

$app->run();

?>