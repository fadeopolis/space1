#!/bin/bash -xe

# install ant
sudo apt-get install ant
# install ivy
sudo apt-get install ivy

# copy ivy lib to ant-lib folder
sudo cp /usr/share/java/ivy.jar /usr/share/ant/lib/ivy.jar

