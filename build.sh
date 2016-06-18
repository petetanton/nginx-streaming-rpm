#!/usr/bin/env bash
cd src
mkdir nginx
cd nginx
rm -rf *
git clone https://github.com/arut/nginx-rtmp-module

wget https://sourceforge.net/projects/pcre/files/pcre/8.38/pcre-8.38.tar.gz/download
mv download pcre-8.38.tar.gz
tar xf pcre-8.38.tar.gz

wget https://github.com/openssl/openssl/archive/OpenSSL_1_0_1t.tar.gz
tar xf OpenSSL_1_0_1t.tar.gz

wget http://nginx.org/download/nginx-1.9.9.tar.gz
tar xf nginx-1.9.9.tar.gz
cd nginx-1.9.9
./configure --add-module=../nginx-rtmp-module --with-pcre=../pcre-8.38 --with-openssl=../openssl-OpenSSL_1_0_1t
make
cd ..
tar cf - nginx-1.9.9/ | gzip > nginx-streaming.tar.gz

cd ..


