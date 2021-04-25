/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.print.attribute.HashPrintServiceAttributeSet;
import java.lang.reflect.Type;
import java.time.*;
import java.util.*;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {
  
  final List<String> DIETARY_PREFERENCES = Arrays.asList("all","none","pollotarian","vegan");
  
  /**
   * The string representation of the base server endpoint (a HTTP address)
   */
  private String endpoint;
  private ShieldingIndividual shieldingIndividual;
  private CateringCompany cateringCompany;
  //private Collection<Order> boxOrders; //list of all history order TODO: turn into list
  private List<Order> boxOrders;
  //private Order latest = null; //used as staging for editing box
  private Order toBeEdited = null;
  private MessagingFoodBox marked = null; //used as staging
  //private List<MessagingFoodBox> defaultBoxes;
  
  private static class ShieldingIndividual { //make all inner class static
    String CHI = null;
    String postCode = null;
    String name = null;
    String surname = null;
    String phoneNumber = null;
    boolean registered = false;
  }
  
  private static class CateringCompany {
    String id;
    String name;
    String postCode;
  }
  
  final static class Order {
    int orderId;
    MessagingFoodBox foodBox;
    LocalDateTime placeTime;
    int status;
    
    public Order(){}
    
    public Order(int id, MessagingFoodBox box, LocalDateTime time, int status){
      orderId = id;
      foodBox = box;
      placeTime = time;
      this.status = status;
    }
  }
  
  // internal field only used for transmission purposes
  final static class MessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    List<Content> contents = null;
    //transient
    String delivered_by = null;
    String diet = null;
    int id = -1;
    String name = null;
  }
 
  final static class Content {
    int id = -1;
    String name = null;
    int quantity = -1;
  }
  
  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.shieldingIndividual = new ShieldingIndividual();
    this.cateringCompany = new CateringCompany(); //add
  }

