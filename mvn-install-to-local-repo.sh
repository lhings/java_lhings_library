#!/bin/bash
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=target/lhings-java-2.4.1.jar -DpomFile=pom.xml
