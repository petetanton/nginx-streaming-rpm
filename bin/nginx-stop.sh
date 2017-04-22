#!/usr/bin/env bash
kill $(ps aux | grep "nginx" | grep "master" | awk '{print $2}')
