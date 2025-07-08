#!/bin/bash

set -e

sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc
sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/logs
echo "Checking if /opt/aws exists..."
ls -l /opt
sudo chown -R ubuntu:ubuntu /opt/aws

wget https://s3.us-west-2.amazonaws.com/amazoncloudwatch-agent-us-west-2/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb

echo "Configuring CloudWatch Agent..."
sudo cp /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
sudo cp /tmp/common-config.toml /opt/aws/amazon-cloudwatch-agent/etc/common-config.toml

echo "Setting up AWS credentials for CloudWatch..."
mkdir -p ~/.aws
sudo cp /tmp/credentials ~/.aws/credentials
sudo chmod 600 ~/.aws/credentials

echo "Fetching CloudWatch Agent configuration..."
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config -m ec2 -s -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json

echo "Starting CloudWatch Agent..."
sudo systemctl enable amazon-cloudwatch-agent
sudo systemctl start amazon-cloudwatch-agent

echo "CloudWatch Agent installation complete!"