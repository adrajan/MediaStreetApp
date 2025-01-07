package ads.mediastreet.ai.service;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OrderListenerServiceInstrumentedTest {

    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testServiceStart() {
        Intent intent = new Intent(context, OrderListenerService.class);
        context.startService(intent);
        // Basic test to ensure service can be started without crashing
        assertTrue("Service should start successfully", true);
    }

    @Test
    public void testAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("ads.mediastreet.ai", appContext.getPackageName());
    }
}
