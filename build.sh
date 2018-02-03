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

wget https://nginx.org/download/nginx-1.12.2.tar.gz
tar xf nginx-1.12.2.tar.gz
cd nginx-1.12.2
./configure --user=nginx --group=nginx --prefix=/etc/nginx --sbin-path=/usr/sbin/nginx --conf-path=/etc/nginx/nginx.conf --error-log-path=/var/log/nginx/error.log --http-log-path=/var/log/nginx/access.log --pid-path=/var/run/nginx.pid --lock-path=/var/run/nginx.lock --with-http_ssl_module --with-pcre --add-module=../nginx-rtmp-module
make
cd ..
tar cf - nginx-1.12.2/ | gzip > nginx-streaming.tar.gz

cd ..


