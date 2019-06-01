package mortgageCalculator;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

public class MortgageCalculatorTest {
	
	@Test
	public void testGetAnnualInterestRate() {
		double ir = MortgageCalculator.getAnnualInterestRate();
		assertEquals(ir, 2.5, 1e-6);
	}
	
	@Test
	public void testSetAnnualInterestRate() {
		assertTrue(MortgageCalculator.setAnnualInterestRate(5.0));
		assertTrue(MortgageCalculator.setAnnualInterestRate(2.5));
	}
	
	@Test
	public void testSetAnnualInterestRateNegative() {
		assertFalse(MortgageCalculator.setAnnualInterestRate(-5.0));
		assertFalse(MortgageCalculator.setAnnualInterestRate(105.0));
	}	
	
	@Test
	public void testPaymentAmount() {
		
		double askingPrice = 500000, downPayment = 100000, rate = 7.;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		// 2802 is the value from https://www.ratesupermarket.ca/mortgages/payment_calculator_results?province=8&mortgage_value=400000&mortgage_rate=7&payment_type=monthly&amortization_period=25
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod, rate);
		assertTrue(result.containsKey("payment"));
		Object value = result.get("payment");
		double payment = Double.parseDouble(value.toString());
		System.out.printf("monthly payment: %f\n", payment);
		assertEquals(2802, payment, 30);
		
