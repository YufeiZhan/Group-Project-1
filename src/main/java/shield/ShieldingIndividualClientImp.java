/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {
  /**
   * The string representation of the base server endpoint (a HTTP address)
   */
  private String endpoint;
  private ShieldingIndividual shieldingIndividual;
  private CateringCompany cateringCompany;
  private Collection<Integer> orderIds;
  private MessagingFoodBox marked = null;
  private List<MessagingFoodBox> boxOfPreference;
  
  private class ShieldingIndividual {
    String CHI;
    String postCode;
    String name;
    String surname;
    String phoneNumber;
    boolean registered = false;
  }
  
  private class CateringCompany {
    String id;
    String name;
    String postCode;
  }
  
  private class Order {
    int orderId;
  }
  
  
  // internal field only used for transmission purposes
  final class MessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    List<Content> contents;
    //transient
    String delivered_by;
    String diet;
    int id;
    String name;
  }
 
  final class Content {
    int id;
    String name;
    int quantity;
  }
  
  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
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
      
      if (response.equals("already registered")) return true;
      
      // unmarshal response
      Type listType = new TypeToken<Collection<String>>() {}.getType();
      responseDetail = new Gson().fromJson(response, listType);
      
      shieldingIndividual = new ShieldingIndividual();
      String pc = responseDetail.get(0);
      shieldingIndividual.postCode = pc.replace(' ', '_');
      shieldingIndividual.name = responseDetail.get(1);
      shieldingIndividual.surname = responseDetail.get(2);
      shieldingIndividual.phoneNumber = responseDetail.get(3);
      shieldingIndividual.CHI = CHI;
      shieldingIndividual.registered = true;
      getClosestCateringCompany();
      orderIds = new ArrayList<>();
      
      return true;
      
      
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    
  }

  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // check validation of inputs
    if (dietaryPreference == null) return null;
    
    String[] preference = {"none", "pollotarian", "vegan"};
    if (!Arrays.asList(preference).contains(dietaryPreference)) return null;
    
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;
  
    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
  
    List<String> boxIds = new ArrayList<String>();
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      assert response != null;
      //System.out.println(response);
      
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);
      
      boxOfPreference = responseBoxes; // save what we get locally
      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        /*
        for (Content c: b.contents) {
          System.out.println(c.id + " " + c.name + " "+ c.quantity);
        }
        */
        boxIds.add(String.valueOf(b.id));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    //System.out.println(boxOfPreference.size());
    return boxIds;
    //return null;
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
    // haven't implemented yet
    // precondition: a chosen food box has been staged to "marked"
    
    if (marked == null) return false;
    //System.out.println("s");
    // marshal data
    Gson gson = new Gson();
    String data = gson.toJson(marked.contents);
    data = "{\"contents\":" + data + "}";
    System.out.println(data);
    
    
    String x = shieldingIndividual.CHI;
    String z = cateringCompany.name;
    String w = cateringCompany.postCode;
    String request = "placeOrder?individual_id="+ x +
                     "&catering_business_name=" + z +
                     "&catering_postcode=" + w;
  
    request = endpoint + request;
  
    try {
      // perform request
      String response = ClientIO.doPOSTRequest(request, data);
      assert response != null;
      
      // add into the order history
      int id = Integer.parseInt(response);
      orderIds.add(id);
      
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
    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    return false;
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    // haven't been tested yet; as there was no registered catering company
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
      List<String> pureDetail = responseDetail.subList(1,responseDetail.size());
      assert pureDetail != null;
      /*
      for (String r: pureDetail) {
        System.out.println(r);
      }
      System.out.println(pureDetail.size());
      */
      return pureDetail;
    
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
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
  public int getFoodBoxNumber() { // only get the number of the boxes for chosen preference
    if (boxOfPreference != null) return boxOfPreference.size();
    return -1; // if haven't chosen the preference, return negative
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    //TODO: check validation of foodBoxId
    
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
  public int getItemsNumberForFoodBox(int foodBoxId) {
    //TODO: check validation of foodBoxId
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
    
  }

  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
    //TODO: check validation of foodBoxId
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
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    //TODO: check validation of foodBoxId
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
    
    return null;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    //TODO: check validation of foodBoxId
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
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    //TODO: check validation of foodBoxId
    
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
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    // check validation of inputs
    int boxId = marked.id;
    Collection<Integer> itemIds = getItemIdsForFoodBox(boxId);
    if (!itemIds.contains(itemId)) return false;
    int q = getItemQuantityForFoodBox(itemId,boxId);
    if (q < quantity) return false;
    
    // change
    for (Content c: marked.contents) {
      if (c.id == itemId) c.quantity = quantity;
      break;
    }
    
    return true;
  }

  @Override
  public Collection<Integer> getOrderNumbers() {
    
    return null;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {
    return null;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    return null;
  }

  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    return 0;
  }

  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    return false;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

  // **UPDATE**
  @Override
  public String getClosestCateringCompany() {
    Collection<String> companies = getCateringCompanies();
    assert companies != null:"Fail to Get Catering Companies";
    List<String> caters = new ArrayList<String>(companies);
    if (caters.size() < 1) return null;
  
    List<String> info = Arrays.asList(caters.get(0).split(","));
    String pc = info.get(2);
    float d = getDistance(shieldingIndividual.postCode,pc);
    //System.out.println(d);
    
    for (String c: caters.subList(1,caters.size())) {
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
}
