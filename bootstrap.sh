#!/bin/sh

set -e

echo "S3 Tests Java running .bootstrap.sh " 1>&2

# Install Java

if [ -f /etc/debian_version ]; then
    for package in openjdk-8-jdk wget unzip; do
        if [ "$(dpkg --status -- $package 2>/dev/null|sed -n 's/^Status: //p')" != "install ok installed" ]; then
            # add a space after old values
            missing="${missing:+$missing }$package"
        fi
    done
    if [ -n "$missing" ]; then
        echo "$0: missing required DEB packages. Installing via sudo." 1>&2
        sudo apt-get -y install $missing
    fi
elif [ -f /etc/fedora-release ]; then
    for package in java-1.8.0-openjdk wget unzip; do
        if [ "$(rpm -qa $package 2>/dev/null)" == "" ]; then
            missing="${missing:+$missing }$package"
        fi
    done
    if [ -n "$missing" ]; then
        echo "$0: missing required RPM packages. Installing via sudo." 1>&2
        sudo yum -y install $missing
    fi
elif [ -f /etc/redhat-release ]; then
    for package in java-1.8.0-openjdk wget unzip; do
        if [ "$(rpm -qa $package 2>/dev/null)" == "" ]; then
            missing="${missing:+$missing }$package"
        fi
    done
    if [ -n "$missing" ]; then
        echo "$0: missing required RPM packages. Installing via sudo." 1>&2
        sudo yum -y install $missing
    fi
fi

# Download and install Gradle
version=4.7

if [ ! -d /opt/gradle ]; then
    sudo mkdir /opt/gradle
fi
wget https://services.gradle.org/distributions/gradle-$version-bin.zip
sudo unzip -o -d /opt/gradle gradle-$version-bin.zip
rm -rf gradle-$version-bin.zip*
export PATH=/opt/gradle/gradle-$version/bin:$PATH
gradle -v

echo "S3 Tests Java END of running .bootstrap " 1>&2
