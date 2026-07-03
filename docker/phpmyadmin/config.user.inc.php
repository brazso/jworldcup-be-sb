<?php
/* Override Servers array */
$cfg['Servers'] = [
    1 => [
        'auth_type' => 'cookie',
        'host' => 'localhost',
        'port' => 3306,
        'socket' => '/var/run/mysqld/mysqld.sock',
        'verbose' => 'Local developer'
    ],
    2 => [
        'host' => 'host.docker.internal',
        'port' => 3307,
        'connect_type' => 'tcp',
        'verbose' => 'Remote production via SSH tunnel'
    ],
];
