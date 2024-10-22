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
import java.util.Set;
import java.util.HashSet;





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

    private long theSpreadInCurrentTick;
    private double midPriceInCurrentTick;
    private double relativeSpreadInCurrentTick;

    private long totalQuantityOfAskOrdersInCurrentTick; // from top 10 orders
    private long totalQuantityOfBidOrdersInCurrentTick; // from top 10 orders

     // lists to store data from multiple orders in the current tick
     private List<AbstractLevel> topBidOrdersInCurrentTick = new ArrayList<>(); // top 10 ask orders
     private List<Long> pricesOfTopBidOrdersInCurrentTick = new ArrayList<>(); // from top 10 ask orders
     private List<Long> quantitiesOfTopBidOrdersInCurrentTick = new ArrayList<>(); // from top 10 ask orders
 
     private List<AbstractLevel> topAskOrdersInCurrentTick = new ArrayList<>(); // top 10 ask orders
     private List<Long> pricesOfTopAskOrdersInCurrentTick = new ArrayList<>(); // from top 10 ask orders
     private List<Long> quantitiesOfTopAskOrdersInCurrentTick = new ArrayList<>(); // from top 10 ask orders


    // getters to obtain data from current tick
    public AbstractLevel getBestBidOrderInCurrentTick() {
        return bestBidOrderInCurrentTick;
    }

    public long getBestBidPriceInCurrentTick() {
        return bestBidPriceInCurrentTick;
    }

    public long getBestBidQuantityInCurrentTick() {
        return bestBidQuantityInCurrentTick;
    }

    public AbstractLevel getBestAskOrderInCurrentTick() { 
        return bestAskOrderInCurrentTick;
    }
    
    public long getBestAskPriceInCurrentTick() {
        return bestAskPriceInCurrentTick;
    }

    public long getBestAskQuantityInCurrentTick() {
        return bestAskQuantityInCurrentTick;
    }


    public long getTheSpreadInCurrentTick() {
        return theSpreadInCurrentTick;
    }

    public double getMidPriceInCurrentTick() {
        return midPriceInCurrentTick;
    }

    public double getRelativeSpreadInCurrentTick() {
        return relativeSpreadInCurrentTick;
    }

    public List<AbstractLevel> getTopBidOrdersInCurrentTick() {
        return topBidOrdersInCurrentTick;
    }

    public List<Long> getPricesOfTopBidOrdersInCurrentTick() { // top 10
        return pricesOfTopBidOrdersInCurrentTick;
    }

    public List<Long> getQuantitiesOfTopBidOrdersInCurrentTick() { // top 10
        return quantitiesOfTopBidOrdersInCurrentTick;
    }

    public List<AbstractLevel> getTopAskOrdersInCurrentTick() {
        return topAskOrdersInCurrentTick;
    }

    public List<Long> getPricesOfTopAskOrdersInCurrentTick() { // top 10
        return pricesOfTopAskOrdersInCurrentTick;
    }

    public List<Long> getQuantitiesOfTopAskOrdersInCurrentTick() { // top 10
        return quantitiesOfTopAskOrdersInCurrentTick;
    }

     // Total quantites of bid and ask orders in top 10 price levels to gauge supply and demand for the instrument

     private long setTotalQuantityOfBidOrdersInCurrentTick() { // top 10
        return totalQuantityOfBidOrdersInCurrentTick = sumOfAllInAListOfLongs(getQuantitiesOfTopBidOrdersInCurrentTick());
    }

    public long getTotalQuantityOfBidOrdersInCurrentTick() { // top 10
        return totalQuantityOfBidOrdersInCurrentTick;
    }

    private long setTotalQuantityOfAskOrdersInCurrentTick() { // top 10
        return totalQuantityOfAskOrdersInCurrentTick = sumOfAllInAListOfLongs(getQuantitiesOfTopAskOrdersInCurrentTick());
    }

    public long getTotalQuantityOfAskOrdersInCurrentTick() { // top 10
        return totalQuantityOfAskOrdersInCurrentTick;
    }

    // variable to cap items of data to analyse
    int MAX_ITEMS_OF_DATA = 10;

    // method to populate lists of longs capped at 10 items
    private void addToAListOfLongs(List<Long> list, long num) {
        list.add(num);
        if (list.size() > MAX_ITEMS_OF_DATA) {
            list.remove(0); // remove oldest piece of data
        }
    }

    // methods to populate lists of orders on both sides
    private void addToAListOfOrders(List<AbstractLevel> listOfOrders, AbstractLevel order) {
        listOfOrders.add(order);
    }

    // method to calculate sum of all in a list of longs
    private long sumOfAllInAListOfLongs(List<Long> list) { 
        return list.stream().reduce(Long::sum).orElse(0L);
    }

    // method to calculate sum of all in a list of doubles
    private double sumOfAllInAListOfDoubles(List<Double> list) { // TODO - unit test
        return list.stream().reduce(Double::sum).get();
    }

    // method to calculate average of all doubles in a list
    private double averageOfDoublesInAList(List<Double> list) { // TODO - unit test
        return sumOfAllInAListOfDoubles(list) / list.size();
    }


    // DATA ABOUT CHILD ORDERS

    // BUY SIDE 
    private long totalExpenditure;
    private long averageEntryPrice; // to be updated every time evaluate method is called
    private long stopLoss; 
    private long profitTargetAskPrice; // use to calculate and update profit and loss over time
    private long totalRevenue; // use to calculate and update profit and loss over time
    private long totalProfitOrLoss;
    private long numOfSharesOwned;

    private long childBidOrderQuantity;
    private long childOrderPriceDifferentiator = 3; 


    private void setAverageEntryPrice() {  
        averageEntryPrice = getFilledAndPartFilledChildBidOrdersList().stream()
        .mapToLong(order -> order.getFilledQuantity() * order.getPrice())
        .sum() / getTotalFilledBidQuantity();
    }

    public double getAverageEntryPrice() { // top 10 // TODO - TEST THIS METHOD
        return averageEntryPrice;
    }


    private void setStopLoss() {
        stopLoss = (long) Math.ceil(getAverageEntryPrice() * 0.99);
    }

    public double getStopLoss() { // top 10 // TODO - TEST THIS METHOD
        return stopLoss;
    }

    private void setChildBidOrderQuantity() {
        childBidOrderQuantity = (long) Math.floor(getTotalQuantityOfBidOrdersInCurrentTick() * 0.1); // set POV to 10%
    }


    public long getChildBidOrderQuantity() { // top 10 // TODO - TEST THIS METHOD
        return childBidOrderQuantity;
    }
    

    private void setProfitTargetAskPrice() {
        profitTargetAskPrice = (long) Math.ceil(getAverageEntryPrice() * 1.03);
    }

    public long getProfitTargetAskPrice() { // top 10 // TODO - TEST THIS METHOD
        return profitTargetAskPrice;
    }

    // list of all child orders including active, inactive, filled and cancelled
    private List<ChildOrder> allChildOrdersList = new ArrayList<>();
    public List<ChildOrder> getAllChildOrdersList() {
        return allChildOrdersList;
    }


    // Filtered lists of child BID orders

    private List<ChildOrder> activeChildBidOrdersList = new ArrayList();
    private List<String> activeChildBidOrdersToStringList= new ArrayList<>(); // TODO - delete later - only for logging
    // HashSet to prevent duplication in list of filled and part filled orders list
    private Set<ChildOrder> bidOrdersMarkedAsFilledOrPartFilled = new HashSet<>();
    private List<ChildOrder> filledAndPartFilledChildBidOrdersList = new ArrayList();
    private List<String> filledAndPartFilledChildBidOrdersListToString = new ArrayList(); // TODO - delete later - only for logging



    private ChildOrder activeChildBidOrderWithLowestPrice = null;
    private ChildOrder activeChildBidOrderWithHighestPrice = null;
    
    private boolean haveActiveBidOrders = false;
    private boolean haveFilledBidOrders = false;
    private long totalFilledBidQuantity;


    public List<ChildOrder> getActiveChildBidOrdersList() {
        return activeChildBidOrdersList;
    }

    public List<String> getActiveChildBidOrdersToStringList() { // TODO - delete later - only for logging
        return activeChildBidOrdersToStringList;
    }

    public ChildOrder getActiveChildBidOrderWithLowestPrice() {
        return activeChildBidOrderWithLowestPrice;
    }


    public ChildOrder getActiveChildBidOrderWithHighestPrice() {
        return activeChildBidOrderWithHighestPrice;
    }

    public List<ChildOrder> getFilledAndPartFilledChildBidOrdersList() { // TODO - unit test
        return filledAndPartFilledChildBidOrdersList;
    }

    public List<String> getFilledAndPartFilledChildBidOrdersListToString() {
        return filledAndPartFilledChildBidOrdersListToString;
    }
    

    private void setTotalFilledBidQuantity() {
        totalFilledBidQuantity = getFilledAndPartFilledChildBidOrdersList().stream()
        .mapToLong(ChildOrder::getFilledQuantity)
        .sum();
    }

    public long getTotalFilledBidQuantity() {
        return totalFilledBidQuantity;
    }

    private void setTotalExpenditure() {
        totalExpenditure = getFilledAndPartFilledChildBidOrdersList().stream()
            .mapToLong(order -> (order.getFilledQuantity() * order.getPrice()))
            .sum();
    }
    
    
    public long getTotalExpenditure() { //TODO test this method
        return totalExpenditure;
    }
    

    // SELL SIDE

    // Filtered lists of my ASK orders

    private List<ChildOrder> allChildAskOrdersList = new ArrayList<>(); // TODO - delete??
    List<String> allChildAskOrdersListToString = new ArrayList<>();
    private List<ChildOrder> activeChildAskOrdersList = new ArrayList<>(); // TODO
    List<String> activeChildAskOrdersListToString = new ArrayList<>(); // TODO - delete when no longer needed, using for logging statements for now
    private List<ChildOrder> filledAndPartFilledChildAskOrdersList = new ArrayList<>(); // TODO
    List<String> filledAndPartFilledChildAskOrdersListToString = new ArrayList<>(); // TODO - delete when no longer needed, using for logging statements for now
    private long totalFilledAskQuantity;

    private boolean haveActiveAskOrders = false;
    private boolean haveFilledAskOrders = false;
    private ChildOrder activeChildAskOrderWithHighestPrice = null;
    private ChildOrder activeChildAskOrderWithLowestPrice = null;
    private long childAskQuantityIfStopLossHit;


    public List<ChildOrder> getActiveChildAskOrdersList() {
        return activeChildAskOrdersList;
    }

    private String activeChildAskOrderWithHighestPriceToString = ""; // TODO - delete when no longer needed, using for logging statements for now

    public ChildOrder getActiveChildAskOrderWithHighestPrice() {
        return activeChildAskOrderWithHighestPrice;
    }

    public ChildOrder getActiveChildAskOrderWithLowestPrice() {
        return activeChildAskOrderWithLowestPrice;
    }
    // HashSet to prevent duplication in list of filled and part filled orders list
    private Set<ChildOrder> askOrdersMarkedAsFilledOrPartFilled = new HashSet<>();

    // List of filled orders as an ArrayList to preserve the sequential order of filled and part filled orders
    // and maintain state across ticks to track changes over time
    public List<ChildOrder> getFilledAndPartFilledChildAskOrdersList() { // TODO - unit test
        return filledAndPartFilledChildAskOrdersList;
    }

    private void setTotalFilledAskQuantity() {
        totalFilledAskQuantity = getFilledAndPartFilledChildAskOrdersList().stream()
        .mapToLong(ChildOrder::getFilledQuantity)
        .sum();
    }

    public long getTotalFilledAskQuantity() { // TODO - TEST THIS METHOD
        return totalFilledAskQuantity;
    }

    private void setTotalRevenue() {
        totalRevenue = getFilledAndPartFilledChildAskOrdersList().stream()
            .mapToLong(order -> (order.getFilledQuantity() * order.getPrice()))
            .sum();
    }
    
    public long getTotalRevenue() { //TODO test this method
        return totalRevenue;
    }
    
    private void setTotalProfitOrLoss() {
        totalProfitOrLoss = getTotalRevenue() - getTotalExpenditure();
    }
    
    public long getTotalProfitOrLoss() { // top 10 // TODO - TEST THIS METHOD
        return totalProfitOrLoss;
    }

    private void setNumOfSharesOwned() {
        numOfSharesOwned = getTotalFilledBidQuantity() - getTotalFilledAskQuantity();
    }

    public long getNumOfSharesOwned() {  // TODO - TEST THIS METHOD
        return numOfSharesOwned;
    }


    public long getTotalFilledQuantityOfAllBidAndAskOrders() {
        return getTotalFilledBidQuantity() + getTotalFilledAskQuantity();
    }

    private long volumeWeightedAveragePrice;

    private void setVolumeWeightedAveragePrice() {
        volumeWeightedAveragePrice = getAllChildOrdersList().stream()
            .filter(order -> order.getFilledQuantity() > 0)
            .mapToLong(order -> order.getFilledQuantity() * order.getPrice())
            .sum() / getTotalFilledQuantityOfAllBidAndAskOrders();
    }
    
    public long getVolumeWeightedAveragePrice() { // top 10 // TODO - TEST THIS METHOD
        return volumeWeightedAveragePrice;
    }

    private void setChildAskQuantityIfStopLossHit() {
        childAskQuantityIfStopLossHit = (long) Math.max(getBestBidQuantityInCurrentTick(), getNumOfSharesOwned());
    }

    private long getChildAskQuantityIfStopLossHit() {
        return childAskQuantityIfStopLossHit;
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

        theSpreadInCurrentTick = bestAskPriceInCurrentTick - bestBidPriceInCurrentTick;
        midPriceInCurrentTick = (bestAskPriceInCurrentTick + bestBidPriceInCurrentTick) / 2;

        // Maths round to limit to 2dp
        relativeSpreadInCurrentTick = Math.round((theSpreadInCurrentTick / midPriceInCurrentTick * 100) * 100 / 100);


        // Loop to populate lists of data about the top bid orders in the current tick
        int maxBidOrders = Math.min(state.getBidLevels(), MAX_ITEMS_OF_DATA); // up to a max of 10 bid orders
        topBidOrdersInCurrentTick.clear();
        pricesOfTopBidOrdersInCurrentTick.clear();
        quantitiesOfTopBidOrdersInCurrentTick.clear();
        for (int i = 0; i < maxBidOrders; i++) {
            AbstractLevel bidOrder = state.getBidAt(i);
            addToAListOfOrders(topBidOrdersInCurrentTick, bidOrder);
            addToAListOfLongs(pricesOfTopBidOrdersInCurrentTick, bidOrder.price);
            addToAListOfLongs(quantitiesOfTopBidOrdersInCurrentTick, bidOrder.quantity);
        }
        setTotalQuantityOfBidOrdersInCurrentTick();
        setChildBidOrderQuantity();


        // Loop to populate lists of data about the top ask orders in the current tick
        int maxAskOrders = Math.min(state.getAskLevels(), 10); // up to a max of 10 ask orders
        getTopAskOrdersInCurrentTick().clear();
        getPricesOfTopAskOrdersInCurrentTick().clear();
        getQuantitiesOfTopAskOrdersInCurrentTick().clear();
        for (int i = 0; i < maxAskOrders; i++) {
            AbstractLevel askOrder = state.getAskAt(i);
            addToAListOfOrders(topAskOrdersInCurrentTick, askOrder);
            addToAListOfLongs(pricesOfTopAskOrdersInCurrentTick, askOrder.price);
            addToAListOfLongs(quantitiesOfTopAskOrdersInCurrentTick, askOrder.quantity);
        }
        setTotalQuantityOfAskOrdersInCurrentTick();


        // UPDATE DATA ABOUT MY CHILD ORDERS

        // BUY SIDE 

        // update list of all child orders
        allChildOrdersList = state.getChildOrders();


        // Update list of active child bid orders
        activeChildBidOrdersToStringList.clear();  // TODO delete later - only for logging now
        activeChildBidOrdersList = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .peek(order -> activeChildBidOrdersToStringList
            .add("ACTIVE CHILD BID Id:" + order.getOrderId() + "[" + order.getQuantity() + "@" + order.getPrice() + "]"))
            .collect(Collectors.toList());

        // if have active child BID orders, update the bids with the lowest and highest price
        if (!activeChildBidOrdersList.isEmpty()) {
            haveActiveBidOrders = true;
            activeChildBidOrderWithLowestPrice = activeChildBidOrdersList.stream()
                .min((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when min() returns an empty Optional
            activeChildBidOrderWithHighestPrice = activeChildBidOrdersList.stream()
                .max((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when min() returns an empty Optional
            }   

        // Update list of filled child BID orders
        filledAndPartFilledChildBidOrdersList = state.getChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY && order.getFilledQuantity() > 0)
            .filter(order -> !bidOrdersMarkedAsFilledOrPartFilled.contains(order))  // Only add if not processed
            .peek(order -> bidOrdersMarkedAsFilledOrPartFilled.add(order))  // Mark as processed
            .peek(order-> filledAndPartFilledChildBidOrdersListToString // TODO DELETE LATER ONLY FOR OUTPUT DURING DEVELOPMENT FOR BACK TESTS
            .add("FILL/PARTFILL BID Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "] filledQuantity: " + order.getFilledQuantity())) // TODO DELETE LATER URING DEVELOPMENT FOR BACK TESTS
            .collect(Collectors.toList());

        // if there are filled child BID Orders
        if (!filledAndPartFilledChildBidOrdersList.isEmpty()) { 
            haveFilledBidOrders = true;
            setTotalFilledBidQuantity(); // update total filled bid quantity 
            setAverageEntryPrice(); // update average entry price
            setStopLoss(); // update stop loss
            setProfitTargetAskPrice();
            setTotalExpenditure(); // update total expenditure
            setVolumeWeightedAveragePrice();
            setNumOfSharesOwned();
        }

        logger.info("[MYALGO] haveFilledBidOrders is: " + haveFilledBidOrders);
        logger.info("[MYALGO] getTotalFilledBidQuantity() is: " + getTotalFilledBidQuantity());
        logger.info("[MYALGO] getAverageEntryPrice() is: " + getAverageEntryPrice());
        logger.info("[MYALGO] getStopLoss() is: " + getStopLoss());        
        logger.info("[MYALGO] getTotalExpenditure() is: " + getTotalExpenditure());

        
        // SELL SIDE

        // Update list of active child ASK orders
        activeChildAskOrdersListToString.clear();  // TODO delete later - only for logging now
        activeChildAskOrdersList = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.SELL)
            .peek(order -> activeChildAskOrdersListToString
            .add("ACTIVE CHILD ASK Id:" + order.getOrderId() + "[" + order.getQuantity() + "@" + order.getPrice() + "]"))
            .collect(Collectors.toList());

        // if have active child ASK orders, update the ask with the highest and lowest price
        if (!activeChildAskOrdersList.isEmpty()) {
            haveActiveAskOrders = true;
            activeChildAskOrderWithHighestPrice = activeChildAskOrdersList.stream()
                .max((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when max() returns an empty Optional        
            activeChildAskOrderWithLowestPrice = activeChildAskOrdersList.stream()
                .min((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when max() returns an empty Optional
            }

        // Update list of filled child ASK orders
        filledAndPartFilledChildAskOrdersList = state.getChildOrders().stream()
            .filter(order -> order.getSide() == Side.SELL && order.getFilledQuantity() > 0)
            .filter(order -> !askOrdersMarkedAsFilledOrPartFilled.contains(order))  // Only add if not processed
            .peek(order -> askOrdersMarkedAsFilledOrPartFilled.add(order))  // Mark as processed
            .peek(order-> filledAndPartFilledChildAskOrdersListToString // TODO DELETE LATER ONLY FOR OUTPUT DURING DEVELOPMENT FOR BACK TESTS
            .add("FILL/PARTFILL ASK Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "] filledQuantity: " + order.getFilledQuantity())) // TODO DELETE LATER URING DEVELOPMENT FOR BACK TESTS
            .collect(Collectors.toList());

        // if there are filled ASK Orders
        if (!filledAndPartFilledChildAskOrdersList.isEmpty()) { 
            haveFilledAskOrders = true;
            setTotalFilledAskQuantity();
            setTotalRevenue(); // update the total revenue
            setVolumeWeightedAveragePrice();
            setNumOfSharesOwned();
        }
        
        setTotalProfitOrLoss();
        

        logger.info("[MYALGO] getTotalFilledAskQuantity() is: " + getTotalFilledAskQuantity());
        logger.info("[MYALGO] getTotalRevenue() is: " + getTotalRevenue());
        logger.info("[MYALGO] getTotalProfitOrLoss() is: " + getTotalProfitOrLoss());
        logger.info("[MYALGO] getNumOfSharesOwned() is: " + getNumOfSharesOwned());
        logger.info("[MYALGO] filledAndPartFilledChildAskOrdersListToString is: " + filledAndPartFilledChildAskOrdersListToString);


        logger.info("\n\n[MYALGO] getAverageEntryPrice()is : " + getAverageEntryPrice() + "\n\n");


        // DECISION LOGIC - CREATE OR CANCEL CHILD ORDERS

        // Exit condition
        if (allChildOrdersList.size() > 6) {
            logger.info("[MYALGO] Condition 'allChildOrdersList.size() > 5' met : returning No Action\n");
            return NoAction.NoAction;
        }
        
        // if I have more than 3 active BID orders, cancel the least competitve one
        if (activeChildBidOrdersList.size() > 3) {
            logger.info("[MYALGO] cancelling activeChildBidOrderWithLowestPrice because it is least competitive \n");
            return new CancelChildOrder(activeChildBidOrderWithLowestPrice);

        // if I have more than 3 active ASK orders, cancel the least competitve one
        } else if (activeChildAskOrdersList.size() > 3) {
            logger.info("[MYALGO] cancelling activeChildAskOrderWithHighestPrice because it is least competitive\n");            
            return new CancelChildOrder(activeChildAskOrderWithHighestPrice);

        // BUG HERE
        // if have shares and VWAP hits the stopLoss, sell all shares for best price possible
        // } else if ((getNumOfSharesOwned() > 0) && (getVolumeWeightedAveragePrice() <= stopLoss)) {
        //     logger.info("[MYALGO] condition '(getNumOfSharesOwned() > 0) && (getVolumeWeightedAveragePrice() <= stopLoss)' met : sniper selling everything");
        //     return new CreateChildOrder(Side.SELL, getChildAskQuantityIfStopLossHit() , getBestBidPriceInCurrentTick());
        
        // if have shares and top bid price reaches profit target, place an aggressive ask order
        } else if ((getNumOfSharesOwned() > 0) && (getBestBidPriceInCurrentTick() >= getProfitTargetAskPrice())) {
            logger.info("[MYALGO] have " + getNumOfSharesOwned() + " shares to sell "
            + " and best bid has reached profit target. Placing aggressive ASK order to sell as much as possible @ " + getBestBidPriceInCurrentTick());
            return new CreateChildOrder(Side.SELL, (long) Math.min(getBestBidQuantityInCurrentTick(), getNumOfSharesOwned()), getBestBidPriceInCurrentTick());
        
        // if have shares and spread is at most one tick size, place aggressive ASK order paying the spread
        } else if ((getNumOfSharesOwned() > 0) && (getTheSpreadInCurrentTick() < 2)) {
            logger.info("[MYALGO] have shares to sell and the spread is narrow : placing aggressive ASK order paying the spread to sell half of shares owned");
            return new CreateChildOrder(Side.SELL, (long) (getNumOfSharesOwned()/2), getBestBidPriceInCurrentTick());


        // if have shares and have no active ASK orders, place passive ASK order at target profit price
        } else if ((getNumOfSharesOwned() > 0) && (haveActiveAskOrders == false)) {
            logger.info("[MYALGO] have shares to sell : placing passive ASK order at profit target price");
            return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getProfitTargetAskPrice());
        
        
        // if have shares and have 1 active ASK order, place passive ASK order 1 tick size above current top ask price
        } else if ((getNumOfSharesOwned() > 0) && (activeChildAskOrdersList.size() == 1) && (getTheSpreadInCurrentTick() < 3)) {
            logger.info("[MYALGO] have shares to sell : placing passive ASK order 1 tick size above current top ask price");
            return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getBestAskPriceInCurrentTick() + 1);
    
        // if have shares and have 2 active ASK orders, place passive ASK order matching current top ask price
        } else if ((getNumOfSharesOwned() > 0) && (activeChildAskOrdersList.size() == 2) && (getTheSpreadInCurrentTick() < 3)) {
            logger.info("[MYALGO] have shares to sell : placing passive ASK order matching current top ask price");
            return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getBestAskPriceInCurrentTick());
    
        // if have shares and spread is wide, place passive ASK order below current best ASK, narrowing the spread
        } else if ((getNumOfSharesOwned() > 0) && (getTheSpreadInCurrentTick() >= 3)) {
            logger.info("[MYALGO] have shares to sell : placing passive ASK order 1 tick size below current top ask price, narrowing the spread");
            return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getBestAskPriceInCurrentTick() - 1);
    
        // if the spread is at most one tick size, pay the spread and place an aggressive BID order at besk ASK price
        } else if (getTheSpreadInCurrentTick() < 2) {
        logger.info("[MYALGO] have " + allChildOrdersList.size() + " child order(s) in total, of which I have "
        + activeChildBidOrdersList.size() + " active child BID order(s) "
        + "and the spread is at most 1 tick size at " + getTheSpreadInCurrentTick() + 
        " Paying the spread to place an aggressive BID order for " + getChildBidOrderQuantity() + "@" + (getBestAskPriceInCurrentTick()) +  " at best ask price\n");
        return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), getBestAskPriceInCurrentTick());
        
        // if I have no active child BID orders and the spread is narrow, place a passive order one below best bid price
        } else if (activeChildBidOrdersList.isEmpty() && (getTheSpreadInCurrentTick() < 3)) {
            logger.info("[MYALGO] have " + allChildOrdersList.size() + " child order(s) in total, of which I have "
            + activeChildBidOrdersList.size() + " active child BID order(s) "
            + "and the spread is narrow at " + getTheSpreadInCurrentTick() + 
            ". Placing a passive BID order for " + getChildBidOrderQuantity() + "@" + (getBestBidPriceInCurrentTick() - 1) +  " one below the best bid price\n");
            return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), getBestBidPriceInCurrentTick() -1);

        // if I have 1 active child BID order and the spread is narrow, place a passive order to match the best bid price
        } else if (activeChildBidOrdersList.size() == 1 && (getTheSpreadInCurrentTick() < 3)) {
            logger.info("[MYALGO] have " + allChildOrdersList.size() + " child order(s) in total, of which I have "
            + activeChildBidOrdersList.size() + " active child BID order(s) "
            + "and the spread is narrow at " + getTheSpreadInCurrentTick() + 
            ". Placing a passive BID order for " + getChildBidOrderQuantity() + "@" + getBestBidPriceInCurrentTick() +  "to match the best bid price\n");
            return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), getBestBidPriceInCurrentTick());
        
        // if I have 2 active child BID orders and the spread is wide, place a passive BID order to become the new best bid price, narrowing the spread
        } else if (activeChildBidOrdersList.size() == 2 && (getTheSpreadInCurrentTick() >= 3)) {
            logger.info("[MYALGO] have " + allChildOrdersList.size() + "child order(s) in total of which I have "
            + activeChildBidOrdersList.size() + " active child BID order(s) "
            + "and the spread is wide at " + getTheSpreadInCurrentTick() + 
            ". Placing a passive BID order for " + getChildBidOrderQuantity() + "@" + (getBestBidPriceInCurrentTick() + 1) +  "above the best bid price, narrowing the spread\n");
            return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), getBestBidPriceInCurrentTick() + 1);

        } else {
            logger.info("[MYALGO] No conditions met : no Action, wait for next Market Data tick");
            return NoAction.NoAction;
        }
    }
}

