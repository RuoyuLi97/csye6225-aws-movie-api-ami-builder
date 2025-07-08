packer {
    required_plugins{
        amazon = {
            version = ">= 1.2.8"
            source = "github.com/hashicorp/amazon"
        }
    }
}

build {
    sources = ["source.amazon-ebs.mysql-ami"]
    provisioner "shell" {
        inline = [
            "sudo apt-get update -y",
            "sudo apt-get install mysql-server -y",
            "sudo systemctl enable mysql",
            "sudo systemctl start mysql"
        ]
    }

    /*
    provisioner "file"{
        source = "./packer/cloudwatch_config_files/amazon-cloudwatch-agent.json"
        destination = "/tmp/amazon-cloudwatch-agent.json"
    }

    provisioner "file"{
        source = "./packer/cloudwatch_config_files/common-config.toml"
        destination = "/tmp/common-config.toml"
    }

    provisioner "file"{
        source = "./packer/cloudwatch_config_files/credentials"
        destination = "/tmp/credentials"
    }

    provisioner "shell" {
        script = "./packer/scripts/aws_cloudwatch_agent_setup.sh"
    }
    */
}

build {
    sources = ["source.amazon-ebs.webapp-ami"]

    provisioner "shell" {
        inline = [
            "sudo apt-get update -y",
            "sudo apt-get install mysql-client -y",
            "sudo apt-get install openjdk-21-jdk -y",
            # "sudo apt-get install nginx -y"
        ]
    }

    provisioner "file" {
        source = "packer/csye-app-3.4.1.jar"
        destination = "/tmp/webapp.jar"
    }

    provisioner "file" {
        source = "./packer/webapp.service"
        destination = "/tmp/webapp.service"
    }
    
    /*
    provisioner "file"{
        source = "./packer/nginx.conf"
        destination = "/tmp/nginx.conf"
    }
    */

    provisioner "shell" {
        script = "./packer/scripts/webapp_deploy.sh"
    }

    /*
    provisioner "file"{
        source = "./packer/cloudwatch_config_files/amazon-cloudwatch-agent.json"
        destination = "/tmp/amazon-cloudwatch-agent.json"
    }

    provisioner "file"{
        source = "./packer/cloudwatch_config_files/common-config.toml"
        destination = "/tmp/common-config.toml"
    }

    provisioner "file"{
        source = "./packer/cloudwatch_config_files/credentials"
        destination = "/tmp/credentials"
    }

    provisioner "shell" {
        script = "./packer/scripts/aws_cloudwatch_agent_setup.sh"
    }
    */
}