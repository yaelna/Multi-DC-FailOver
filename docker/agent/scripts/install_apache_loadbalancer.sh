#!/bin/sh
set -e

APACHE_HOME_DIR=${INSTALLATIONS_FOLDER}/apachehome
APACHE_INSTALLATION_DIR=${INSTALLATIONS_FOLDER}/apacheinstallation

mkdir -p ${APACHE_HOME_DIR}
mkdir -p ${APACHE_INSTALLATION_DIR}
cd ${APACHE_INSTALLATION_DIR}

wget https://xap-test.s3.amazonaws.com/installations/apachelb/httpd-2.2.29.tar.gz
tar -zxvf httpd-2.2.29.tar.gz
cd httpd-2.2.29

./configure --enable-proxy=shared  --enable-proxy-connect=shared --enable-proxy-balancer=shared --enable-proxy-http=shared --enable-ssl --enable-so --prefix=${APACHE_HOME_DIR}
make
make install

cd ${APACHE_HOME_DIR}
sed -i -e 's/Listen 80/Listen 7777/g' conf/httpd.conf

mkdir -p ${APACHE_HOME_DIR}/conf/gigaspaces/


# add properties required by xap
echo "ProxyPass /balancer !

# Proxy Management

<Location /balancer>
SetHandler balancer-manager

Order Deny,Allow
Deny from all
Allow from all
</Location>

ProxyStatus On
<Location /status>
SetHandler server-status

Order Deny,Allow
Deny from all
Allow from all
</Location>" >> conf/httpd.conf

echo "Include conf/gigaspaces/*.conf" >> conf/httpd.conf


rm -rf ${APACHE_INSTALLATION_DIR}