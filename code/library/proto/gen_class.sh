#!/bin/bash

# use your protoc path
PROTOC=protoc.exe

$PROTOC --java_out=../src/main/java/ *.proto