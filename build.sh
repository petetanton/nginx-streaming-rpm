#!/usr/bin/env bash
#rpmdev-wipetree
rpmdev-setuptree
#cp ~/jobs/nginx-streaming-build/workspace/nginx-streaming.tar.gz ~/rpmbuild/SOURCES/nginx-streaming.tar.gz
mkdir SOURCES
cp ~/jobs/nginx-streaming-build/workspace/nginx-streaming.tar.gz SOURCES/nginx-streaming.tar.gz
#cp SPECS/nginx-streaming-rpm.spec ~/rpmbuild/SPECS/nginx-streaming.spec
rpmbuild SPECS/nginx-streaming-rpm.spec

