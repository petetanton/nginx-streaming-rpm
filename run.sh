#!/usr/bin/env bash
java -Duk.tanton.streaming.live.dynamo.publishersTable="live-streaming-system-Publishers-W7E9YS75J8K4" -Duk.tanton.streaming.live.dynamo.accountsTable="live-streaming-system-Accounts-1DFWU7XRPBPR8" -Duk.tanton.streaming.live.sqs.transcodeQueue="https://sqs.eu-west-1.amazonaws.com/977503918776/live-streaming-transcoder-TranscoderInputQueue-DVVJRI0HJO8I" -jar target/nginx-streaming-jar-with-dependencies.jar