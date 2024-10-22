package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * This test plugs together all of the infrastructure, including the order book (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data coming back from the order book.
 *
 * If you cross the srpead (i.e. you BUY an order with a price which is == or > askPrice()) you will match, and receive
 * a fill back into your order from the order book (visible from the algo in the childOrders of the state object.
 *
 * If you cancel the order your child order will show the order status as cancelled in the childOrders of the state object.
 *
 */
public class MyAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    double delta = 0.0001;

    //UNIT TESTING 
    @Test
    public void unitTesting() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();
        // Create a sample market data tick
        send(Tick1());
        // Manually set the state as the algo logic evaluates the tick
        SimpleAlgoState state = container.getState();
        // Invoke the evaluate method, which will internally update the data
        myAlgoLogic.evaluate(state);

        assertEquals("testing the method: getBestBidOrderInCurrentTick()", "BID[160@98]", myAlgoLogic.getBestBidOrderInCurrentTick().toString()); // quantity = 100 from orderbook + 60 placed by MyAlgoLogic
        assertEquals("testing the method: getBestBidPriceInCurrentTick()", 98, myAlgoLogic.getBestBidPriceInCurrentTick());
        assertEquals("testing the method: getBestBidQuantityInCurrentTick()", 160, myAlgoLogic.getBestBidQuantityInCurrentTick()); // 100 from orderbook + 60 placed by MyAlgoLogic
        assertEquals("testing the method: getBestAskOrderInCurrentTick()", "ASK[101@100]", myAlgoLogic.getBestAskOrderInCurrentTick().toString()); 
        assertEquals("testing the method: getBestAskPriceInCurrentTick()", 100, myAlgoLogic.getBestAskPriceInCurrentTick());
        assertEquals("testing the method: getBestAskQuantityInCurrentTick()", 101, myAlgoLogic.getBestAskQuantityInCurrentTick());
        assertEquals("testing the method: getTheSpreadInCurrentTick()", 2, myAlgoLogic.getTheSpreadInCurrentTick());
        assertEquals("testing the method: getMidPriceInCurrentTick()", 99, myAlgoLogic.getMidPriceInCurrentTick(), delta);
        assertEquals("testing the method: getRelativeSpreadInCurrentTick()", 2, myAlgoLogic.getRelativeSpreadInCurrentTick(), delta);
        assertEquals("testing the method: getTopBidOrdersInCurrentTick()", "[BID[160@98], BID[200@95], BID[300@91]]", myAlgoLogic.getTopBidOrdersInCurrentTick().toString());
        assertEquals("testing the method: getPricesOfTopBidOrdersInCurrentTick()", "[98, 95, 91]", myAlgoLogic.getPricesOfTopBidOrdersInCurrentTick().toString());
        assertEquals("testing the method: getQuantitiesOfTopBidOrdersInCurrentTick()", "[160, 200, 300]", myAlgoLogic.getQuantitiesOfTopBidOrdersInCurrentTick().toString());
        assertEquals("testing the method: getTopAskOrdersInCurrentTick()", "[ASK[101@100], ASK[200@110], ASK[5000@115], ASK[5600@119]]", myAlgoLogic.getTopAskOrdersInCurrentTick().toString());
        assertEquals("testing the method: getPricesOfTopAskOrdersInCurrentTick()", "[100, 110, 115, 119]", myAlgoLogic.getPricesOfTopAskOrdersInCurrentTick().toString());
        assertEquals("testing the method: getQuantitiesOfTopAskOrdersInCurrentTick()", "[101, 200, 5000, 5600]", myAlgoLogic.getQuantitiesOfTopAskOrdersInCurrentTick().toString());

    
    
    }

    @Test
    public void testBullishMarketConditions() throws Exception {
        //create a sample market data tick....
        send(Tick1());

        //ADD asserts when you have implemented your algo logic
        //assertEquals(container.getState().getChildOrders().size(), 3);

        //when: market data moves towards us
        send(Tick2());

        //then: get the state
        var state = container.getState();

        //Check things like filled quantity, cancelled order count etc....
        //long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        //and: check that our algo state was updated to reflect our fills when the market data
        //assertEquals(225, filledQuantity);

        send(Tick3());
        send(Tick4());
        send(Tick5());
        send(Tick6());
        send(Tick7());
    }

}
