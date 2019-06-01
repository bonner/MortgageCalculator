This guide walks you through the process of building a Docker image for running a Spring Boot application.

prerequisites: 
		1. Java 1.8 or above
		2. Latest version of Gradle or Maven
		3. Latest version of Docker
		4. If you are NOT using a Linux machine, you will need a virtualized server. Visit https://www.virtualbox.org/wiki/Downloads for Download and install

Follow the below steps in sequence.

NOTE: if any of the below commands fail with the permission denied error in Linux env, prefix <sudo> to each cmds to run the cmds in root

- if you are using maven, execute the below cmd to buid and start Spring Boot application-

		$ ./mvnw package && java -jar target/mortgate-calculator-0.1.0.jar

- Navigate to http://localhost:8080/swagger-ui.html#!/mortgage-calculator-controller to see the API documentation 


- To Containerize the Hello World Application (Refer the Dockerfile for details) run the cmds below which will build a Docker image in the name mydocker/spring-boot-docker:latest

- if you are using maven, execute the below cmd - 

		$ ./mvnw install dockerfile:build
		
 
- Run the docker image with the below cmd
	
		$ docker run -p 8081:8080 -t mortgagecalculator/mortgate-calculator
                 
- Navigate to http://localhost:8081/swagger-ui.html#!/mortgage-calculator-controller to see the API documentation 
- Here 8081 is the Docker port and 8080 is the Tomcat port where is Spring Boot Application is 	running. 

contact bonner.mike@gmail.com for more details and queries. 


			
