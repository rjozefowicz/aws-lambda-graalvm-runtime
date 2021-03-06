#!/bin/sh
mvn clean package
mkdir build-artifacts
$JAVA_HOME/bin/native-image -J-Dcom.sun.xml.internal.bind.v2.bytecode.ClassTailor.noOptimize=true -H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime -J-Djava.util.concurrent.ForkJoinPool.common.parallelism=1 -H:+PrintAnalysisCallTree -H:EnableURLProtocols=http,https -H:-SpawnIsolates -H:+JNI --no-server -H:-UseServiceLoaderFeature -H:+StackTrace -H:InitialCollectionPolicy='com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime' --enable-all-security-services -H:+TraceClassInitialization -H:+ReportExceptionStackTraces --no-fallback -H:ReflectionConfigurationFiles=reflection-config.json --allow-incomplete-classpath --initialize-at-build-time=org.apache.http.HttpClientConnection,org.apache.http.protocol.HttpContext,org.apache.http.conn.routing.HttpRoute,org.apache.http.pool.ConnPoolControl,org.apache.http.conn.HttpClientConnectionManager,org.slf4j,org.apache.log4j -jar target/native-function-jar-with-dependencies.jar build-artifacts/native-function.o
chmod +x build-artifacts/native-function.o
cp $JAVA_HOME/lib/libsunec.so build-artifacts/libsunec.so
cp $JAVA_HOME/lib/security/cacerts build-artifacts/cacerts
cp bootstrap build-artifacts/bootstrap
cd build-artifacts
zip -r ../target/native-lambda.zip *
cd ..
rm -rf build-artifacts

aws lambda update-function-code --function-name $1 --zip-file fileb://target/native-lambda.zip