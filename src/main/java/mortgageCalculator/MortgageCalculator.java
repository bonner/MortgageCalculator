/*
Author: Michael Bonner 
Date: 05/30/2019 
*/ 
package mortgageCalculator;

import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Michael Bonner
 * @since 20190630
*/
public class MortgageCalculator {
    
    private static Map<?, ?> createMap(Object... keyValues) {
        assert (keyValues.length % 2 == 0);
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
    
    private static double annualInterestRate = 2.5;
    /*
    private static final Map<String, Integer> schedule2PaymentsPerYear = Stream.of(
        new AbstractMap.SimpleEntry<>("weekly", 52), 
        new AbstractMap.SimpleEntry<>("biweekly", 52/2),
        new AbstractMap.SimpleEntry<>("monthly", 12))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    */
    private static final Map<String, Double> schedule2PaymentsPerYear = new HashMap<String, Double>();
    static {
    	//schedule2PaymentsPerYear.put("weekly", 52.1429);
    	//schedule2PaymentsPerYear.put("biweekly", 52.1429/2.);
    	schedule2PaymentsPerYear.put("weekly", 52.);
    	schedule2PaymentsPerYear.put("biweekly", 26.);
    	schedule2PaymentsPerYear.put("monthly", 12.0);
    }
    private static final int minAmortizationPeriod = 5;
    private static final int maxAmortizationPeriod = 25;
    private static final double minDpBound = 500000.0;
    private static final double ltMinDpBoundRate = 0.05;
    private static final double gtMinDpBoundRate = 0.1;
    
    /**
     * 
     * @param paymentSchedule
     * @param amortizationPeriod
     * @param errors
     */
    private static void validateScheduleAndAmortization(String paymentSchedule, int amortizationPeriod,
            ArrayList<String> errors) {
        // validate amortization_period, paymentSchedule
        if (!schedule2PaymentsPerYear.containsKey(paymentSchedule.toLowerCase())) {
            errors.add("Payment schedule must be one of " + schedule2PaymentsPerYear.keySet().toString());
        }
        if (amortizationPeriod < minAmortizationPeriod || amortizationPeriod > maxAmortizationPeriod) {
            errors.add("The amortization period must be greater than or equal to 5 and less than or equal to 25.");
        }        
    }

    /**
     Get the recurring payment amount of a mortgage using the following formula
     
     Payment formula: P = L[c(1 + c)^n]/[(1 + c)^n - 1]
         P = Payment
         L = Loan Principal
         c = Interest Rate 
         n is the number of payments 
         
     Mortgage insurance is added to the principal for all mortgages with less than 20% down. 
     Mortgage insurance is not available for
     mortgages > $1 million.
    
     Mortgage insurance rates are as follows:
     Down payment Insurance Cost
     5-9.99% 3.15%
     10-14.99% 2.4%
     15%-19.99% 1.8%
     20%+ N/A
         
     * @param askingPrice 
     * @param downPayment Must be at least 5% of first $500k plus 10% of any amount above $500k (So $50k on a $750k
mortgage)
     * @param paymentSchedule Min 5 years, max 25 years
     * @param amortizationPeriod Weekly, biweekly, monthly
     * @return The mortgage payment amount
     */
    static public Map<?, ?> paymentAmount(double askingPrice, double downPayment, String paymentSchedule,  
            int amortizationPeriod) {

        ArrayList<String> errors = new ArrayList<String>();
        final double minDownPayment = askingPrice < minDpBound ? ltMinDpBoundRate*askingPrice : 
            ltMinDpBoundRate*minDpBound + gtMinDpBoundRate*(askingPrice - minDpBound);
        
        validateScheduleAndAmortization(paymentSchedule, amortizationPeriod, errors);
        
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
            errors.add(String.format("The down payment must be greater than %6.2f", minDownPayment));
        }
        if (errors.size() > 0) {
            StringJoiner sj = new StringJoiner(", ");
            errors.stream().forEach(e -> sj.add(e));
            throw new IllegalArgumentException(sj.toString());
        }
        
        // calculate the number of payments n
        double paymentsPerYear = schedule2PaymentsPerYear.get(paymentSchedule);
        double numPayments = amortizationPeriod * paymentsPerYear;
        
        // calculate insurance, add to principal, I am doing this after checking the minimum down 
        // payment, we might have to do this before!
        // TODO: externalize all hard coded numbers.
        double dp2apRatio = downPayment / askingPrice;
        double insurance = 0.;
        if (dp2apRatio < 0.2 && askingPrice < 1e6) {
            if (dp2apRatio < .10) 
                insurance = 0.0315*askingPrice;
            else if (dp2apRatio < .15) 
                insurance = 0.024*askingPrice;
            else if (dp2apRatio < .20) 
                insurance = 0.018*askingPrice;
        }

        double principal = insurance + askingPrice - downPayment;   
        
        // annualInterestRate is an annual rate, convert it to per payment.
        //double rate = annualInterestRate / 100.0 / numPayments;
        double rate = annualInterestRate / 100.0 / paymentsPerYear;
        
        // calculate the payment, P = L[c(1 + c)^n]/[(1 + c)^n - 1]
        double interestRateToNumPayments = Math.pow(1 + rate, numPayments);
        double payment = principal * rate * interestRateToNumPayments / (interestRateToNumPayments - 1);
        
        System.out.printf("rate: %f paymentsPerYear: %f numPayments: %f principal: %f payment: %f\n", 
                rate, paymentsPerYear, numPayments, principal, payment);
          
        return createMap("payment", payment, "num_payments", numPayments, "rate", rate,
                "payments_per_year", paymentsPerYear, "minimum_down_payment", minDownPayment,
                "downpayment_to_askingprice_ratio", dp2apRatio, "insurance", insurance, 
                "principal", String.format("%6.1f = %6.1f + %6.1f - %6.1f", principal, insurance, askingPrice, downPayment));
    }

