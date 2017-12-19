# java
#
# VERSION       Java 7,8

# use the centos base image provided by dotCloud
FROM centos:7.4.1708
MAINTAINER Kobi Kisos kobi@gigaspaces.com.

# Upgrading system
#RUN yum -y upgrade
RUN yum -y swap -- remove fakesystemd -- install systemd systemd-libs
#RUN yum -y update; yum clean all
RUN yum -y install wget subversion nc tar unzip

RUN wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
RUN yum -y install apache-maven

# Downloading and installing Java 7
RUN wget https://s3.amazonaws.com/gigaspaces-pro-files/jdk-7u80-linux-x64.rpm -O /tmp/jdk-7u80-linux-x64.rpm
RUN yum -y install /tmp/jdk-7u80-linux-x64.rpm

# Downloading and installing Java 8
RUN wget https://s3.amazonaws.com/gigaspaces-pro-files/jdk-8u151-linux-x64.rpm -O /tmp/jdk-8u151-linux-x64.rpm
RUN yum -y install /tmp/jdk-8u151-linux-x64.rpm

RUN yum -y install iptables

RUN alternatives --install /usr/bin/java jar /usr/java/latest/bin/java 200000
RUN alternatives --install /usr/bin/javaws javaws /usr/java/latest/bin/javaws 200000
RUN alternatives --install /usr/bin/javac javac /usr/java/latest/bin/javac 200000

RUN echo "switch-java() {" >> /root/.bashrc
RUN echo "if [ $"1" -eq 7 ]" >> /root/.bashrc
RUN echo "then" >> /root/.bashrc
RUN echo "ln -snf /usr/java/jdk1.7.0_80/ /usr/java/latest" >> /root/.bashrc
RUN echo "else" >> /root/.bashrc
RUN echo "ln -snf /usr/java/jdk1.8.0_131/ /usr/java/latest" >> /root/.bashrc
RUN echo "fi" >> /root/.bashrc
RUN echo "}" >> /root/.bashrc
RUN echo "alias config-java=switch-java" >> /root/.bashrc

ENV JAVA_HOME /usr/java/latest
RUN echo "export JAVA_HOME=/usr/java/latest" >> /root/.bashrc
ENV PATH $JAVA_HOME/bin:$PATH

ENV XAP_NIC_ADDRESS "#local:ip#"
ENV NIC_ADDR "#local:ip#"
RUN mkdir /checkouts
RUN mkdir /xap
RUN mkdir /root/.m2
RUN mkdir /output
RUN mkdir /work
RUN mkdir /deploy
RUN mkdir /tmp/zookeeper

RUN /bin/bash -c "source /root/.bashrc"

ENV EXT_JAVA_OPTIONS "-Dcom.gs.transport_protocol.lrmi.bind-port=10000-10100 -Dcom.gigaspaces.start.httpPort=9104 -Dcom.gigaspaces.system.registryPort=7102 -Dcom.gs.deploy=/deploy -Dcom.gs.work=/work -Dcom.gigaspaces.logger.RollingFileHandler.filename-pattern.homedir=/output"
ENV XAP_LOOKUP_GROUPS sgtest
ENV LOOKUPGROUPS sgtest

# install openssh server
RUN yum -y install openssh-server

# install openssh clients
RUN yum -y install openssh-clients

# create host keys
RUN ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_key
RUN ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_rsa_key
RUN ssh-keygen -b 1024 -t dsa -f /etc/ssh/ssh_host_dsa_key

# set password for ssh
RUN echo root:root | chpasswd

# make ssh directories
RUN mkdir /root/.ssh
RUN mkdir /var/run/sshd

# enable networking
RUN echo 'NETWORKING=yes' >> /etc/sysconfig/network

# install supervisord
RUN /bin/rpm -Uvh http://download.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm
RUN yum -y install python-setuptools
RUN easy_install supervisor
RUN /usr/bin/echo_supervisord_conf > /etc/supervisord.conf
RUN mkdir -p /var/log/supervisor

# make supervisor run in foreground
RUN sed -i -e "s/^nodaemon=false/nodaemon=true/" /etc/supervisord.conf

# tell supervisor to include relative .ini files
RUN mkdir /etc/supervisord.d
RUN echo [include] >> /etc/supervisord.conf
RUN echo 'files = /etc/supervisord.d/*.ini' >> /etc/supervisord.conf

# add sshd program to supervisord config
RUN echo [program:sshd] >> /etc/supervisord.d/ssh.ini
RUN echo 'command=/usr/sbin/sshd -D' >> /etc/supervisord.d/ssh.ini
RUN echo  >> /etc/supervisord.d/ssh.ini
RUN echo 'StrictHostKeyChecking no' >> /etc/ssh/ssh_config

# install apache load-balancer
ADD scripts /installations/scripts/
ENV INSTALLATIONS_FOLDER /installations/
RUN yum -y install gcc openssl openssl-devel libssl-dev
RUN /bin/bash -c "source /root/.bashrc &&\
/installations/scripts/install_apache_loadbalancer.sh"

# install selenium
RUN echo "Using Firefox 34.0.5"
RUN yum install -y bzip2
RUN wget https://ftp.mozilla.org/pub/firefox/releases/34.0.5/linux-x86_64/en-US/firefox-34.0.5.tar.bz2 -O /tmp/firefox-34.0.5.tar.bz2
RUN tar -vxjf /tmp/firefox-34.0.5.tar.bz2 -C /tmp
RUN ln -s /tmp/firefox/firefox /usr/bin/firefox
ENV PATH $JAVA_HOME/bin:$PATH
RUN yum install -y xorg-x11-server-Xvfb
RUN mkdir -p /selenium-webui

RUN yum install hostname -y

RUN echo "installing gtk2 libraries"
RUN yum install -y gtk2-2.24.31-1.el7.x86_64

EXPOSE 10000-10100
EXPOSE 9104
EXPOSE 7102
EXPOSE 4174
EXPOSE 22
EXPOSE 7000-7010
EXPOSE 7055
EXPOSE 2222
EXPOSE 3333
EXPOSE 2181
EXPOSE 3306

#create user to rum MySql
RUN yum install -y sudo
RUN useradd -c 'User that run mysql' -m -d /home/sgtest -s /bin/bash sgtest
RUN usermod -a -G root sgtest
RUN echo sgtest 'ALL=(ALL)   NOPASSWD: ALL' >> /etc/sudoers
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]

