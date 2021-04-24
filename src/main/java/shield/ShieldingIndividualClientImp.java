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
  }

  @Override
  public boolean registerShieldingIndividual(String CHI) {
    // check validation of inputs
    // assert(CHI != null);
    if (CHI == null) return false;
    // length
    if (CHI.length() != 10) return false;
    // format
    if (!CHI.matches("[0-9]{10}")) return false;
    System.out.println(1);
    
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
    System.out.println("2");
    
    // construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI;
  
    // setup the response recepient
    List<String> responseDetail = new ArrayList<String>();
    System.out.println(7);
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      System.out.println(response.equals("already registered"));
      if (response.equals("already registered")) return true;
      System.out.println(4);
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
      System.out.println(5);
      //set cateringCompany
      getClosestCateringCompany();
      System.out.println(6);
      //set order list
      boxOrders = new ArrayList<Order>();
      //set default box
      //int check = getFoodBoxNumber();
     // assert check > 0; // if fail to set, check == -1
      return true;
      
    } catch (Exception e) {
      System.out.println(3);
      e.printStackTrace();
      return false;
    }
    
    
  }

  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    // check validation of inputs
    if (dietaryPreference == null) return null;
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    List<String> boxIds = new ArrayList<String>();
    
    // unexpected preference -> empty list
    String[] preference = {"none", "pollotarian", "vegan"};
    if (!Arrays.asList(preference).contains(dietaryPreference.toLowerCase())) return boxIds;
    
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
      
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
      
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        /*
        for (Content c: b.contents) {
          System.out.println(c.id + " " + c.name + " "+ c.quantity);
        }
        */
        boxIds.add(String.valueOf(b.id));
      }
      return boxIds;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    //System.out.println(boxOfPreference.size());
    
  }

  // check whether t and now are in the same week
  public boolean isInSameWeek(LocalDateTime t) {
    Calendar cal1 = Calendar.getInstance();
    int currentYear = cal1.getWeekYear();
    int currentWeek = cal1.WEEK_OF_YEAR;
    Calendar cal2 = Calendar.getInstance();
    cal2.set(t.getYear(), t.getMonthValue(), t.getDayOfMonth(),t.getHour(),t.getMinute(),t.getSecond());
    int latestYear = cal2.getWeekYear();
    int latestWeek = cal2.WEEK_OF_YEAR;
    if (currentYear == latestYear && currentWeek == latestWeek) return true;
    return false;
  }
  
  // **UPDATE2** REMOVED PARAMETER
  @Override
  public boolean placeOrder() {
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    //precondition: assigned closest company
    if (cateringCompany == null) {
      getClosestCateringCompany();
    }
    // precondition: haven't ordered this week
    Order latest = boxOrders.get(boxOrders.size() - 1);
    if (isInSameWeek(latest.placeTime) && latest.status != 4) return false;
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
    System.out.println(data);
    
    
//    TODO: no slash '/' here
    String x = shieldingIndividual.CHI;
    String z = cateringCompany.name;
    String w = cateringCompany.postCode;
    String request = "placeOrder?individual_id="+ x +
                     "&catering_business_name=" + z +
                     "&catering_postcode=" + w;
  
    request = endpoint + request;
  
    try {
      //System.out.println(3);
      // perform request
      String response = ClientIO.doPOSTRequest(request, data);
      assert response != null;
      
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

  @Override
  public boolean editOrder(int orderNumber) {
    
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    // successfully toBeEdited has been set up
    if (toBeEdited == null) return false;
    assert toBeEdited != null: "toBeEdited should have been set";
    // precondition: orderNumber exist
    //Collection<Integer> orderIds = getOrderNumbers();
    //if (!orderIds.contains(orderNumber)) return false;
    // precondition: order status is still placed
    //boolean success = requestOrderStatus(orderNumber); // update local order status
    //assert success;
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
    
    String request = "/editOrder?order_id="+orderNumber;
  
    request = endpoint + request;
  
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

  @Override
  public boolean cancelOrder(int orderNumber) {
    // precondition: isRegistered()
    if (!isRegistered()) return false;
    // precondition: orderNumber exist
    Collection<Integer> orderIds = getOrderNumbers();
    if (!orderIds.contains(orderNumber)) return false;
    // precondition: order status is still placed
    boolean success = requestOrderStatus(orderNumber); // update local order status
    if (!success) return false;
    String s = getStatusForOrder(orderNumber);
    if (s.equals("delivered") || s.equals("cancelled")) return false;
    
  
    String request = "/cancelOrder?order_id="+orderNumber;
  
    request = endpoint + request;
  
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
  
    //TODO: do we really need the boxOrders?
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

  @Override
  public boolean isRegistered() {
    return shieldingIndividual.registered;
  }

  @Override
  public String getCHI() {
    return shieldingIndividual.CHI;
  }

  @Override
  public int getFoodBoxNumber() { // set all default boxes for user
    // check individual's validity to use methods
    if (!isRegistered()) return -1;
    
    // construct the endpoint request
    String request = "/showFoodBox?";
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
      
      //defaultBoxes = responseBoxes;
    
      return responseBoxes.size();
    } catch (Exception e) {
      e.printStackTrace();
      return -1; // if haven't chosen the preference, return negative
    }
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    //check validation of foodBoxId
    int range = getFoodBoxNumber();
    if (foodBoxId <= 0 || foodBoxId > range) return null;
    
    // construct the endpoint request
    String request = "/showFoodBox?";
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
      
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if (b.id == foodBoxId) return b.diet;
      }
    } catch (Exception e) {
      e.printStackTrace();
      
    }
    return null;
    /*
    Collection<String> none = showFoodBoxes("none");
    for (String i: none) {
      if (Integer.parseInt(i) == foodBoxId) return "none";
    }
    Collection<String> pollo = showFoodBoxes("pollotarian");
    for (String i: pollo) {
      if (Integer.parseInt(i) == foodBoxId) return "pollotarian";
    }
    Collection<String> vegan = showFoodBoxes("vegan");
    for (String i: vegan) {
      if (Integer.parseInt(i) == foodBoxId) return "vegan";
    }
    */
    
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {     //item diet:getDiretary(id)
    // check individual's validity to use methods          //list of box = showfoodbox(diet)
    if (!isRegistered()) return -1;                        //iterate list to get the box
                                                           //get item num for food box
    //check validation of foodBoxId
    int range = getFoodBoxNumber();
    if (foodBoxId <= 0 || foodBoxId > range) return -1;
  
    // construct the endpoint request
    String request = "/showFoodBox?";
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
    
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if (b.id == foodBoxId) return b.contents.size();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
    
    /*
    String preference = getDietaryPreferenceForFoodBox(foodBoxId);
    showFoodBoxes(preference); // also set boxOfPreference to the list of foodbox of this preference
    assert boxOfPreference != null;
    
    MessagingFoodBox box = null;
    //get index of this box in this id list
    for (MessagingFoodBox b: boxOfPreference) {
      if (b.id == foodBoxId) {
        box = b;
        break;
      }
    }
    if (box == null) return -1; // if failed, return negative
    return box.contents.size();
    */
  }

  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    //check validation of foodBoxId
    int range = getFoodBoxNumber();
    if (foodboxId <= 0 || foodboxId > range) return null;
    System.out.println(7);
    // construct the endpoint request
    String request = "/showFoodBox?";
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
    Collection<Integer> items = new ArrayList<Integer>();
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
    
      MessagingFoodBox foodBox = null;
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if (b.id == foodboxId) {
          foodBox = b;
          break;
        }
      }
      System.out.println(8);
      if (foodBox == null) return null;
      System.out.println(9);
      
      for (Content c: foodBox.contents) {
        items.add(c.id);
      }
      return items;
      
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(10);
      return null;
    }
    
    
    /*
    String preference = getDietaryPreferenceForFoodBox(foodboxId);
    showFoodBoxes(preference); // also set boxOfPreference to the list of foodbox of this preference
    assert boxOfPreference != null;
  
    MessagingFoodBox box = null;
    //get index of this box in this id list
    for (MessagingFoodBox b: boxOfPreference) {
      if (b.id == foodboxId) {
        box = b;
        break;
      }
    }
    if (box == null) return null; // if no matching box, return null
    Collection<Integer> items = new ArrayList<Integer>();
    for (Content c: box.contents) {
      items.add(c.id);
    }
    return items;
     */
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    //check validation of foodBoxId
    int range = getFoodBoxNumber();
    if (foodBoxId <= 0 || foodBoxId > range) return null;
    
    // construct the endpoint request
    String request = "/showFoodBox?";
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
    
      MessagingFoodBox foodBox = null;
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if (b.id == foodBoxId) {
          foodBox = b;
          break;
        }
      }
      if (foodBox == null) return null;
    
      for (Content c: foodBox.contents) {
        if (c.id == itemId) return c.name;
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    /*
    String preference = getDietaryPreferenceForFoodBox(foodBoxId);
    showFoodBoxes(preference); // also set boxOfPreference to the list of foodbox of this preference
    assert boxOfPreference != null;
  
    MessagingFoodBox box = null;
    //get index of this box in this id list
    for (MessagingFoodBox b: boxOfPreference) {
      if (b.id == foodBoxId) {
        box = b;
        break;
      }
    }
    if (box == null) return null; // if no matching box, return null
    
    for (Content c: box.contents) {
      if (c.id == itemId) return c.name;
    }
    */
    return null;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return -1;
    
    //check validation of foodBoxId
    int range = getFoodBoxNumber();
    if (foodBoxId <= 0 || foodBoxId > range) return -1;
  
    // construct the endpoint request
    String request = "/showFoodBox?";
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
    
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
    
      MessagingFoodBox foodBox = null;
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if (b.id == foodBoxId) {
          foodBox = b;
          break;
        }
      }
      if (foodBox == null) return -1;
    
      for (Content c: foodBox.contents) {
        if (c.id == itemId) return c.quantity;
      }
    
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
    /*
    String preference = getDietaryPreferenceForFoodBox(foodBoxId);
    showFoodBoxes(preference); // also set boxOfPreference to the list of foodbox of this preference
    assert boxOfPreference != null;
  
    MessagingFoodBox box = null;
    //get index of this box in this id list
    for (MessagingFoodBox b: boxOfPreference) {
      if (b.id == foodBoxId) {
        box = b;
        break;
      }
    }
    if (box == null) return -1; // if no matching box, return negative
  
    for (Content c: box.contents) {
      if (c.id == itemId) return c.quantity;
    }
    return -1;
     */
   
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    // check individual's validity to use methods
    if (!isRegistered()) return false;
    
    //check validation of foodBoxId
    int range = getFoodBoxNumber();
    if (foodBoxId <= 0 || foodBoxId > range) {
      marked = null; // check Piazza 693
      return false;
    }
  
    // construct the endpoint request
    String request = "/showFoodBox?";
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
      
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
      
      MessagingFoodBox fb = null;
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        //System.out.println("box: " + b.id);
        if (b.id == foodBoxId) {
          fb = b;
          break;
        }
      }
