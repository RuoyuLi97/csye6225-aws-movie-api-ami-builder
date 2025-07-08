# CSYE6225 - Movie API AMI Builder
### Network and Cloud Computing Course Project

## Course Context
This project was developed as part of **CSYE6225 - Network and Cloud Computing** at Northeastern University, demonstrating practical application of immutable infrastructure principles and custom AMI creation using Packer.

## Overview
Packer templates for building custom AMIs with pre-configured Spring Boot movie API application, MySQL database, and CloudWatch monitoring for automated AWS deployment.

### Technical Specifications
- **EC2 Instance Type**: t2.micro (free tier eligible)
- **Operating System**: Ubuntu (latest)
- **Programming Language**: Java JDK 21
- **Web Server Framework**: Spring Boot
- **Database**: MySQL Server
- **Reverse Proxy**: Nginx
- **Infrastructure**: Packer for image building, GitHub Actions for automation

## Architecture
- **Webapp AMI**: Pre-configured with Spring Boot application, Java runtime, and CloudWatch agent
- **MySQL AMI**: Pre-configured with MySQL server, database setup, and monitoring capabilities
- **Immutable Infrastructure**: AMIs built once and deployed multiple times without modification
- **Monitoring**: CloudWatch agent pre-installed for comprehensive system metrics

## AMI Components

### Webapp AMI Configuration
- **Java Runtime**: OpenJDK 21 for Spring Boot application
- **Application**: Movie ratings API with JWT authentication and multiple endpoints
- **Reverse Proxy**: Nginx configuration for load balancing and SSL termination
- **Service Management**: Systemd service for automatic application startup
- **CloudWatch Agent**: Comprehensive system metrics collection
- **Security**: Spring Security with JWT authentication and authorization

### Application Features
- **Authentication**: JWT-based user authentication with secure token management
- **Movie API**: Complete CRUD operations for movie data management
- **Ratings System**: Movie rating and review functionality
- **Metadata Service**: EC2 instance metadata exposure for load balancer testing
- **Health Monitoring**: Dedicated health check endpoint for load balancer integration
- **External Links**: IMDB and TMDB integration for movie metadata

## File Structure
```
├── packer/                                 # Packer configuration directory
│   ├── build.pkr.hcl                       # Main Packer build configuration
│   ├── source.pkr.hcl                      # Source AMI and instance configuration
│   ├── variables.pkr.hcl                   # Build variables and parameters
│   ├── nginx.conf                          # Nginx reverse proxy configuration
│   ├── webapp.service                      # Systemd service configuration
│   ├── cloudwatch_config_files/            # CloudWatch monitoring setup
│   │   ├── amazon-cloudwatch-agent.json    # CloudWatch agent configuration
│   │   ├── common-config.toml              # CloudWatch common configuration
│   │   └── credentials                     # AWS credentials template
│   └── scripts/                            # Setup and deployment scripts
│       ├── aws_cloudwatch_agent_setup.sh   # CloudWatch agent installation
│       └── webapp_deploy.sh                # Application deployment script
├── scripts/                                # Database and ingestion scripts
│   ├── ingest_movies.sh                    # Movie data ingestion script
│   └── table_creation.sql                  # Database schema creation
├── src/main/java/com/example/              # Spring Boot application source
│   ├── controller/                         # REST API controllers
│   │   ├── AuthController.java             # User authentication endpoints
│   │   ├── HealthCheckController.java      # Health check endpoint
│   │   ├── LinkController.java             # External links API
│   │   ├── MetadataController.java         # EC2 metadata endpoint
│   │   ├── MovieController.java            # Movie data API
│   │   └── RatingController.java           # Movie ratings API
│   ├── model/                              # Data models and entities
│   │   ├── Link.java                       # External link model
│   │   ├── Metadata.java                   # EC2 metadata model
│   │   ├── Movie.java                      # Movie entity model
│   │   └── Rating.java                     # Rating entity model
│   ├── security/                           # Security and authentication
│   │   ├── GenerateSecret.java             # JWT secret generation
│   │   ├── JwtFilter.java                  # JWT authentication filter
│   │   ├── JwtService.java                 # JWT token service
│   │   └── SecurityConfig.java             # Spring Security configuration
│   └── Main.java                           # Application entry point
├── src/main/resources/                     # Application configuration
│   └── application.yaml                    # Spring Boot application config
├── .github/workflows/                      # GitHub Actions workflows
│   ├── packer.yaml                         # AMI build automation workflow
│   └── continuous-deploy.yaml              # Full CI/CD pipeline implementation
├── .gitignore                              # Git ignore configuration
├── pom.xml                                 # Maven build configuration
└── README.md                               # This file
```

