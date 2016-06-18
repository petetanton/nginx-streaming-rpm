#!/usr/bin/env bash
cd /opt/nginx-build
tar -zxvf nginx-streaming.tar.gz
cd nginx-streaming
sudo make install