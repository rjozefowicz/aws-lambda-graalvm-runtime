#!/bin/sh
mvn clean package
$JAVA_HOME/bin/native-image -J-Dcom.sun.xml.internal.bind.v2.bytecode.ClassTailor.noOptimize=true -H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime -J-Djava.util.concurrent.ForkJoinPool.common.parallelism=1 -H:+PrintAnalysisCallTree -H:EnableURLProtocols=http,https -H:-SpawnIsolates -H:+JNI --no-server -H:-UseServiceLoaderFeature -H:+StackTrace -H:InitialCollectionPolicy='com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime' --enable-all-security-services -H:+TraceClassInitialization -H:+ReportExceptionStackTraces --no-fallback -H:ReflectionConfigurationFiles=reflection-config.json --allow-incomplete-classpath --initialize-at-build-time=org.apache.http.HttpClientConnection,org.apache.http.protocol.HttpContext,org.apache.http.conn.routing.HttpRoute,org.apache.http.pool.ConnPoolControl,org.apache.http.conn.HttpClientConnectionManager,org.slf4j.impl.Log4jLoggerAdapter,org.slf4j.helpers.FormattingTuple,org.slf4j.helpers.SubstituteLoggerFactory,org.slf4j.impl.StaticLoggerBinder,org.slf4j.helpers.MarkerIgnoringBase,org.slf4j.helpers.NamedLoggerBase,org.slf4j.helpers.MessageFormatter,org.slf4j.impl.Log4jLoggerAdapter,org.slf4j.helpers.FormattingTuple,org.slf4j.helpers.SubstituteLoggerFactory,org.slf4j.impl.StaticLoggerBinder,org.slf4j.helpers.MarkerIgnoringBase,org.slf4j.helpers.NamedLoggerBase,org.slf4j.helpers.MessageFormatter,org.slf4j.LoggerFactoryorg.slf4j.impl.Log4jLoggerAdapter,org.slf4j.helpers.FormattingTuple,org.apache.log4j.Category,org.slf4j.LoggerFactory,org.apache.log4j.Priority,org.slf4j.helpers.NOPLoggerFactory,org.slf4j.impl.Log4jLoggerFactory,org.slf4j.helpers.Util,io.netty.util.internal.logging.Log4JLogger -jar target/native-function-jar-with-dependencies.jar native-function.o
chmod +x build-artifacts/native-function.o
zip target/native-lambda.zip bootstrap native-function.o
rm -f native-function.o