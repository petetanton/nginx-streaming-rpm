#!/usr/bin/env bash
mkdir /opt/nginx-build
cd /opt/nginx-build
tar -zxvf nginx-streaming.tar.gz
tar -zxvf OpenSSL_1_0_1t.tar.gz
tar -zxvf pcre-8.38.tar.gz

cd nginx-1.9.9
sudo make install