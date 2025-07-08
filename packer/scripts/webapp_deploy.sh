#!/bin/bash

set -e

sudo mkdir -p /opt/webapp
echo "Checking if /opt/webapp exists..."
ls -l /opt

sudo groupadd csye6225
sudo useradd --system -g csye6225 -s /usr/sbin/nologin csye6225

# sudo rm /etc/nginx/sites-enabled/default
# sudo cp /tmp/nginx.conf /etc/nginx/sites-available/webapp.conf
# sudo ln -s /etc/nginx/sites-available/webapp.conf /etc/nginx/sites-enabled/
# sudo sed -i 's/worker_connections 768;/worker_connections 1024;/g' /etc/nginx/nginx.conf
# sudo sed -i 's/#\s*multi_accept on;/multi_accept on;/g' /etc/nginx/nginx.conf

echo "Checking if /tmp/webapp.jar exists..."
ls -l /tmp/webapp.jar
sudo cp /tmp/webapp.jar /opt/webapp/webapp.jar
sudo cp /tmp/webapp.service /etc/systemd/system/webapp.service

sudo systemctl daemon-reload
sudo systemctl enable webapp
sudo systemctl start webapp
# sudo systemctl enable nginx
# sudo systemctl start nginx

sudo chown -R csye6225:csye6225 /opt/webapp