//      TODO: this delete?
      if (fb == null) System.out.println("NULL");
      marked = fb;
      if (marked != null) return true;
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
    /*
    // if haven't set, then set the marked box
    String preference = getDietaryPreferenceForFoodBox(foodBoxId);
    showFoodBoxes(preference); // also set boxOfPreference to the list of foodbox of this preference
    assert boxOfPreference != null;
  
    MessagingFoodBox box = null;
    
    for (MessagingFoodBox b: boxOfPreference) {
      if (b.id == foodBoxId) {
        box = b;
        break;
      }
    }
    if (box == null) return false; // if no matching box, return negative
    marked = box;
    return true;
    */
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    // check individual's validity to use methods
    if (!isRegistered()) return false;
    
    // check validation of inputs
    if (marked == null) return false;
    
    int boxId = marked.id;
    Collection<Integer> itemIds = getItemIdsForFoodBox(boxId);
    if (!itemIds.contains(itemId)) return false;
    int q = getItemQuantityForFoodBox(itemId,boxId);
    if (q < quantity || quantity < 0) return false;
    // change
    for (Content c: marked.contents) {
      if (c.id == itemId) {
        c.quantity = quantity;
        return true; // revise
      }
    }
    return false; // revise
    
  }

  @Override
  public Collection<Integer> getOrderNumbers() {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    if (boxOrders == null) return null;
    
    Collection<Integer> ids = new ArrayList<Integer>();
    for (Order o: boxOrders) {
      ids.add(o.orderId);
    }
    return ids;
  }

  @Override
  //Todo: check server first
  public String getStatusForOrder(int orderNumber) {
    // check individual's validity to use methods
    if (!isRegistered()) return null;
    
    //check validation of inputs
    if (boxOrders == null) return null;
    
    for (Order o: boxOrders) {
      if (o.orderId == orderNumber) {
        if (o.status == 0) return "placed";
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
        toBeEdited = o; //TODO: need deep copy
        break;
      }
    }
    return toBeEdited != null;
  }
  */
  
  
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
  
  @Override
  //Todo: so have no previous knowledge about server at all
  //Todo: if decrease quantity first
  
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
          return true;
        }
      }
    }
    return false;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

  // **UPDATE**
  @Override
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
  
  //------------------------- getter/setter for testing--------------------------
  public MessagingFoodBox getMarked() {return this.marked;}
  public void setMarked(MessagingFoodBox b) {this.marked = b;}
  public Collection<Order> getBoxOrders() {return this.boxOrders;}
  public ShieldingIndividual getShieldingIndividual() {return this.shieldingIndividual;}
  
  public void setBoxOrders(List<Order> boxOrders) {
    this.boxOrders = boxOrders;
  }
  
  //public Order getLatest() {return this.latest;}
  //public void setLatest(Order o) {
    //this.latest = o;
  //}
  
  public Order getToBeEdited() { return this.toBeEdited; }
  public void setToBeEdited(Order o) { this.toBeEdited = o; }
  
  public void setShieldingIndividual(String CHI){
    shieldingIndividual.CHI = CHI;
    shieldingIndividual.registered = true;
    marked = new MessagingFoodBox();
    // add marked content id .....
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
}
