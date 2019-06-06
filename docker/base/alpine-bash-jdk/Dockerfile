# alpine, java, ssh, bash

FROM alpine:3.9
MAINTAINER BNZ

ENV LANG C.UTF-8

#ADD VERSION .

#set password-less permission on start, install bash and openssh
 # RUN apk --update add --no-cache openssh bash \
  #    && sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config \
   #   &&  echo 'root:root' | chpasswd \
    #  && rm -rf /var/cache/apk/*

RUN apk add --update add --no-cache bash && rm -rf /var/cache/apk/*

# add a simple script that can auto-detect the appropriate JAVA_HOME value
# based on whether the JDK or only the JRE is installed
RUN { \
		echo '#!/bin/sh'; \
		echo 'set -e'; \
		echo; \
		echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
	} > /usr/local/bin/docker-java-home \
	&& chmod +x /usr/local/bin/docker-java-home

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin
ENV JAVA_VERSION 8u191
#ENV JAVA_ALPINE_VERSION 8.191.12-r0
ENV JAVA_ALPINE_VERSION  8.201.08-r0

RUN set -x \
	&& apk add --no-cache \
		openjdk8="$JAVA_ALPINE_VERSION" \
	&& [ "$JAVA_HOME" = "$(docker-java-home)" ]




