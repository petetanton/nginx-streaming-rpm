#!/usr/bin/env bash
#rpmdev-wipetree
rpmdev-setuptree
cp ~/jobs/nginx-streaming-build/workspace/nginx-streaming.tar.gz ~/rpmbuild/SOURCES/nginx-streaming.tar.gz
cp SPECS/nginx-streaming-rpm.spec ~/rpmbuild/SPECS/nginx-streaming.spec
buildrpm nginx-streaming

