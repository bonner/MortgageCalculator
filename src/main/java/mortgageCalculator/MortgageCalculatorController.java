/*
Author: Michael Bonner 
Date: 05/30/2019 
*/ 

package mortgageCalculator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;

@SpringBootApplication
@RestController
public class MortgageCalculatorController {

    protected static ResponseEntity<Map<?, ?>> resp(HttpStatus status, Object... keyValues) {
        assert (keyValues.length % 2 == 0);
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return new ResponseEntity<Map<?, ?>>(map, status);
    }
    protected static ResponseEntity<Map<?, ?>> resp(HttpStatus status, Map<?, ?> map) {
        return new ResponseEntity<Map<?, ?>>(map, status);
    }
    

    @ApiOperation(value = "Get the recurring payment amount of a mortgage", response = Map.class)
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully calculated mortgage payment"),
            @ApiResponse(code = 400, message = "Unexpected request data")})
    @RequestMapping(path = "/payment-amount", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Map<?, ?>> paymentAmount(
            @ApiParam(defaultValue = "500000") @RequestParam("asking_price") double askingPrice, 
            @ApiParam(defaultValue = "70000") @RequestParam("down_payment") double downPayment, 
            @ApiParam(defaultValue = "monthly", allowableValues = "weekly, biweekly, monthly") @RequestParam("payment_schedule") String paymentSchedule, 
            @ApiParam(defaultValue = "25", value = "The number of years to paty off the loan, min 5 years, max 25 years", allowableValues = "range[5,25]") @RequestParam("amortization_period") int amortizationPeriod) {

        try {
            Map<?, ?> map = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, amortizationPeriod);
            return resp(HttpStatus.OK, map);            
        }
        catch (IllegalArgumentException e) {
            return resp(HttpStatus.BAD_REQUEST, "error", e.getMessage());
        }
    }
    
    @ApiOperation(value = "Get the maximum mortgage amount", response = Map.class)
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully calculated the maximum mortgage amount"),
            @ApiResponse(code = 400, message = "Unexpected request data")})
    @RequestMapping(path = "/mortgage-amount", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Map<?, ?>> mortgageAmount(
            @ApiParam(defaultValue = "2000") @RequestParam("payment") double payment, 
            @ApiParam(defaultValue = "0") @RequestParam(name = "down_payment", required = false) double downPayment, 
            @ApiParam(defaultValue = "monthly", allowableValues = "weekly, biweekly, monthly") @RequestParam("payment_schedule") String paymentSchedule, 
            @ApiParam(defaultValue = "25", value = "The number of years to paty off the loan, min 5 years, max 25 years", allowableValues = "range[5,25]") @RequestParam("amortization_period") int amortizationPeriod) {
         
         try {
            Map<?, ?> map = MortgageCalculator.mortgageAmount(payment, downPayment, paymentSchedule, amortizationPeriod);
            return resp(HttpStatus.OK, map);            
        }
        catch (IllegalArgumentException e) {
            return resp(HttpStatus.BAD_REQUEST, "error", e.getMessage());
        }
    }

    @ApiOperation(value = "Get interest rate", response = Map.class)
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully retrieved interest rate", response = Map.class)})
    @RequestMapping(path = "/interest-rate", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Map<?, ?>> getAnnualInterestRate() {
        return resp(HttpStatus.OK, "interest_rate", MortgageCalculator.getAnnualInterestRate()); 
    }

    @ApiOperation(value = "Set interest rate", response = Map.class)
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully set interest rate"),
            @ApiResponse(code = 400, message = "Invalid interest rate"),
            })
    @RequestMapping(path = "/interest-rate/{annualInterestRate}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Map<?, ?>> setannualInterestRate(
            @ApiParam(value = "The new interest rate, must be greater than 0 and less than or equal to 100.", allowableValues = "range[0,100]") 
            @PathVariable(name = "annualInterestRate", required = true) double newAnnualInterestRate) {
        
        double oldAnnualInterestRate = MortgageCalculator.getAnnualInterestRate();
        if (MortgageCalculator.setAnnualInterestRate(newAnnualInterestRate)) 
            return resp(HttpStatus.OK, "old_interest_rate", oldAnnualInterestRate, "new_interest_rate", newAnnualInterestRate); 
        else 
            return resp(HttpStatus.BAD_REQUEST, "error", 
                    String.format("The intest rate, %1.3f, must be greater than zero and less than or equal to 100", newAnnualInterestRate));
    }

    public static void main(String[] args) {
        SpringApplication.run(MortgageCalculatorController.class, args);
    }

}
