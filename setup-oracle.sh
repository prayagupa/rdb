#!/usr/bin/env bash
duwamish-oracle() {
    echo "=========================================================="
    echo "setting up oracle"
    echo "=========================================================="

    git clone https://github.com/oracle/docker-images.git
    cd docker-images/OracleDatabase/SingleInstance/dockerfiles
    cp linuxx64_12201_database.zip 12.2.0.1/
    ./buildDockerImage.sh -v 12.2.0.1
    docker images
}
