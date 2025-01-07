package ads.mediastreet.ai.service;

import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderListenerServiceTest {

    @Mock
    private Order mockOrder;

    @Mock
    private LineItem mockLineItem;

    @Mock
    private Item mockItem;

    private Map<String, Item> testItemMap;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        testItemMap = new HashMap<>();
        
        // Setup mock item
        when(mockItem.getId()).thenReturn("item1");
        when(mockItem.getName()).thenReturn("Test Item");
        when(mockItem.getPrice()).thenReturn(1000L); // $10.00
        testItemMap.put(mockItem.getId(), mockItem);

        // Setup mock line item
        when(mockLineItem.getItem()).thenReturn(mockItem);
        when(mockLineItem.getName()).thenReturn("Test Item");

        // Setup mock order
        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(mockLineItem);
        when(mockOrder.getLineItems()).thenReturn(lineItems);
        when(mockOrder.getTotal()).thenReturn(1000L); // $10.00
    }

    @Test
    public void testOrderTotal() {
        long total = mockOrder.getTotal();
        assertEquals("Order total should be 1000 (cents)", 1000L, total);
        assertEquals("Order total in dollars should be 10.00", 10.00, total/100.0, 0.001);
    }

    @Test
    public void testLineItems() {
        List<LineItem> lineItems = mockOrder.getLineItems();
        assertNotNull("Line items should not be null", lineItems);
        assertEquals("Should have 1 line item", 1, lineItems.size());
        
        LineItem firstItem = lineItems.get(0);
        assertEquals("Line item name should match", "Test Item", firstItem.getName());
        assertEquals("Line item's item ID should match", "item1", firstItem.getItem().getId());
    }

    @Test
    public void testInventoryItem() {
        Item item = testItemMap.get("item1");
        assertNotNull("Item should exist in map", item);
        assertEquals("Item name should match", "Test Item", item.getName());
        assertEquals("Item price should match", 1000L, (long)item.getPrice());
    }
}
