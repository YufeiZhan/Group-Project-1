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
  
  // possible list of dietary preferences
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
  
  
  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.shieldingIndividual = new ShieldingIndividual();
    this.cateringCompany = new CateringCompany(); //add
  }
  
  // ==================================== Internal Fields ====================================
  
  /**
   * Class storing relevant information aboout a shielding individual
   */
  private static class ShieldingIndividual { //make all inner class static
    String CHI = null;
    String postCode = null;
    String name = null;
    String surname = null;
    String phoneNumber = null;
    boolean registered = false;
  }
  
  /**
   * Class storing relevant information about any catering company
   */
  private static class CateringCompany {
    String id;
    String name;
    String postCode;
  }
  
  /**
   * This class stores the box content as well as any related info of the order.
   */
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
  
  /**
   * This is an internal field only used for transmission purposes.
   */
  final static class MessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    List<Content> contents = null;
    //transient
    String delivered_by = null;
    String diet = null;
    int id = -1;
    String name = null;
  }
  
  /**
   * Store the id, name and quantity of a specific food item
   */
  final static class Content {
    int id = -1;
    String name = null;
    int quantity = -1;
  }


// ==================================== Server Endpoints Functions ====================================
  @Override
  public boolean registerShieldingIndividual(String CHI) {
    // check validation of inputs
    if (CHI == null) return false;
    // length
    if (CHI.length() != 10) return false;
    // format
    if (!CHI.matches("[0-9]{10}")) return false;
    
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
    
    // construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI;
  
    // setup the response recepient
    List<String> responseDetail = new ArrayList<String>();
    
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

      //set cateringCompany
      getClosestCateringCompany();
      System.out.println("User register checked cc assign: " + cateringCompany.name);

      //set order list
      boxOrders = new ArrayList<Order>();
      return true;
      
    } catch (Exception e) {
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
  
  // **UPDATE2** REMOVED PARAMETER
  @Override
  public boolean placeOrder() {
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    //always assign the most closest catering company
    getClosestCateringCompany();
    if (cateringCompany == null) return false;

    // precondition: haven't ordered this week / never ordered
    if (boxOrders.size() > 0) {
      Order latest = boxOrders.get(boxOrders.size() - 1);
      if (isInSameWeek(latest.placeTime) && latest.status != 4) return false;
    }
    
    // precondition: a chosen food box has been staged to "marked"
    if (marked == null) return false;

    // marshal data
    Gson gson = new Gson();
    String data = gson.toJson(marked.contents);
    data = "{\"contents\":" + data + "}";
    
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
      assert response != null;
      // add into the order history
      int id = Integer.parseInt(response);

      Order newOrder = new Order();
      newOrder.orderId = id;
      newOrder.foodBox = marked;
      newOrder.placeTime = LocalDateTime.now();
      newOrder.status = 0;
      boxOrders.add(newOrder); // new order added to the end of the list
      
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
    
    // marshal data
    Gson gson = new Gson();
    String data = gson.toJson(toBeEdited.foodBox.contents);
    data = "{\"contents\":" + data + "}";
    String request = endpoint + "/editOrder?order_id="+orderNumber;
  
    try {
      // perform request
      String response = ClientIO.doPOSTRequest(request, data);
      assert response != null;

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
      assert response != null;
      
      // unmarshal response
      Type listType = new TypeToken<Collection<String>>() {}.getType();
      responseDetail = new Gson().fromJson(response, listType);
      
      assert responseDetail != null: "No Registered Catering Companies";
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
    // check individual's validity to use methods
    if (!isRegistered()) return false;
    //check validation of foodBoxId
    if (!isValidFoodBoxId(foodBoxId)){
      marked = null; // check Piazza 693
      return false;
    }
  
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
    //check individual's validity to use methods
    if (!isRegistered()) return null;
    //check validity of inputs
    if (boxOrders == null) return null;
    
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        if (convertIntStatusToString(o.status) != null) {
          return convertIntStatusToString(o.status);
        }else {
          return null;
        }
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
  
  //  ---------------------- Order Modification ----------------------
  
  @Override
  //eliminate the pick step. if orderNum equal to orderNum of toBeEdit; then modify it
  //if not, then add some thing to toBeEdit
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) { // currently work for all order
    // check individual's validity to use methods
    if (!isRegistered()) return false;

    //check validation of inputs
    if (boxOrders == null) return false; //what if used in place order
                                         // and the order is the first order ever that this individual have placed.
                                         // that is, boxOrders is indeed null;
    if (toBeEdited == null || toBeEdited.orderId != orderNumber) {
      boolean success = pickOrderToEdit(orderNumber);
      if (!success) return false;
    }
    assert (toBeEdited.orderId == orderNumber);
    
    for (Content c: toBeEdited.foodBox.contents) {
      if (c.id == itemId) {
        if (quantity < c.quantity) {
          c.quantity = quantity;
          return true;
        }
      }
    }
    return false;
  }
  
  protected boolean pickOrderToEdit(int orderNumber) {
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
        break;
      }
    }
    return toBeEdited != null;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

//  ---------------------- Find Catering Company for User ----------------------
  // **UPDATE**
  @Override
  //assign the most closest cc when call this function each time
  public String getClosestCateringCompany() {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    Collection<String> companies = getCateringCompanies();
    assert companies != null:"Fail to Get Catering Companies";
    List<String> caters = new ArrayList<String>(companies);
    if (caters.size() < 1) return null;
    
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
    return cateringCompany.name;
   
  }
  
  //==================================== Public Functions for Testing ====================================
  protected MessagingFoodBox getMarked() {return this.marked;}
  protected void setMarked(MessagingFoodBox b) {this.marked = b;}
  protected List<Order> getBoxOrders() {return this.boxOrders;}
  protected ShieldingIndividual getShieldingIndividual() {return this.shieldingIndividual;}
  protected String getShieldingIndividualPostcode() {return this.shieldingIndividual.postCode;}
  
  protected void setBoxOrders(List<Order> boxOrders) {
    this.boxOrders = boxOrders;
  }
  
  protected Order getToBeEdited() { return this.toBeEdited; }
  protected void setToBeEdited(Order o) { this.toBeEdited = o; }
  
  protected void setShieldingIndividual(String CHI, String postCode){
    shieldingIndividual.CHI = CHI;
    shieldingIndividual.postCode = postCode;
    shieldingIndividual.registered = true;
    marked = new MessagingFoodBox();
    boxOrders = new ArrayList<Order>();
    // add marked content id .....
  }
  
  protected void setCateringCompany(String name, String postCode){
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
    list.add(createDummyTestOrder());
    list.add(createDummyTestOrder());
    list.add(createDummyTestOrder());
    return list;
  }
  
  /**
   * Create a dummy test order variable based on status only for test use.
   * @return a valid dummy Order object
   */
  protected Order createDummyTestOrder(){
    Order order = new Order(0, createDummyTestFoodBox(), LocalDateTime.now(),0);
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
  
  // check whether t and now are in the same week
  private boolean isInSameWeek(LocalDateTime t) {
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
