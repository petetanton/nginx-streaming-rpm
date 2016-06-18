#!/usr/bin/env bash
mkdir /opt/nginx-build
cd /opt/nginx-build
tar -zxvf nginx-streaming.tar.gz
cd nginx-streaming
sudo make install