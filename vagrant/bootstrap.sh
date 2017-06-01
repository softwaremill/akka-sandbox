#!/bin/bash

function installSbt {
	echo "Installing Sbt"
	wget -q https://dl.bintray.com/sbt/debian/sbt-0.13.6.deb
	dpkg -i sbt-0.13.6.deb
	rm sbt-0.13.6.deb
}

function installScala {
	echo "Installing Scala"
	wget -q http://downloads.typesafe.com/scala/2.11.2/scala-2.11.2.deb
	dpkg -i scala-2.11.2.deb
	rm scala-2.11.2.deb
}

function installJava {
	echo "Installing Java"
	add-apt-repository ppa:webupd8team/java

	apt-get update

	echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
	apt-get -y install oracle-java8-installer
}

function installDatadogAgent {
    echo "Installing Datadog with key $datadog_api_key"
    export DD_API_KEY=$datadog_api_key
    bash -c "$(curl -L https://raw.githubusercontent.com/DataDog/dd-agent/master/packaging/datadog-agent/source/install_agent.sh)"
}

apt-get update

installJava
installScala
installSbt
installDatadogAgent