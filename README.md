Name: Ruoyu Li

EC2 instance type: t2.micro

OS: ubuntu

Language: java JDK 21

Web Server Framework Used: Spring Boot

Build and Deploy instructions
- work flow steps:
    1. Set up EC2 Instance: 
        The workflow creates an EC2 instance in the us-west-2 region using a specific AMI. It configures AWS credentials and security group for EC2 access.
    
    2. Build and Package Application:
        The build job checks out the code and sets up JDK 21. It uses Maven to compile and package the application into a .jar file.
    3. Deploy to EC2:
        Once the EC2 instance is created and the application is built, the deployment job will:
        - Download SSH info from the previous job.
        - Use SSH to copy the .jar file to the EC2 instance.
        - Run the application on the EC2 instance in the background using nohup and java -jar.
- After the deployment completes, the application should be accessible at the EC2 public IP address on port 8080.