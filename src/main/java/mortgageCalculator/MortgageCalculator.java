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
 * MortgageCalculator - A class containing static methods for calculating mortgage payments and
 * amounts. 
 * 
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
    //The default annual interest rate to use for all calculations.
    private static double annualInterestRate = 2.5;
    
    // Map of payment schedule strings to number of payments per year.
    private static final Map<String, Integer> schedule2PaymentsPerYear = Stream.of(
        new AbstractMap.SimpleEntry<>("weekly", 52), 
        new AbstractMap.SimpleEntry<>("biweekly", 52/2),
        new AbstractMap.SimpleEntry<>("monthly", 12))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    
    private static final int minAmortizationPeriod = 5;
    private static final int maxAmortizationPeriod = 25;
    
    // Asking price bound for minimum down payment calculation 
    private static final double minDpBound = 500000.0;
    // Percentage for asking price amount below minDpBound 
    private static final double ltMinDpBoundRate = 0.05;
    // Percentage for remaining asking price about minDpBound 
    private static final double gtMinDpBoundRate = 0.1;
    
    // Insurance calculation down payment to asking price bounds.
    private static final double[] dp2atRatioBounds = new double[] {0.1, 0.15, 0.2};
    // Insurance percentages for bounds defined in dp2atRatioBounds
    private static final double[] insurancePercentages = new double[] {0.0315, 0.024, 0.018};

    /**
     * Get the recurring payment amount of a mortgage using the following formula
     * 
     * Payment formula: P = L[c(1 + c)^n]/[(1 + c)^n - 1]
     *     P = Payment
     *     L = Loan Principal
     *     c = Interest Rate 
     *     n is the number of payments 
     *    
     * Mortgage insurance is added to the principal for all mortgages with less than 20% down. 
     * Mortgage insurance is not available for
     * mortgages greater than $1 million.
     *
     * Mortgage insurance rates are as follows:
     * Down payment Insurance Cost
     * 5-9.99% 3.15%
     * 10-14.99% 2.4%
     * 15%-19.99% 1.8%
     * 20%+ N/A
     *    
     * @param askingPrice The total amount of money required, the loan is equal to this amount plus 
     *        insurance minus the down payment.
     * @param downPayment Must be at least 5% of first $500k plus 10% of any amount above $500k 
     *        (So $50k on a $750k mortgage)
     * @param paymentSchedule The payment schedule, valid values are: Weekly, biweekly, monthly.
     * @param amortizationPeriod The period, in years, the loan is to be paid off, Min 5 years, max 25 years.
     * @param annualInterestRate The annual insurance rate, as percentage ie 2.5%.
     * @return The mortgage payment amount
     */
    static public Map<?, ?> paymentAmount(double askingPrice, double downPayment, String paymentSchedule,  
            int amortizationPeriod, double annualInterestRate) {

        ArrayList<String> errors = new ArrayList<String>();
        final double minDownPayment = askingPrice < minDpBound ? ltMinDpBoundRate*askingPrice : 
            ltMinDpBoundRate*minDpBound + gtMinDpBoundRate*(askingPrice - minDpBound);
        
        validateScheduleAndAmortization(paymentSchedule, amortizationPeriod, errors);
        
        if (!validateInterestRate(annualInterestRate)) {
        	errors.add("The interest rate must be greater than zero and less than or equal to 100.");
        }
        if (downPayment > askingPrice) {
            errors.add("The down payment cannot exceed the asking price.");
        }
        if (downPayment < 0) {
            errors.add("The down payment must be larger than zero.");
        }
        if (askingPrice < 0) {
            errors.add("The asking price must be larger than zero.");
        }
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
        
        // calculate insurance, add to principal
        // I am doing this after checking the minimum down payment, should this be done before, ie 
        // is the minimum down payment a function of asking price or asking price + insurance?
        double insurance = calculateInsurance(askingPrice, downPayment);
        double principal = insurance + askingPrice - downPayment;   
        
        // annualInterestRate is an annual rate, convert it to per payment.
        double rate = annualInterestRate / 100.0 / paymentsPerYear;
        
        // calculate the payment, P = L[c(1 + c)^n]/[(1 + c)^n - 1]
        double interestRateToNumPayments = Math.pow(1 + rate, numPayments);
        double payment = principal * rate * 
        		interestRateToNumPayments / (interestRateToNumPayments - 1);
          
        return createMap("payment", payment, 
        		"num_payments", numPayments, 
        		"rate", rate,
                "payments_per_year", paymentsPerYear, 
                "minimum_down_payment", minDownPayment,
                "downpayment_to_askingprice_ratio", downPayment / askingPrice,
                "insurance", insurance, 
                "loan_total", numPayments*payment,
                "principal", String.format("%6.1f = %6.1f + %6.1f - %6.1f", principal, insurance, askingPrice, downPayment));
    }
    
    /**
     * Calls paymentAmount using the class variable annualInterestRate. See that method for 
     * parameter/return details.
     */
    static public Map<?, ?> paymentAmount(double askingPrice, double downPayment, String paymentSchedule,  
            int amortizationPeriod) {
        return paymentAmount(askingPrice, downPayment, paymentSchedule, amortizationPeriod, 
        		MortgageCalculator.annualInterestRate);	
    }

    /**
     * Calculate the maximum mortgage amount.
     * L = P[(1 + c)^n - 1]/[c(1 + c)^n]
     * 
     * @param payment The desired payment for the given paymentSchedule.
     * @param downPayment The down payment is added to the maximum mortgage amount.
     * @param paymentSchedule The payment schedule, valid values are: Weekly, biweekly, monthly.
     * @param amortizationPeriod The period, in years, the loan is to be paid off, Min 5 years, max 25 years.
     * @param annualInterestRate The annual insurance rate, as percentage ie 2.5%.
     * 
     * @return the maximum mortgage amount
    */    
    static public Map<?, ?> mortgageAmount(double payment, double downPayment, 
            String paymentSchedule, int amortizationPeriod, double annualInterestRate) {
        
        ArrayList<String> errors = new ArrayList<String>();
        
        validateScheduleAndAmortization(paymentSchedule, amortizationPeriod, errors);
        if (!validateInterestRate(annualInterestRate))
        	errors.add("The interest rate must be greater than zero and less than or equal to 100.");
        
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
        
        return createMap("mortgage_amount", maxMortgageAmount,   
                "num_payments", numPayments, 
                "rate", rate,
                "payments_per_year", paymentsPerYear);        
    }
    
    /**
     * Call mortgageAmount with the default annual interest rate and a downPayment equal to zero.
     * See that method for parameter/return
     * details.
     */
    static public Map<?, ?> mortgageAmount(double payment, 
            String paymentSchedule, int amortizationPeriod) {
        return mortgageAmount(payment, 0.0, paymentSchedule, amortizationPeriod, 
        		MortgageCalculator.annualInterestRate);
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
        
        if (validateInterestRate(newAnnualInterestRate)) {
            MortgageCalculator.annualInterestRate = newAnnualInterestRate;
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Validate payment schedule and amortization amount.
     * 
     * @param paymentSchedule The payment schedule, valid values are: Weekly, biweekly, monthly.
     * @param amortizationPeriod The period, in years, the loan is to be paid off, Min 5 years, max 25 years.
     * @param errors If values are invalid, error messages, are added to this list, the caller is 
     * responsible to check this list and handle the errors appropriately.
     */
    private static void validateScheduleAndAmortization(String paymentSchedule, int amortizationPeriod,
            ArrayList<String> errors) {
        
        if (!schedule2PaymentsPerYear.containsKey(paymentSchedule.toLowerCase())) {
            errors.add("Payment schedule must be one of " + schedule2PaymentsPerYear.keySet().toString());
        }
        if (amortizationPeriod < minAmortizationPeriod || amortizationPeriod > maxAmortizationPeriod) {
            errors.add("The amortization period must be greater than or equal to 5 and less than or equal to 25.");
        }        
    }
    
    /**
     * Validate that the interest rate is greater than zero and less than or equal to 100.
     * 
     * @return True if the interest rate is valid.
     */
    private static boolean validateInterestRate(double interestRate) {
    	return interestRate > 0.0 && interestRate <= 100.0;    	
    }

    /**
     * Calculate the required insurance, only applicable for asking prices below 1million.
     * 
     * @param askingPrice The total amount of money required, the loan is equal to this amount plus 
     *        insurance minus the down payment.
     * @param downPayment Must be at least 5% of first $500k plus 10% of any amount above $500k 
     *        (So $50k on a $750k mortgage)
     * @return The insurance amount for the loan.
     */
    private static double calculateInsurance(double askingPrice, double downPayment) {

        double dp2apRatio = downPayment / askingPrice, insurance = 0.;
      
        for (int i = 0; i < dp2atRatioBounds.length; i++) {
        	if (dp2apRatio < dp2atRatioBounds[i]) {
        		insurance = insurancePercentages[i]*askingPrice;
        		break;
        	}
        }
        return insurance;
    }
}

