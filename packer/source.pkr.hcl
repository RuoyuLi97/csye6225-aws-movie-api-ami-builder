source "amazon-ebs" "mysql-ami"{
    ami_name = "mysql-ami"
    instance_type = var.instance_type
    region = var.aws_region
    source_ami = var.source_ami
    ssh_username = var.ssh_username
    associate_public_ip_address = true
}

source "amazon-ebs" "webapp-ami"{
    ami_name = "webapp-ami"
    instance_type = var.instance_type
    region = var.aws_region
    source_ami = var.source_ami
    ssh_username = var.ssh_username
    associate_public_ip_address = true
}