		//1293 is the value https://www.ratesupermarket.ca/mortgages/payment_calculator_results?province=8&mortgage_value=400000&mortgage_rate=7&payment_type=bi_weekly&amortization_period=25
		paymentSchedule = "biweekly";  
		result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod, rate);
		assertTrue(result.containsKey("payment"));
		value = result.get("payment");
		payment = Double.parseDouble(value.toString());
		System.out.printf("bi-weekly payment: %f\n", payment);
		assertEquals(1293, payment, 15);	
		
		// 647 is the value from https://www.ratesupermarket.ca/mortgages/payment_calculator_results?province=8&mortgage_value=400000&mortgage_rate=7&payment_type=weekly&amortization_period=25
		paymentSchedule = "weekly";  
		result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod, rate);
		assertTrue(result.containsKey("payment"));
		value = result.get("payment");
		payment = Double.parseDouble(value.toString());
		System.out.printf("weekly payment: %f\n", payment);
		assertEquals(647, payment, 5);	
		
	}
	
	@Test
	public void testPaymentAmountDefaultRate() {
		
		double askingPrice = 500000, downPayment = 100000;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		// 1792 is the value from https://www.ratesupermarket.ca/mortgages/payment_calculator_results?province=8&mortgage_value=400000&mortgage_rate=2.5&payment_type=monthly&amortization_period=25
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod);
		
		assertTrue(result.containsKey("payment"));
		Object value = result.get("payment");
		double payment = Double.parseDouble(value.toString());
		System.out.printf("monthly payment: %f\n", payment);
		assertEquals(payment, 1792, 3);
	}

	@Test
	public void testPaymentAmountLessThanMinBound() {
		
		double askingPrice = 400000, downPayment = 100000;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		// 1433 is the value from https://www.ratesupermarket.ca/mortgages/payment_calculator_results?province=8&mortgage_value=300000&mortgage_rate=2.5&payment_type=monthly&amortization_period=25
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod);
		
		assertTrue(result.containsKey("payment"));
		Object value = result.get("payment");
		double payment = Double.parseDouble(value.toString());
		System.out.printf("monthly payment: %f\n", payment);
		assertEquals(payment, 1344, 3);
	}
	
	@Test
	public void testMortgageAmount() {

		double askingPrice = 500000, downPayment = 100000, rate = 5.;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod, rate);	
		
		Object value = result.get("payment");
		double payment = Double.parseDouble(value.toString());
		System.out.printf("monthly payment: %f\n", payment);

		result = MortgageCalculator.mortgageAmount(payment, downPayment, paymentSchedule, 
				amortizationPeriod, rate);	
		value = result.get("mortgage_amount");
		double maxMortgage = Double.parseDouble(value.toString());
		System.out.printf("max mortgage: %f\n", maxMortgage);
		
		assertEquals(maxMortgage, askingPrice, 1);
	}
	
	@Test
	public void testMortgageAmountWeekly() {

		double askingPrice = 500000, downPayment = 100000;
		String paymentSchedule = "weekly";  
		int amortizationPeriod = 25;
		double rate = 5.;
		
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod, rate);	
		
		Object value = result.get("payment");
		double payment = Double.parseDouble(value.toString());
		System.out.printf("monthly payment: %f\n", payment);

		result = MortgageCalculator.mortgageAmount(payment, downPayment, paymentSchedule, 
				amortizationPeriod, rate);	
		value = result.get("mortgage_amount");
		double maxMortgage = Double.parseDouble(value.toString());
		System.out.printf("max mortgage: %f\n", maxMortgage);
		
		assertEquals(maxMortgage, askingPrice, 1);
	}
	
	@Test
	public void testMortgageAmountUsingDefaults() {

		double askingPrice = 500000, downPayment = 100000;
		String paymentSchedule = "weekly";  
		int amortizationPeriod = 25;
		double rate = 5.;
		
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod);	
		
		Object value = result.get("payment");
		double payment = Double.parseDouble(value.toString());
		System.out.printf("monthly payment: %f\n", payment);

		result = MortgageCalculator.mortgageAmount(payment, paymentSchedule, amortizationPeriod);	
		value = result.get("mortgage_amount");
		double maxMortgage = Double.parseDouble(value.toString());
		System.out.printf("max mortgage: %f\n", maxMortgage);
		
		assertEquals(askingPrice - downPayment, maxMortgage, 1);
	}
	
	@Test 
	public void testMinDownPayment() {
		double askingPrice = 750000, downPayment = 50000;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod);	
		
		double minimumDownPayment = Double.parseDouble(result.get("minimum_down_payment").toString());
		System.out.printf("minimum_down_payment: %f\n", minimumDownPayment);	
		
		assertEquals(minimumDownPayment, 50000.0, 1e-6);
	}

	@Test 
	public void testMinDownPaymentValidation() {
		double askingPrice = 750000, downPayment = 49000;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, 
		    		paymentSchedule, amortizationPeriod);
		    fail("Calling paymentAmount with a down payment less than the minimum should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
	}
	
	@Test 
	public void testScheduleValidation() {
		double askingPrice = 750000, downPayment = 49000;
		String paymentSchedule = "dne";  
		int amortizationPeriod = 25;
		
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, 
		    		paymentSchedule, amortizationPeriod);
		    fail("Calling paymentAmount with a unknown payment schedule should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
	}

	@Test 
	public void testAmortizationValidationMax() {
		double askingPrice = 750000, downPayment = 49000;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 30;
		
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, 
		    		paymentSchedule, amortizationPeriod);
		    fail("Calling paymentAmount with an invalid amortization value should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
	}
	
	@Test 
	public void testAmortizationValidationMin() {
		double askingPrice = 750000, downPayment = 49000;
		String paymentSchedule = "weekly";  
		int amortizationPeriod = 2;
		
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, 
		    		paymentSchedule, amortizationPeriod);
		    fail("Calling paymentAmount with an invalid amortization value should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
	}
	
	@Test 
	public void testNegativePricendDownpaymentValidation() {
		double askingPrice = -100, downPayment = -120;
		String paymentSchedule = "weekly";  
		int amortizationPeriod = 20;
		
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, 
		    		paymentSchedule, amortizationPeriod);
		    fail("Calling paymentAmount with an invalid amortization value should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			//e.printStackTrace();
		}
	}
	
	@Test 
	public void testInterestRateValidation() {
		double askingPrice = 750000, downPayment = 49000, rate = 101;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, 
		    		paymentSchedule, amortizationPeriod, rate);
		    fail("Calling paymentAmount with a invalid interest rate value should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
		rate = -1;
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, 
		    		paymentSchedule, amortizationPeriod, rate);
		    fail("Calling paymentAmount with a invalid interest rate value should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
	}
	
	@Test 
	public void testMortgageAmountInterestRateValidation() {
		double payment = 2500, downPayment = 49000, rate = 101;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		try {
		    Map<?,?> result = MortgageCalculator.mortgageAmount(payment, downPayment, 
		    		paymentSchedule, amortizationPeriod, rate);
		    fail("Calling paymentAmount with a down payment less than the minimum should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
		rate = -1;
		try {
		    Map<?,?> result = MortgageCalculator.paymentAmount(payment, downPayment, 
		    		paymentSchedule, amortizationPeriod, rate);
		    fail("Calling paymentAmount with a down payment less than the minimum should raise an exception.");
		}
		catch (IllegalArgumentException e) {
			
		}
	}
	
	@Test 
	public void testInsuranceCalculation() {
		// Testing insurance is calculated based on down payment to asking price ratio of 0.145, the
		// percentage for this value is 0.024.(Note hard coded dependency on those values in the 
		// class, this test will need to be modified if those change).
		double askingPrice = 750000, dp2apRatio = 0.145, downPayment = dp2apRatio * askingPrice;
		String paymentSchedule = "monthly";  
		int amortizationPeriod = 25;
		
		System.out.printf("downPayment: %f\n", downPayment);
		
		Map<?,?> result = MortgageCalculator.paymentAmount(askingPrice, downPayment, paymentSchedule, 
				amortizationPeriod);
		
        double expectedInsurance = 0.024*askingPrice;
        
		Object value = result.get("insurance");
		double insurance = Double.parseDouble(value.toString());
		System.out.printf("expectedInsurance: %f insurance: %f\n", expectedInsurance, insurance);
		
		assertEquals(insurance, expectedInsurance, 1);
	}
}

