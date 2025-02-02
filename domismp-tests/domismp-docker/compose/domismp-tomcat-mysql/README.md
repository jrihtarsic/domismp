# Experimental SMP docker image
Purpose of compose plan is to startup fully functional SMP environment for demo and testing. The plan contains. 
 - MySql database
 - SML services
 - CAS instance (EULOGIN)
 - email server (inbucket)


# Run environment

The environment can be started with the following command. ()

    docker compose -f docker-compose.yml up -d

To start the environment with local configuration (for development and testing). 

    docker compose -f docker-compose.yml -f docker-compose.local.yml up -d

# Stop environment
docker-compose -f docker-compose.yml down


## Start/Stop environment with provides bash scripts

To start the environment the bash scripts can be used. Scripts configures the environment variables and starts the services.
The command: 

    ./runCompose.sh 

The compose scripts has the following options:

- i: path to the database data initialization script, default: SMP_PROJECT_FOLDER/smp-webapp/src/main/smp-setup/database-scripts/mysql5innodb-data.sql
- v: version of the SMP to start. If not provided, the version will defined by maven project version
- l: start with local compose file docker-compose.localhost.yml, default: false. The compose file is used to start  the SMP with local configuration (e.g. exporting ports, etc.)

The command:

    ./runCompose.sh local -i /path/to/your/data.sql -v 1.0.0 -l true
 


## SMP 
url: http://localhost:8982/smp/

http://eulogin.protected.smp.local:8982/smp/
eulogin.protected.smp.local

### MYSQL 
Database client connection (for testing and debugging )
url: jdbc:mysql://localhost:3908/smp
Username: smp
Password: smp

### Volume (-v /opt/dockerdata/sml:/data)
Mysql database files and tomcat configuration (and logs) can be externalized for experimenting with different SMP settings.


## SML 
url: http://localhost:8982/edelivery-sml/

### MYSQL 
Database client connection (for testing and debugging )
url: jdbc:mysql://localhost:3908/sml
Username: sml
Password: sml

## CAS - EULOGIN 
url: https://localhost:7102/cas/login
Users: (For details see the CAS configuration: [userDataBase.xml](eulogin%2Finit-data%2FuserDataBase.xml)):
- user (Peter.PARKER@dummy-mail-not-exists.eu)/123456
- system (Bruce.BANNER@dummy-mail-not-exists.eu)/123456
- user-02 (Tony.STARK@dummy-mail-not-exists.eu)/123456

 To use eulogin add the following hostname mappings (Linux: /etc/hosts, windows: C:\Windows\System32\drivers\etc\hosts
                                                                                 
 - 127.0.0.1 eulogin-mock-server
 - 127.0.0.1 eulogin.protected.smp.local


## Mail server

Mock mail server for monitoring send alert mails. 
url: http://localhost:9005/monitor 
    
