/*
Author: Michael Bonner 
Date: 05/30/2019 
*/ 

package mortgageCalculator;

import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.AbstractMap;
import java.util.ArrayList;


public class MortgageCalculator {
    
    private static double annualInterestRate = 2.5;
    private static final Map<String, Integer> schedule2PaymentsPerYear = Stream.of(
        new AbstractMap.SimpleEntry<>("weekly", 4*12), 
        new AbstractMap.SimpleEntry<>("biweekly", 52/2),
        new AbstractMap.SimpleEntry<>("monthly", 12))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    private static final int minAmortizationPeriod = 5;
    private static final int maxAmortizationPeriod = 25;
    private static final double minDpBound = 500000.0;
    private static final double ltMinDpBoundRate = 0.05;
    private static final double gtMinDpBoundRate = 0.1;

    /*
Down Payment: Must be at least 5% of first $500k plus 10% of any amount above $500k (So $50k on a $750k
mortgage)
Amortization Period: Min 5 years, max 25 years
Payment schedule: Weekly, biweekly, monthly
DownPayment(/mortgage-amount): If included its value should be added to the maximum mortgage returned (optional)

Payment formula: P = L[c(1 + c)^n]/[(1 + c)^n - 1]
P = Payment
L = Loan Principal
c = Interest Rate
n is your number of payments (the number of months you will be paying the loan)
    */

    /**
     * 
     * @param askingPrice
     * @param downPayment
     * @param paymentSchedule
     * @param amortizationPeriod
     * @return
     */
    public double paymentAmount(double askingPrice, double downPayment, String paymentSchedule,  
    		int amortizationPeriod) {

    	ArrayList<String> errors = new ArrayList<String>();
    	final double minDownPayment = askingPrice < minDpBound ? ltMinDpBoundRate*askingPrice : 
    		ltMinDpBoundRate*minDpBound + gtMinDpBoundRate*(askingPrice - minDpBound);
        paymentSchedule = paymentSchedule.toLowerCase();
        
        // validate amortization_period, paymentSchedule
    	if (!schedule2PaymentsPerYear.containsKey(paymentSchedule)) {
    		errors.add("Payment schedule must be one of " + schedule2PaymentsPerYear.keySet().toString());
    	}
    	if (amortizationPeriod < minAmortizationPeriod || amortizationPeriod > maxAmortizationPeriod) {
    		errors.add("The amortization period must be >= 5 and less than or equal to 25.");
    	}
        // validate downPayment is less than the asking price.
    	if (downPayment > askingPrice) {
    		errors.add("The down payment cannot exceed the asking price.");
    	}
    	if (downPayment < 0) {
    		errors.add("The down payment must be larger than zero.");
    	}
    	if (askingPrice < 0) {
    		errors.add("The asking price must be larger than zero.");
    	}
    	//
    	if (downPayment < minDownPayment) {
    		errors.add(String.format("The down payment to aking price ratio must be greater than %6.2f", minDownPayment));
    	}
    	if (errors.size() > 0) {
    		StringJoiner sj = new StringJoiner(", ");
    		errors.stream().forEach(e -> sj.add(e));
    		throw new IllegalArgumentException(sj.toString());
    	}
        
        // calculate the number of payments n
    	int paymentsPerYear = schedule2PaymentsPerYear.get(paymentSchedule);
        double numPayments = amortizationPeriod * paymentsPerYear;
        
        // calculate insurance, add to principal, I am doing this after checking the minimum down 
        // payment, we might have to do this before!
        /*
		Mortgage insurance is required on all mortgages with less than 20% down. Insurance must be
		calculated and added to the mortgage principal. Mortgage insurance is not available for
		mortgages > $1 million.
		Mortgage insurance rates are as follows:
		Down payment Insurance Cost
		5-9.99% 3.15%
		10-14.99% 2.4%
		15%-19.99% 1.8%
		20%+ N/A
        */
        // TODO: externalize all hard coded numbers.
        double dp2apRatio = downPayment / askingPrice;
        double insurance = 0.;
        if (dp2apRatio < 0.2 && askingPrice < 1e6) {
        	if (dp2apRatio < 10.) 
        		insurance = 0.0315*askingPrice;
        	else if (dp2apRatio < 15.) 
        		insurance = 0.024*askingPrice;
        	else if (dp2apRatio < 20.) 
        		insurance = 0.018*askingPrice;
        }

        double principal = insurance + askingPrice - downPayment;   
        
        // annualInterestRate is an annual rate, we need to convert it to monthly
        // ... and then probably to weekly or biweekly.
        double rate = annualInterestRate / 100.0 / numPayments;
        
        // calculate the payment 
        // P = L[c(1 + c)^n]/[(1 + c)^n - 1]
        double annualInterestRateToNumPayments = Math.pow(1 + rate, numPayments);
        //double annualInterestRateToNumPayments = rate2Np(rate, (double)numPayments);
        double payment = principal * rate * annualInterestRateToNumPayments / (annualInterestRateToNumPayments - 1);
        
        System.out.printf("rate: %f paymentsPerYear: %d numPayments: %f principal: %f payment: %f\n", 
        		rate, paymentsPerYear, numPayments, principal, payment);
          
        //return resp(HttpStatus.OK, "payment", payment, "asking_price", askingPrice, 
        //        "down_payment", downPayment, "payment_schedule", paymentSchedule, 
        //        "amortization_period", amortizationPeriod, "num_payments", numPayments, 
        //        "payments_per_year", paymentsPerYear, "minimum_down_payment", minDownPayment,
        //        "downpayment_to_askingprice_ratio", dp2apRatio, "insurance", insurance, 
        //        "principal", String.format("%6.1f = %6.1f + %6.1f - %6.1f", principal, insurance, askingPrice, downPayment));
 
        return payment;
    }
    
    public double mortgageAmount(double payment, double downPayment, 
    		String paymentSchedule,  int amortizationPeriod) {
    	
    	 return 0.0;		
    }


    public double getannualInterestRate() {
        return MortgageCalculator.annualInterestRate; 
    }

    public boolean setannualInterestRate(double newAnnualInterestRate) {
        
        if (newAnnualInterestRate > 0.0 && newAnnualInterestRate <= 100.0) {
            MortgageCalculator.annualInterestRate = newAnnualInterestRate;
            return true;
        }
        else {
            return false;
        }
    }
}

