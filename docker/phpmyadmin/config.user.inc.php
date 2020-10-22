<?php
/* Override Servers array */
$cfg['Servers'] = [
    1 => [
        'auth_type' => 'cookie',
        'host' => 'localhost',
        //'port' => 3306,
        'socket' => '/var/run/mysqld/mysqld.sock',
        'verbose' => 'Local developer',
    ],
    2 => [
        'auth_type' => 'cookie',
        'host' => 'worldcup.zematix.hu',
        //'port' => 3306,
        //'ssl' => true,
        //'ssl_verify' => false,
        //'ssl_key' => '../client-key.pem',
		//'ssl_cert' => '../client-cert.pem',
		//'ssl_ca' => '../server-ca.pem',
        'verbose' => 'Remote production',
    ],
];