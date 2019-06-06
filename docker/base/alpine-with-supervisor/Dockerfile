# alpine, java, ssh, bash, wget

FROM alpine:3.9
MAINTAINER BNZ

ENV LANG C.UTF-8



#install openssh & bash & supervisor
#set password-less permission on start
  RUN apk --update add --no-cache openssh bash supervisor \
      && sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config \
      &&  echo 'root:root' | chpasswd \
      && rm -rf /var/cache/apk/*

 # create directory for child images to store configuration in supervisor and  set supervisor base configuration
 RUN  mkdir -p /var/log/supervisor && \
       mkdir -p /etc/supervisor/conf.d && \
       ADD supervisor.conf /etc/supervisor.conf

#install wget
 RUN   apk update \
  &&   apk add ca-certificates wget \
   &&  update-ca-certificates \
   &&  apk add openssl


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
ENV JAVA_ALPINE_VERSION 8.191.12-r0



RUN set -x \
    && apk update && apk upgrade \
    && apk add --no-cache bash \
    && apk add --no-cache \
      openjdk8="$JAVA_ALPINE_VERSION" \
    && [ "$JAVA_HOME" = "$(docker-java-home)" ]


EXPOSE 22 80 443

# default command
CMD ["supervisord", "-c", "/etc/supervisor.conf"]