## Build Process

### Automated Build (GitHub Actions)
**Option 1 - AMI Only (`packer.yaml`)**:
1. **Application Build**: Maven compiles Spring Boot application
2. **AMI Creation**: Packer builds both webapp and MySQL AMIs
3. **Configuration**: CloudWatch agent and monitoring setup
4. **Validation**: AMI functionality verification

**Option 2 - Full CI/CD Pipeline (`continuous-deploy.yaml`)**:
1. **Infrastructure Provisioning**: VPC, subnets, security groups creation
2. **AMI Building**: MySQL AMI creation with database setup
3. **Application Deployment**: Webapp instance with Java application
4. **Database Configuration**: EBS volume mounting and MySQL setup
5. **End-to-end Testing**: Complete infrastructure validation

### Manual Build
```bash
# Prerequisites
packer init ./packer
packer validate ./packer

# Build MySQL AMI
packer build -only=amazon-ebs.mysql-ami ./packer

# Build Webapp AMI  
packer build -only=amazon-ebs.webapp-ami ./packer
```

## Configuration Requirements

### Required Secrets (GitHub Actions)
```yaml
AWS_ACCESS_KEY_ID: Valid AWS access key for AMI creation and deployment
AWS_SECRET_ACCESS_KEY: Valid AWS secret key for AMI creation and deployment
DATABASE_PASSWORD: MySQL database password for application configuration
DATABASE_USER_NAME: MySQL database username for application setup
EC2_SSH_KEY: Private SSH key for EC2 instance access and configuration
```

**⚠️ Important Note**: All secrets in this repository's AWS_DEPLOYMENT environment contain placeholder/invalid values for security purposes. To build AMIs or run CI/CD workflows, you must:
1. Configure valid AWS credentials with EC2, IAM, and VPC permissions
2. Generate valid SSH key pair and configure in AWS
3. Set appropriate database credentials for your deployment
4. Update all GitHub repository secrets with actual values
5. Ensure AWS account has sufficient permissions for full infrastructure provisioning

## CloudWatch Monitoring
Pre-configured metrics collection includes:
- **CPU Metrics**: Usage by type (system, user, idle, iowait)
- **Memory Metrics**: Used percentage and swap utilization  
- **Disk Metrics**: Used percentage for mounted filesystems
- **Network Metrics**: Bytes sent/received
- **Custom Metrics**: Application-specific monitoring

## Deployment Integration
These AMIs are designed to work with the companion [Terraform infrastructure repository](https://github.com/RuoyuLi97/csye6225-aws-terraform-infrastructure) for complete automated deployment:

### Integrated Workflow
1. **AMI Creation**: This repository builds custom AMIs with pre-configured applications
2. **Infrastructure Deployment**: Terraform repository deploys AMIs with load balancing and networking
3. **Automated Provisioning**: Complete infrastructure provisioning using custom AMIs
4. **Monitoring Integration**: Integrated CloudWatch dashboards and metrics collection

### Deployment Options
- **Manual AMI Building**: Use `packer.yaml` workflow to build AMIs, then deploy via Terraform repository
- **Full CI/CD Pipeline**: Use `continuous-deploy.yaml` for complete infrastructure provisioning
- **Production Deployment**: Use Terraform repository with pre-built AMIs for scalable deployment

**Note**: For production deployments, it's recommended to use the [Terraform infrastructure repository](https://github.com/RuoyuLi97/csye6225-aws-terraform-infrastructure) which provides load balancing across multiple instances, advanced networking, and comprehensive monitoring.

## Performance Optimizations
- **Pre-installed Dependencies**: Faster instance launch times
- **Optimized Configuration**: Database and application tuning
- **Security Hardening**: Minimal attack surface with security updates
- **Monitoring Ready**: Immediate metrics collection upon deployment

## Security Features
- **Minimal Base Image**: Ubuntu with only required packages
- **Security Updates**: Latest patches applied during build
- **Access Controls**: Proper file permissions and user management
- **Secrets Management**: No hardcoded credentials in AMI

## Tech Stack
Packer | AWS | Ubuntu | Java | Spring Boot | MySQL | Nginx | CloudWatch | GitHub Actions