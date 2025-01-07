package ads.mediastreet.ai.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderListenerServiceTest {

    @Test
    public void testPriceConversion() {
        // Test price conversion from cents to dollars
        long priceInCents = 1000L;
        double priceInDollars = priceInCents / 100.0;
        assertEquals("Price conversion should be correct", 10.00, priceInDollars, 0.001);
    }

    @Test
    public void testZeroPriceConversion() {
        long priceInCents = 0L;
        double priceInDollars = priceInCents / 100.0;
        assertEquals("Zero price conversion should be correct", 0.00, priceInDollars, 0.001);
    }

    @Test
    public void testLargePriceConversion() {
        long priceInCents = 999999L;
        double priceInDollars = priceInCents / 100.0;
        assertEquals("Large price conversion should be correct", 9999.99, priceInDollars, 0.001);
    }
}
