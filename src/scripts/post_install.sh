#!/usr/bin/env bash
mkdir /opt/nginx-build
cd /opt/nginx-build
tar -zxvf nginx-streaming.tar.gz
cd nginx-1.9.9
sudo make install