// ==================================== Server Endpoints Functions ====================================
  @Override
  public boolean registerShieldingIndividual(String CHI) {
    // check validation of inputs
    // assert(CHI != null);
    if (CHI == null) return false;
    // length
    if (CHI.length() != 10) return false;
    // format
    if (!CHI.matches("[0-9]{10}")) return false;
//    System.out.println(1);
    
    int dd = Integer.parseInt(CHI.substring(0,2));
    int mm = Integer.parseInt(CHI.substring(2,4));
    int yy = Integer.parseInt(CHI.substring(4,6));
    boolean dateIsValid = true;
    try {
      LocalDate.of(yy, mm, dd);
    } catch (DateTimeException e) {
      dateIsValid = false;
    }
    if (!dateIsValid) return false;
//    System.out.println("2");
    
    // construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI;
  
    // setup the response recepient
    List<String> responseDetail = new ArrayList<String>();
//    System.out.println(7);
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      System.out.println(response.equals("already registered"));
      if (response.equals("already registered")) return true;
//      System.out.println(4);
      // unmarshal response
      Type listType = new TypeToken<Collection<String>>() {}.getType();
      responseDetail = new Gson().fromJson(response, listType);
      
      // set individual
      String pc = responseDetail.get(0);
      shieldingIndividual.postCode = pc.replace(' ', '_');
      shieldingIndividual.name = responseDetail.get(1);
      shieldingIndividual.surname = responseDetail.get(2);
      shieldingIndividual.phoneNumber = responseDetail.get(3);
      shieldingIndividual.CHI = CHI;
      shieldingIndividual.registered = true;
//      System.out.println(5);
      //set cateringCompany
      getClosestCateringCompany();
      System.out.println("User register checked cc assign: " + cateringCompany.name);
//      System.out.println(6);
      //set order list
      boxOrders = new ArrayList<Order>();
      //set default box
      //int check = getFoodBoxNumber();
     // assert check > 0; // if fail to set, check == -1
      return true;
      
    } catch (Exception e) {
//      System.out.println(3);
      e.printStackTrace();
      return false;
    }
    
    
  }

  @Override
  /**
   * Based on dietary preference, returns a collection of food ids
   *
   * @param "all","none","pollotarian" or "vegan"
   * @return a collection of box id
   */
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    // check validation of inputs
    if (dietaryPreference == null) return null;
    if (! (DIETARY_PREFERENCES.contains(dietaryPreference.toLowerCase()))) return null;
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = getFoodBoxes(dietaryPreference);
    List<String> boxIds = new ArrayList<String>();
    
    if (responseBoxes != null){
      for (MessagingFoodBox b : responseBoxes) {
        boxIds.add(String.valueOf(b.id));
      }
      return boxIds;
    } else{
      return null;
    }
  }

  // check whether t and now are in the same week
  public boolean isInSameWeek(LocalDateTime t) {
    Calendar cal1 = Calendar.getInstance();
    int currentYear = cal1.getWeekYear();
    int currentWeek = cal1.get(Calendar.WEEK_OF_YEAR);
    Calendar cal2 = Calendar.getInstance();
    cal2.set(t.getYear(), t.getMonthValue()-1, t.getDayOfMonth(),t.getHour(),t.getMinute(),t.getSecond());
    int latestYear = cal2.getWeekYear();
    int latestWeek = cal2.get(Calendar.WEEK_OF_YEAR);
    if (currentYear == latestYear && currentWeek == latestWeek) return true;
    return false;
  }
  
  // **UPDATE2** REMOVED PARAMETER
  @Override
  public boolean placeOrder() {
    System.out.println("Enter placeOrder.");
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    //precondition: assigned closest company
    System.out.println("placeOrder cc pre-assignment check: " + cateringCompany);
    //always assign the most updated catering company
    getClosestCateringCompany();
//    System.out.println("here1");
    if (cateringCompany == null) return false;
//    System.out.println("here2");
    // precondition: haven't ordered this week / never ordered
    if (boxOrders.size() > 0) {
      Order latest = boxOrders.get(boxOrders.size() - 1);
      if (isInSameWeek(latest.placeTime) && latest.status != 4) return false;
    }
    
    System.out.println("placeOrder preconditions checked.");
//    System.out.println("here3");
    /*
    if (latest != null) {
      Duration delta = Duration.between(latest.placeTime, LocalDateTime.now());
      if (delta.toDays() < 7 && latest.status != 4) return false; // even if one seconds smaller than 7 days, it returns 6
    }
     
     */
    
    // precondition: a chosen food box has been staged to "marked"
    if (marked == null) return false;
    //System.out.println(9);
    // marshal data
    Gson gson = new Gson();
    String data = gson.toJson(marked.contents);
    data = "{\"contents\":" + data + "}";
//    System.out.println(data);
    System.out.println("placeOrder marked checked.");
    
    String x = shieldingIndividual.CHI;
    String z = cateringCompany.name;
    String w = cateringCompany.postCode;
    String request = "/placeOrder?individual_id="+ x +
                     "&catering_business_name=" + z +
                     "&catering_postcode=" + w;
    System.out.println("placeOrder request checked: " + x +" "+ z + " " +w);
    request = endpoint + request;
  
    try {
      // perform request
      String response = ClientIO.doPOSTRequest(request, data);
//      System.out.println(response);
      assert response != null;
      System.out.println("placeOrder reponse checked:" + response);
      
      // add into the order history
      int id = Integer.parseInt(response);
      //System.out.println(4);
      Order newOrder = new Order();
      newOrder.orderId = id;
      newOrder.foodBox = marked;
      newOrder.placeTime = LocalDateTime.now();
      newOrder.status = 0;
      boxOrders.add(newOrder); // new order added to the end of the list
      
      //latest = newOrder;
      // clear marked
      marked = null;
      
      return true;
    
      
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    
    
  }
  
  /**
   * Update server with the modified order previously using {@link #setItemQuantityForOrder(int,int,int)}.
   *
   * @param orderNumber the order number
   * @return true if updating successfully with the server
   */
  @Override
  public boolean editOrder(int orderNumber) {
    
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    // precondition: orderNumber exist
    Collection<Integer> orderIds = getOrderNumbers();
    if (!orderIds.contains(orderNumber)) return false;
    // precondition: toBeEdited should be staged
    if (toBeEdited == null) return false;
    assert toBeEdited != null: "Haven't select and modify the order to be edited";
    // precondition: staging toBeEdited must have same order number as parameter
    if (toBeEdited.orderId != orderNumber) return false;
    // precondition: order status is still placed
    if (toBeEdited.status != 0) return false;
    
    //String s = getStatusForOrder(orderNumber);
    //if (!s.equals("placed")) return false;
    /*
    Order ord = null;
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        ord = o;
        break;
      }
    }
    assert ord != null;
    */
    // marshal data
    Gson gson = new Gson();
    String data = gson.toJson(toBeEdited.foodBox.contents);
    data = "{\"contents\":" + data + "}";
    //System.out.println(data);
    
    String request = endpoint + "/editOrder?order_id="+orderNumber;
  
    try {
      // perform request
      String response = ClientIO.doPOSTRequest(request, data);
      assert response != null;
      
      /*
      if (response.equals("True")) {
        // check if latest order is this one, if it is, then update
        if (latest.orderId == orderNumber) latest = ord;
        return true;
      }
      */
      if (response.equals("True")) {
        int idx = -1;
        for (int i = 0; i < boxOrders.size(); i++) {
          if (boxOrders.get(i).orderId == orderNumber) {
            idx = i;
            break;
          }
        }
        assert idx != -1;
        boxOrders.set(idx, toBeEdited);
        toBeEdited = null;
        return true;
      }
    
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  
  /**
   * Only registered users can use ths function.
   * Preconditions are existing order number in the order list and valid order status.
   *
   * @param orderNumber the order number to be cancelled
   * @return true if cancel successfully with server
   */
  @Override
  public boolean cancelOrder(int orderNumber) {
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    // precondition: orderNumber exist
    Collection<Integer> orderIds = getOrderNumbers();
    if (!orderIds.contains(orderNumber)) return false;
    // precondition: order status is still placed, packed or dispatched
    if (!requestOrderStatus(orderNumber)) return false; // update local order status too if a valid query
    String s = getStatusForOrder(orderNumber);
    if (s.equals("delivered") || s.equals("cancelled") || s.equals("dispatched")) return false;
    String request = endpoint + "/cancelOrder?order_id="+orderNumber;
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(request);
      assert response != null;
    
//      System.out.println("response: "+ response);
      if (response.equals("True")) {
        for (Order o: boxOrders) {
          if (o.orderId == orderNumber) {
            o.status = 4;
            return true;
          }
        }
      }
    
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    // precondition: orderNumber exist
    Collection<Integer> orderIds = getOrderNumbers();
    if (!orderIds.contains(orderNumber)) return false;
    
    String request = "/requestStatus?order_id="+orderNumber;
  
    request = endpoint + request;
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(request);
      assert response != null;
      
      int s = Integer.parseInt(response);
      assert -1 <= s && s <= 4;
      
      for (Order o: boxOrders) {
        if (o.orderId == orderNumber) {
          o.status = s;
          return true;
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    String request = "/getCaterers";
  
    // setup the response recepient
    List<String> responseDetail = new ArrayList<String>();
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      //System.out.println(response);
      assert response != null;
      
      // unmarshal response
      Type listType = new TypeToken<Collection<String>>() {}.getType();
      responseDetail = new Gson().fromJson(response, listType);
      
      assert responseDetail != null: "No Registered Catering Companies";
      // The first element is always "", because first line of txt file is always blank
      //TODO: may ignore this empty string for submission (check Piazza 801 again)
//      List<String> pureDetail = responseDetail.subList(1,responseDetail.size());
//      assert pureDetail != null;
      /*
      for (String r: pureDetail) {
        System.out.println(r);
      }
      System.out.println(pureDetail.size());
      */
      return responseDetail;
    
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
    // check individual's validity to use methods
    if (!isRegistered()) return -1;
    
    // check validation of inputs
    assert postCode1 != null;
    assert postCode2 != null;
    
    assert postCode1.matches("EH([1-9]|(1[0-7]))_[1-9][A-Z][A-Z]");
    assert postCode2.matches("EH([1-9]|(1[0-7]))_[1-9][A-Z][A-Z]");
    
    String request = "/distance?postcode1="+postCode1+"&postcode2="+postCode2;
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      float f = Float.parseFloat(response);
      return f;
      
    } catch (Exception e) {
      e.printStackTrace();
      // if failure, then -1
      return -1;
    }
  }

// ==================================== Clients Interfaces Functions ====================================

// ---------------------- Query for User from System ----------------------
  @Override
  public boolean isRegistered() {
    return shieldingIndividual.registered;
  }

  @Override
  public String getCHI() {
    return shieldingIndividual.CHI;
  }

//  ---------------------- Query for Food Box from Server ----------------------
  @Override
  public int getFoodBoxNumber() { // set all default boxes for user
    // check individual's validity to use methods
    if (!isRegistered()) return -1;
    
    Collection<String> allBoxes = showFoodBoxes("all");
    
    if (allBoxes != null){
      return allBoxes.size();
    } else{
      return -1;
    }
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    //check validation of foodBoxId
    if (!isValidFoodBoxId(foodBoxId)) return null;
    
    //TODO: can be optimized later on
    Collection<String> noneBoxes = showFoodBoxes("none");
    if (noneBoxes.contains(String.valueOf(foodBoxId))) return "none";
    Collection<String> veganBoxes = showFoodBoxes("vegan");
    if (veganBoxes.contains(String.valueOf(foodBoxId))) return "vegan";
    Collection<String> pollotarianBoxes = showFoodBoxes("pollotarian");
    if (pollotarianBoxes.contains(String.valueOf(foodBoxId))) return "pollotarian";
    
    // if none of the type contains the input food box id
    return null;
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return -1;
    
    //check validation of foodBoxId
    if (!isValidFoodBoxId(foodBoxId)) return -1;
  
    List<MessagingFoodBox> responseBoxes = getFoodBoxes("all");
    for(MessagingFoodBox box : responseBoxes){
      if(box.id == foodBoxId){
        return box.contents.size();
      }
    }
    
    // if no corresponding food box
    return -1;
  }

  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    //check validation of foodBoxId
    if (!isValidFoodBoxId(foodboxId)) return null;
  
    List<MessagingFoodBox> responseBoxes = getFoodBoxes("all");
    Collection<Integer> items = new ArrayList<Integer>();
    for(MessagingFoodBox box : responseBoxes){
      if(box.id == foodboxId){
        if (box == null) return null;
        for (Content c: box.contents) {
          items.add(c.id);
        }
        return items;
      }
    }
    
    // if no corresponding food box
    return null;
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    //check validation of foodBoxId
    if (!isValidFoodBoxId(foodBoxId)) return null;
  
    List<MessagingFoodBox> responseBoxes = getFoodBoxes("all");
    //find the box first
    for(MessagingFoodBox box : responseBoxes){
      if(box.id == foodBoxId){
        //then find the item
        for (Content c: box.contents) {
          if (c.id == itemId) return c.name;
        }
      }
    }
    
    // if no corresponding item in the identified food box
    return null;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return -1;
    //check validation of foodBoxId
    if (!isValidFoodBoxId(foodBoxId)) return -1;
  
    List<MessagingFoodBox> responseBoxes = getFoodBoxes("all");
    //find the box first
    for(MessagingFoodBox box : responseBoxes){
      if(box.id == foodBoxId){
        //then find the item
        for (Content c: box.contents) {
          if (c.id == itemId) return c.quantity;
        }
      }
    }
    
    // if no corresponding item in the identified food box
    return -1;
  }
  
  //  ---------------------- Pick Box Related ----------------------
  /**
   * Marks internal client food box in the marked food box field.
   *
   * @param  foodBoxId the food box id wanted by the user client
   * @return true if valid food box id
   */
  @Override
  public boolean pickFoodBox(int foodBoxId) {
//    System.out.println("enter pickFoodBox.");
    // check individual's validity to use methods
    if (!isRegistered()) return false;
//    System.out.println("pickFoodBox pre-condition1 checked.");
    //check validation of foodBoxId
    if (!isValidFoodBoxId(foodBoxId)){
      marked = null; // check Piazza 693
      return false;
    }
//    System.out.println("pickFoodBox pre-condition2 checked.");
  
    List<MessagingFoodBox> responseBoxes = getFoodBoxes("all");
    MessagingFoodBox fb = null;
  
    for(MessagingFoodBox box : responseBoxes){
      if(box.id == foodBoxId){
        fb = box;
        break;
      }
    }
  
    // always update marked box with this update
    marked = fb;
    if (marked != null){
      return true;
    }else{
      return false;
    }
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    //check individual's validity to use methods
    if (!isRegistered()) return false;
    //check validity of picked food box
    if (marked == null) return false;
    
    int boxId = marked.id;
    Collection<Integer> itemIds = getItemIdsForFoodBox(boxId);
    //check if given item is within the picked box
    if (!itemIds.contains(itemId)) return false;
    //check validity of the specified quantity
    int q = getItemQuantityForFoodBox(itemId,boxId);
    if (q < quantity || quantity < 0) return false;
    
    for (Content c: marked.contents) {
      if (c.id == itemId) {
        c.quantity = quantity;
        return true;
      }
    }
    return false;
  }
  
  //  ---------------------- Query for Order From System ----------------------

  @Override
  public Collection<Integer> getOrderNumbers() {
    // check validity
    if (!isRegistered()) return null; // check individual's validity to use methods
    if (boxOrders == null) return null;
    
    Collection<Integer> ids = new ArrayList<Integer>();
    for (Order o: boxOrders) {
      ids.add(o.orderId);
    }
    return ids;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    //check validity of inputs
    if (boxOrders == null) return null;
    
    //TODO: refactor
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
//        System.out.println(o.status);
        if (o.status == 0) {
//          System.out.println("se");
          return "placed";
        }
        else if (o.status == 1) return "packed";
        else if (o.status == 2) return "dispatched";
        else if (o.status == 3) return "delivered";
        else if (o.status == 4) return "cancelled";
        else return null;

      }
    }
    return null;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    //check validation of inputs
    if (boxOrders == null) return null;
    
    Order ord = null;
    List<Integer> itemIds = new ArrayList<Integer>();
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        ord = o;
        break;
      }
    }
    if (ord == null) return null;
    for (Content c: ord.foodBox.contents) {
      itemIds.add(c.id);
    }
    
    return itemIds;
  }

  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    //check validation of inputs
    if (boxOrders == null) return null;
  
    Order ord = null;
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        ord = o;
        break;
      }
    }
    if (ord == null) return null;
    for (Content c: ord.foodBox.contents) {
      if (c.id == itemId) return c.name;
    }
    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    // check individual's validity to use methods
    if (!isRegistered()) return -1;
    
    //check validation of inputs
    if (boxOrders == null) return -1;
  
    Order ord = null;
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        ord = o;
        break;
      }
    }
    if (ord == null) return -1;
    for (Content c: ord.foodBox.contents) {
      if (c.id == itemId) return c.quantity;
    }
    return -1;
  }
  /*
  public boolean pickOrderToEdit(int orderNumber) {
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    // precondition: orderNumber exist
    Collection<Integer> orderIds = getOrderNumbers();
    if (!orderIds.contains(orderNumber)) return false;
    // precondition: order status is still placed
    boolean success = requestOrderStatus(orderNumber); // update local order status
    if (!success) return false;
    String s = getStatusForOrder(orderNumber);
    if (!s.equals("placed")) return false;
    
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        Gson gson = new Gson();
        String ct = gson.toJson(o);
        
        Type listType = new TypeToken<Order>() {} .getType();
        toBeEdited = new Gson().fromJson(ct, listType);
        
        //toBeEdited = o; //TODO: need deep copy
        break;
      }
    }
    return toBeEdited != null;
  }
  */
  
  //  ---------------------- Order Modification ----------------------
  
  public boolean pickOrderToEdit(int orderNumber) {
    // precondition: isRegistered()
    if (!isRegistered()) return false;
//    System.out.println(1);
    // precondition: orderNumber exist
    Collection<Integer> orderIds = getOrderNumbers();
//    System.out.println(2);
    if (!orderIds.contains(orderNumber)) return false;
    // precondition: order status is still placed
    boolean success = requestOrderStatus(orderNumber); // update local order status
//    System.out.println(3);
    if (!success) return false;
//    System.out.println(4);
    String s = getStatusForOrder(orderNumber);
//    System.out.println(5);
    //System.out.println(s);
    if (!s.equals("placed")) return false;
  
//    System.out.println(6);
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        Gson gson = new Gson();
        String ct = gson.toJson(o);
        
        Type listType = new TypeToken<Order>() {} .getType();
        toBeEdited = new Gson().fromJson(ct, listType);
        
        //toBeEdited = o; //TODO: need deep copy
        break;
      }
    }
    return toBeEdited != null;
  }
  
  @Override
  //Todo: so have no previous knowledge about server at all
  
  //eliminate the pick step. if orderNum equal to orderNum of toBeEdit; then modify it
  //if not, then add some thing to toBeEdit
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) { // currently work for all order
    // check individual's validity to use methods
    if (!isRegistered()) return false;
//    System.out.println("s1");
    //check validation of inputs
    if (boxOrders == null) return false; //what if used in place order
                                         // and the order is the first order ever that this individual have placed.
                                         // that is, boxOrders is indeed null;
//    System.out.println("s2");
    if (toBeEdited == null || toBeEdited.orderId != orderNumber) {
//      System.out.println("s4");
      boolean success = pickOrderToEdit(orderNumber);
//      System.out.println(success);
      if (!success) return false;
    }
    assert (toBeEdited.orderId == orderNumber);
    
    /*
    // only orders valid for editing could be set successfully to keep sync to server
    Order ord = null;
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber && o.status == 0) {
        ord = o;
        break;
      }
    }
    if (ord == null) return false;  // return false if un-found order or could not be edited
     */
    for (Content c: toBeEdited.foodBox.contents) {
      if (c.id == itemId) {
        if (quantity < c.quantity) {
          c.quantity = quantity;
//          System.out.println("q:"+c.quantity);
          return true;
        }
      }
    }
    return false;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

//  ---------------------- Find Catering Company for User ----------------------
  // **UPDATE**
  @Override
  //assign the most closest cc when call this function each time
  public String getClosestCateringCompany() {
    System.out.println("!!Enter getClosestCC");
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    Collection<String> companies = getCateringCompanies();
    assert companies != null:"Fail to Get Catering Companies";
    List<String> caters = new ArrayList<String>(companies);
  
    System.out.println("getClosestCC caters size: " + caters.size());
    
    if (caters.size() < 1) return null;
  
    System.out.println("!getClosestCC post-size check: ");
  
    //get the distance of the first cater as baseline
    List<String> info = Arrays.asList(caters.get(0).split(","));
    String pc = info.get(2);
    float d = getDistance(shieldingIndividual.postCode,pc);
    //System.out.println("d");
    
    for (String c: caters.subList(1,caters.size())) { //search from the second cater in the list
      List<String> moreInfo = Arrays.asList(c.split(","));
      String morePC = moreInfo.get(2);
      float dis = getDistance(shieldingIndividual.postCode, morePC);
      //System.out.println(dis);
      if (0 <= dis && dis <= d) {
        d = dis;
        info = moreInfo;
      }
    }
    
    cateringCompany = new CateringCompany();
    cateringCompany.id = info.get(0);
    cateringCompany.name = info.get(1);
    cateringCompany.postCode = info.get(2);
    System.out.println("!!getClosestCC check: "+ info.get(0)+ " " + info.get(1) + " " + info.get(2));
  
  
    return cateringCompany.name;
   
  }
  
  //==================================== Public Functions for Testing ====================================
//  ToDo: how about using protected?
//  //set the baseline test order num and then gradually increment to avoid repeated order number
//  private int nextTestOrderNum = 100000000;
  
  public MessagingFoodBox getMarked() {return this.marked;}
  public void setMarked(MessagingFoodBox b) {this.marked = b;}
  public List<Order> getBoxOrders() {return this.boxOrders;}
  public ShieldingIndividual getShieldingIndividual() {return this.shieldingIndividual;}
  public String getShieldingIndividualPostcode() {return this.shieldingIndividual.postCode;}
  
  public void setBoxOrders(List<Order> boxOrders) {
    this.boxOrders = boxOrders;
  }
  
  public Order getToBeEdited() { return this.toBeEdited; }
  public void setToBeEdited(Order o) { this.toBeEdited = o; }
  
  public void setShieldingIndividual(String CHI, String postCode){
    shieldingIndividual.CHI = CHI;
    shieldingIndividual.postCode = postCode;
    shieldingIndividual.registered = true;
    marked = new MessagingFoodBox();
    boxOrders = new ArrayList<Order>();
    // add marked content id .....
  }
  
  public void setCateringCompany(String name, String postCode){
    cateringCompany.name = name;
    cateringCompany.postCode = postCode;

  }
  
  protected void setOrderListStatus(int element,int status){
    boxOrders.get(element).status = status;
  }
  
  /**
   * Create a dummy list of order number only for test use.
   * There are 3 orders, each with status packed, dispatched and delivered.
   * Used to test cancel order.
   *
   * @return list of order
   */
  protected List<Order> createDummyTestOrderList(){
    List<Order> list = new ArrayList<>();
//    list.add(createDummyTestOrder(4));
    list.add(createDummyTestOrder());
    list.add(createDummyTestOrder());
    list.add(createDummyTestOrder());
//    list.add(createDummyTestOrder(0));
    return list;
  }
  
  /**
   * Create a dummy test order variable based on status only for test use.
   * @return a valid dummy Order object
   */
  protected Order createDummyTestOrder(){
    Order order = new Order(0, createDummyTestFoodBox(), LocalDateTime.now(),0);
//    nextTestOrderNum += 1; //increment for next use
    return order;
  }
  
  /**
   * Create a dummy test box variable only for test use.
   * @return a valid dummy MessagingFoodBox object
   */
  protected MessagingFoodBox createDummyTestFoodBox(){
    String box = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
            "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}],\"delivered_by\":\"catering\"," +
            "\"diet\":\"none\",\"id\":1,\"name\":\"box a\"}";
    Type listType = new TypeToken<MessagingFoodBox>() {} .getType();
    MessagingFoodBox b1 = new Gson().fromJson(box, listType);
    return b1;
  }
  
  public void setStagedFoodBox(){
    String request = "/showFoodBox?";
    
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
    
      //always return the first box for simplicity for tesing purpose
      MessagingFoodBox fb = responseBoxes.get(0);
      marked = fb;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Convert order status from integer to string
   *
   * @param orderStatus integer order status from 0 to 4
   * @return String representation of order status
   */
  protected String convertIntStatusToString(int orderStatus){
    switch (orderStatus){
      case(0):
        return "placed";
      case(1):
        return "packed";
      case(2):
        return "dispatched";
      case(3):
        return "delivered";
      case(4):
        return "cancelled";
      default:
        return null;
    }
  }
  
  //==================================== Private Helper Functions ====================================
  
  /**
   * Inner helper method to get boxes by dietary preference from the server
   * Since it is private so no need to check validity of input
   *
   * @param dietaryPreference one of "all","none","vegan" or "pollotarian"
   * @return a list of messaging food box by querying the server
   */
  private List<MessagingFoodBox> getFoodBoxes(String dietaryPreference){
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
    List<String> boxIds = new ArrayList<String>();
  
    // construct the endpoint request
    //if dietary preference is "all", append nothing to request
    if (dietaryPreference.equals("all")) dietaryPreference = "";
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
      
      return responseBoxes;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Check if the given food box id is valid.
   * Assume that the food box in the server is given continuous id,
   * So there's a valid range for ids and we check if this id is within this range.
   *
   * @param foodBoxId id of a food box
   * @return true if within the range
   */
  private boolean isValidFoodBoxId(int foodBoxId){
    int range = getFoodBoxNumber();
    if (foodBoxId <= 0 || foodBoxId > range){
      return false;
    } else{
      return true;
    }
  }
}
