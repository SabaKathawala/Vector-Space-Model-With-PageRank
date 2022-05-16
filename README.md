# Vector-Space-Model-With-PageRank

This project implements a search engine based on the combination of vector space model and PageRank. The system crawled and indexed 3000 web pages within the UIC domain. Pages were ranked using TF-IDF, PageRank, Query Expansion and combination of the three techniques. The combination was based on the QD- PageRank algorithm by Richardson and Domingos.

For any given query, the candidate web pages were retrieved using vector-space model, and sorted based on a TF-IDF from vector-space model and PageRank scores. The system was designed and developed using different modules, consisting of crawler, text processor, indexer, scorer, and retrieval. A web interface allowed users to query the system using different ranking schemes. A comparison of different techniques was done by manual evaluation of five sample queries.

### How to run the project

Follow below steps for running on MACs

-> Download Apache Maven: https://www-us.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz

-> Install node.js and npm: https://www.npmjs.com/get-npm

-> Install Brew: https://brew.sh/

### Install MVN
-> Open terminal
-> Run below commands (Path: Apache Maven .tar file location)
```
cd Path
tar xzvf apache-maven-3.6.0-bin.tar.gz
pwd
```
-> Copy the output of pwd in a file and run below command
```
export PATH=copied_path/bin:$PATH
mvn -v
```
-> You should see your mvn version info
-> Note that you will need to run the "export" command every time if using mvn commands in a new terminal

#### Installing Redis
-> Open a new terminal tab 
-> run the below command
```
brew install redis
```
-> Download Redis dump file from: https://drive.google.com/open?id=1D4dp2kEcrHXAsIIZKFkwwPu1M-8AYZ1H
### Very Important
-> Open file /usr/local/etc/redis.conf in a notepad

-> Search for dbfilename and move to the line dir /data/mydirectory/

-> Change this path to the location where you downloaded the dump.rdb file(Redis dump file)

-> if you change the filename, then update the name next to dbfilename
```
# The filename where to dump the DB
dbfilename mydump.rdb (Put your dump file name here in place of mydump.rdb)

# The working directory.
#
# The DB will be written inside this directory, with the filename specified
# above using the 'dbfilename' configuration directive.
# 
# Also the Append Only File will be created inside this directory.
# 
# Note that you must specify a directory here, not a file name.
dir /data/mydirectory/  (Put your dump file directory here in place of /data/mydirectory/)
```
-> Link for reference: https://stackoverflow.com/questions/14497234/how-to-recover-redis-data-from-snapshotrdb-file-copied-from-another-machine

-> Now start the server. Wait till it says Ready to accept connections
```
redis-server
```
-> In this first terminal window, run the below commands
```
git clone https://github.com/SabaKathawala/Vector-Space-Model-With-PageRank.git
cd path-to-the-cloned-repo
mvn spring-boot:run
```
-> It will take around 5 minutes for the server to start the first time

-> Open a browser

-> In the address bar type: localhost:8080
-> Application starts

-> https://drive.google.com/open?id=10qJvU4ZfTfZiwF-bV48Nage4-5gUIwTD : Video of the Project

