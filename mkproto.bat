@echo off
protoc --java_out=src\main\java src\main\protobuf\risp_event.proto
