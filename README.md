# Mortgage Calculator REST API

This is a spring boot application that implements a RESTful API that calculates the payment for a given asking price and the maximum mortgage for a desired payment amount.
The following equation is used to do these calculations:

     Payment formula: P = L[c(1 + c)^n]/[(1 + c)^n - 1]
          P = Payment
          L = Loan Principal
          c = Interest Rate 
          n = the number of payments 
Mortgage insurance is added to the principal for all mortgages with less than 20% down. 
Mortgage insurance is not available for mortgages greater than $1 million.
Mortgage insurance rates are as follows:

     Down payment Insurance Cost
       5-9.99% 3.15%
       10-14.99% 2.4%
       15%-19.99% 1.8%
       20%+ N/A
       
For _/payment-amount_, the down payment must be at least 5% of first $500k plus 10% of any amount above $500k (So $50k on a $750k
mortgage).

For _/payment-amount_ and _/mortgage-amount_, the amortization period can be within 5 to 25 years. Valid payment schedules are: Weekly, biweekly, monthly.

For _/morgage-amount_, if the down payment is included its value is added to the maximum mortgage returned.

Full API usage can be found at http://localhost:8081/swagger-ui.html#!/mortgage-calculator-controller once the application is running.

A postman collection is also available in [Mortgage Calculator.postman_collection.json](https://github.com/bonner/MortgageCalculator/blob/master/Mortgage%20Calculator.postman_collection.json) that demonstrates API usage.
     
The API is currently publicly available at https://mortgage-calculator-96513.herokuapp.com, API documentation is at https://mortgage-calculator-96513.herokuapp.com/swagger-ui.html.

The EclEmma eclipse plugin was used to calculate test coverage, currently coverage of the MortgageCalculator class is >98%.

## Prerequisites: 
* Java 10.0.2
* Maven 3.6.1
* Docker 18.09.2
* If you are NOT using a Linux machine, you will need a virtualized server. Visit https://www.virtualbox.org/wiki/Downloads for Download and install

## Building

Follow the below steps in sequence.

NOTE: if any of the below commands fail with the permission denied error in Linux env, prefix <sudo> to each cmds to run the cmds in root

- Execute the below cmd to buid and start the application

		$ ./mvnw package && java -jar target/mortgate-calculator-0.1.0.jar

- Navigate to http://localhost:8080/swagger-ui.html#!/mortgage-calculator-controller to see the API documentation 


- To containerize the mortgate calculator Application (Refer the Dockerfile for details) run the cmds below which will build a Docker image in the name mortgagecalculator/mortgate-calculator:latest


- execute the below cmd 

		$ ./mvnw install dockerfile:build
		
 
- Run the docker image with the below cmd
	
		$ docker run -p 8081:8080 -t mortgagecalculator/mortgate-calculator
                 
- Navigate to http://localhost:8081/swagger-ui.html#!/mortgage-calculator-controller to see the API documentation 
- Here 8081 is the Docker port and 8080 is the Tomcat port where the application is running. 

contact bonner.mike@gmail.com for more details and inquiries. 
