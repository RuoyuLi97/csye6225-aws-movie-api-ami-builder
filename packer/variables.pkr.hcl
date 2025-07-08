variable "aws_region" {
    type = string
    default = "us-west-2"
}

variable "ssh_username" {
    type = string
    default = "ubuntu"
}

variable "instance_type" {
    type = string
    default = "t2.micro"
}

variable "source_ami" {
    type = string
    default = "ami-00c257e12d6828491"
}