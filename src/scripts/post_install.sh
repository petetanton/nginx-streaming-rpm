#!/usr/bin/env bash
mkdir /opt/nginx-build
cd /opt/nginx-build
tar -zxvf nginx-streaming.tar.gz

cd nginx-1.9.9
./configure --user=nginx --group=nginx --prefix=/etc/nginx --sbin-path=/usr/sbin/nginx --conf-path=/etc/nginx/nginx.conf --error-log-path=/var/log/nginx/error.log --http-log-path=/var/log/nginx/access.log --pid-path=/var/run/nginx.pid --lock-path=/var/run/nginx.lock --with-http_ssl_module --with-pcre --add-module=../nginx-rtmp-module
make
make install
useradd -d /etc/nginx/ -s /sbin/nologin nginx
mv /opt/nginx-build/nginx.sh /etc/init.d/nginx
chmod +x /etc/init.d/nginx