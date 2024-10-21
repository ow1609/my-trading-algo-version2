package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AbstractLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;





public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    
    private int marketDataTickCount = 0;

    // DATA ABOUT THE CURRENT MARKET TICK

     // variables to store data from the current tick
    private AbstractLevel bestBidOrderInCurrentTick;
    private long bestBidPriceInCurrentTick;
    private long bestBidQuantityInCurrentTick;


    private AbstractLevel bestAskOrderInCurrentTick;
    private long bestAskPriceInCurrentTick;
    private long bestAskQuantityInCurrentTick;

    // getters to obtain data from current tick
    public long getBestBidPriceInCurrentTick() {
        return bestBidPriceInCurrentTick;
    }

    public double getBestBidQuantityInCurrentTick() {
        return bestBidQuantityInCurrentTick;
    }

    // DATA ABOUT MY CHILD ORDERS

    // my bid orders

    private boolean haveActiveBidOrders = false;
    private ChildOrder activeChildBidOrderWithLowestPrice = null;

    private List<ChildOrder> filledAndPartFilledChildBidOrdersList = new ArrayList<>();
    
    public List<ChildOrder> getFilledAndPartFilledChildBidOrdersList() { // TODO - unit test
        return filledAndPartFilledChildBidOrdersList;
    }

    private long totalFilledBidQuantity;

    private void setTotalFilledBidQuantity() {
        totalFilledBidQuantity = getFilledAndPartFilledChildBidOrdersList().stream()
        .mapToLong(ChildOrder::getFilledQuantity)
        .sum();
    }

    public long getTotalFilledBidQuantity() {
        return totalFilledBidQuantity;
    }


    private long averageEntryPrice; // to be updated every

    private void setAverageEntryPrice() {  
        averageEntryPrice = getFilledAndPartFilledChildBidOrdersList().stream()
        .mapToLong(order -> order.getFilledQuantity() * order.getPrice())
        .sum() / getTotalFilledBidQuantity();
    }

    public double getAverageEntryPrice() { // top 10 // TODO - TEST THIS METHOD
        return averageEntryPrice;
    }

    // list of all my child orders including active, inactive, filled and cancelled
    private List<ChildOrder> allChildOrdersList = new ArrayList<>();
    public List<ChildOrder> getAllChildOrdersList() {
        return allChildOrdersList;
    }


    // list of my active bid orders
    private List<ChildOrder> activeChildBidOrdersList = new ArrayList<>();
    public List<ChildOrder> getActiveChildBidOrdersList() {
        return activeChildBidOrdersList;
    }

    private List<String> activeChildBidOrdersToStringList= new ArrayList<>();
    public List<String> getActiveChildBidOrdersToStringList() {
        return activeChildBidOrdersToStringList;
    }



    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("\n\n[MYALGO] THIS IS MARKET DATA TICK NUMBER: " + marketDataTickCount + "\n\n");
        marketDataTickCount += 1;

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);
        
        // UPDATE DATA ABOUT CURRENT MARKET DATA TICK

        bestBidOrderInCurrentTick = state.getBidAt(0);
        bestBidQuantityInCurrentTick = bestBidOrderInCurrentTick.quantity;
        bestBidPriceInCurrentTick = bestBidOrderInCurrentTick.price;



        bestAskOrderInCurrentTick = state.getAskAt(0);
        bestAskQuantityInCurrentTick = bestAskOrderInCurrentTick.quantity;
        bestAskPriceInCurrentTick = bestAskOrderInCurrentTick.price;


        // UPDATE DATA ABOUT MY CHILD ORDERS

        // update list of all child orders
        allChildOrdersList = state.getChildOrders();


        // Update list of my active bid orders
        activeChildBidOrdersToStringList.clear();
        activeChildBidOrdersList = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .peek(order -> activeChildBidOrdersToStringList
            .add("ACTIVE CHILD BID Id:" + order.getOrderId() + "[" + order.getQuantity() + "@" + order.getPrice() + "]"))
            .collect(Collectors.toList());

        if (!activeChildBidOrdersList.isEmpty()) {
            haveActiveBidOrders = true;
            activeChildBidOrderWithLowestPrice = activeChildBidOrdersList.stream()
                .min((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when min() returns an empty Optional
        } 

        // CREATE OR CANCEL BID LOGIC

        // Exit condition
        if (allChildOrdersList.size() > 5) { // this number multiplied by the number of ticks is how many times evaluate will be called
            logger.info("\n\n[MYALGO] currently have "
                + allChildOrdersList.size() + " child orders and "
                + activeChildBidOrdersList.size() + " active child bid orders");
            logger.info("[MYALGO] Condition 'allChildOrdersList.size() > 5' met : returning No Action\n");
            return NoAction.NoAction;
        }
        
        if (activeChildBidOrdersList.size() >= 3) {
            logger.info("\n\n[MYALGO] currently have "
                + allChildOrdersList.size() + " child orders and "
                + activeChildBidOrdersList.size() + " active child bid orders");
            logger.info("[MYALGO] Condition 'activeChildBidOrdersList.size() >= 3' met : cancelling first child bid order\n");
            ChildOrder childOrderToCancel = activeChildBidOrdersList.get(0);
            return new CancelChildOrder(childOrderToCancel);

        } else {
                logger.info("\n\n[MYALGO] currently have "
                + allChildOrdersList.size() + " child orders and "
                + activeChildBidOrdersList.size() + " active child bid orders");
                logger.info("[MYALGO] fewer than 5 child orders and have fewer than 3 active bid orders : creating new bid order\n");
                return new CreateChildOrder(Side.BUY, bestBidQuantityInCurrentTick, bestBidPriceInCurrentTick - 2);
            }   
        }
    
}