    /**
     * Calculate the maximum mortgage amount.
     * L = P[(1 + c)^n - 1]/[c(1 + c)^n]
     * 
     * @param payment The desired payment for the given paymentSchedule.
     * @param paymentSchedule The payment schedule, valid values are: Weekly, biweekly, monthly.
     * @param amortizationPeriod The period, in years, the loan is to be paid off, Min 5 years, max 25 years.
     * @return the maximum mortgage amount
    */    
    static public Map<?, ?> mortgageAmount(double payment, double downPayment, 
            String paymentSchedule, int amortizationPeriod) {
        
        ArrayList<String> errors = new ArrayList<String>();
        
        validateScheduleAndAmortization(paymentSchedule, amortizationPeriod, errors);
        if (errors.size() > 0) {
            StringJoiner sj = new StringJoiner(", ");
            errors.stream().forEach(e -> sj.add(e));
            throw new IllegalArgumentException(sj.toString());
        }
        
        double paymentsPerYear = schedule2PaymentsPerYear.get(paymentSchedule.toLowerCase());
        double numPayments = amortizationPeriod * paymentsPerYear;
        double rate = annualInterestRate / 100.0 / paymentsPerYear;
        
        double interestRateToNumPayments = Math.pow(1 + rate, numPayments);
        double denominator = rate * interestRateToNumPayments / (interestRateToNumPayments - 1);
        double maxMortgageAmount = (payment / denominator) + downPayment;
        
        double maxMortgageAmount2 = payment * (interestRateToNumPayments - 1) / (rate * interestRateToNumPayments);
        
        return createMap("mortgage_amount", maxMortgageAmount,   
                "mortgage_amount2", maxMortgageAmount2, "annualInterestRate", annualInterestRate,
                "num_payments", numPayments, "rate", rate,
                "payments_per_year", paymentsPerYear);        
    }
    
    /**
     * Call mortgageAmount with a downPayment equal to zero.
     *
     * @param payment The desired payment for the given paymentSchedule.
     * @param paymentSchedule The payment schedule, valid values are: Weekly, biweekly, monthly.
     * @param amortizationPeriod The period, in years, the loan is to be paid off, Min 5 years, max 25 years.
     * @return the maximum mortgage amount
     */
    static public Map<?, ?> mortgageAmount(double payment, 
            String paymentSchedule, int amortizationPeriod) {
        return mortgageAmount(payment, 0.0, paymentSchedule, amortizationPeriod);
    }

    /**
     * Get the annual interest rate.
     * 
     * @return the annual interest rate.
     */
    static public double getAnnualInterestRate() {
        return MortgageCalculator.annualInterestRate; 
    }

    /**
     * Set the annual interest rate.
     * 
     * @param newAnnualInterestRate The new annual interest rate, must be greater than zero and less than or equal to 100.
     * @return True if the interest was set to newAnnualInterestRate
     */
    static public boolean setAnnualInterestRate(double newAnnualInterestRate) {
        
        if (newAnnualInterestRate > 0.0 && newAnnualInterestRate <= 100.0) {
            MortgageCalculator.annualInterestRate = newAnnualInterestRate;
            return true;
        }
        else {
            return false;
        }
    }
}

