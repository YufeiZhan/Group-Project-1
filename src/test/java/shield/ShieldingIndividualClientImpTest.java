/**
 *
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";
  private Properties clientProps;
  
  private final String generalTestPostcode = "EH16_5AY";
  
  private ShieldingIndividualClientImp newClient;
  private String newCHI;
  
  private ShieldingIndividualClientImp registeredClient; // with no order history
  private String registeredCHI;
  private String registeredValidOrderNum;
  private int registeredInvalidOrderNum;
  
  private ShieldingIndividualClientImp closestCateringCompanyClient;
  private String closestCateringCompanyName;
  
  private ShieldingIndividualClientImp registeredClient2; // with latest order placed this week
  private String testCHI2;
  private int orderId1;
  private int orderId2;
  
  private ShieldingIndividualClientImp registeredClient3; // with latest order placed last week
  private String testCHI3;

  private int orderId3;
  
  private int invalidOrderId = 5789;

  
  //Edit Box
//  private ShieldingIndividualClientImp editBoxClient;
//  private String editBoxUserCHI;
//  private int editBoxOrderNum;
  
  //Cancel Order
  private ShieldingIndividualClientImp cancelOrderClient;
  private String cancelOrderUserCHI;
  private String cancelOrderCateringCompanyName;
  private String cancelOrderCateringCompanyPostCode;
  
  //Request Order
  private ShieldingIndividualClientImp requestOrderClient;
  private String requestOrderUserCHI;
  private String requestOrderCateringCompanyName;
  private String requestOrderCateringCompanyPostCode;

  private Properties loadProperties(String propsFilename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties props = new Properties();

    try {
      InputStream propsStream = loader.getResourceAsStream(propsFilename);
      props.load(propsStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return props;
  }

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);
  
    newClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    newCHI = "0101111245";
    
    registeredClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    registeredCHI = "0505150000";
    String userRegistrationRequest = "/registerShieldingIndividual?CHI="+registeredCHI;
    
    // ------- Edit Order -------
    registeredClient2 = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));  //add
    testCHI2 = "0505150110"; //add
    String userRegistration2Request = "/registerShieldingIndividual?CHI="+testCHI2; //add
  
    registeredClient3 = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    testCHI3 = "0504130000";
    String userRegistration3Request = "/registerShieldingIndividual?CHI="+testCHI3;

    // ------- Cancel Order -------
    cancelOrderClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cancelOrderUserCHI = "0202020000";
    String cancelOrderUserRegistrationRequest = "/registerShieldingIndividual?CHI="+cancelOrderUserCHI;
    cancelOrderCateringCompanyName = "cancelOrderTestCateringCompany";
    cancelOrderCateringCompanyPostCode = "EH6_5GG";
    
    String cancelOrderCateringCompanyRegistrationRequest = "/registerCateringCompany?business_name=" + cancelOrderCateringCompanyName
            + "&postcode=" + cancelOrderCateringCompanyPostCode;
  
    // ------- Request Order -------
    requestOrderClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    requestOrderUserCHI = "0606060000";
    String requestOrderUserRegistrationRequest = "/registerShieldingIndividual?CHI="+requestOrderUserCHI;
    requestOrderCateringCompanyName = "requestOrderTestCateringCompany";
    requestOrderCateringCompanyPostCode = "EH1_1AA";
    String requestOrderCateringCompanyRegistrationRequest = "/registerCateringCompany?business_name=" + requestOrderCateringCompanyName
            + "&postcode=" + requestOrderCateringCompanyPostCode;
    
    // ------- Closest Catering Company -------
    closestCateringCompanyName = "tempCateringCompanyForTestInShieldingClient";
    String closestCateringCompanyRegistrationRequest = "/registerCateringCompany?business_name="+closestCateringCompanyName+"&postcode="+generalTestPostcode;
    closestCateringCompanyClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    String closestCateringCompanyCHI = "0202020000";
    String closestCateringCompanyClientRegistrationRequest = "/registerShieldingIndividual?CHI=" + closestCateringCompanyCHI;
    
  
    try {
      //-------------Closest Company----------------
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + closestCateringCompanyRegistrationRequest);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + closestCateringCompanyClientRegistrationRequest);
      closestCateringCompanyClient.setShieldingIndividual(closestCateringCompanyCHI,generalTestPostcode);
      
      //--------for registeredClient-----------
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistrationRequest);
//      registeredValidOrderNum = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + placedOrderRequest);
      registeredClient.setShieldingIndividual(registeredCHI,generalTestPostcode);
//      registeredClient.setStagedFoodBox();
      registeredClient.setCateringCompany(closestCateringCompanyName,generalTestPostcode);
  
      //===============setOrders=================
      String box1 = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
                    "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}],\"delivered_by\":\"catering\"," +
                    "\"diet\":\"none\",\"id\":1,\"name\":\"box a\"}";
      String content1 = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
              "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}]}";
      Type listType = new TypeToken<ShieldingIndividualClientImp.MessagingFoodBox>() {} .getType();
      
      //============for registeredClient2===============
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistration2Request); //add
      registeredClient2.setShieldingIndividual(testCHI2,generalTestPostcode); //add
      registeredClient2.setCateringCompany(closestCateringCompanyName,generalTestPostcode); //add
      ShieldingIndividualClientImp.MessagingFoodBox b1 = new Gson().fromJson(box1, listType);
      ShieldingIndividualClientImp.MessagingFoodBox b2 = new Gson().fromJson(box1, listType);
      
      // prepare place order request to server
      String x = testCHI2;
      String z = closestCateringCompanyName;
      String w = generalTestPostcode;
      String o2 = "/placeOrder?individual_id="+ x +
              "&catering_business_name=" + z +
              "&catering_postcode=" + w;
      //-------------update server for order 1----------------
      String response1 = ClientIO.doPOSTRequest(clientProps.getProperty("endpoint")+o2, content1);
      orderId1 = Integer.parseInt(response1);
      // set desired order status
      String status1 = "/updateOrderStatus?order_id="+orderId1+"&newStatus=delivered";
      String statusRes1 = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + status1);
      //System.out.println(statusRes1);
      assertEquals("True", statusRes1);
      //-------------update server for order 2----------------
      String response2 = ClientIO.doPOSTRequest(clientProps.getProperty("endpoint")+o2, content1);
      orderId2 = Integer.parseInt(response2);
      //-------------update local order history----------------
      ShieldingIndividualClientImp.Order newOrder1 = new ShieldingIndividualClientImp.Order();
      newOrder1.status = 3;
      newOrder1.orderId = orderId1;
      newOrder1.foodBox = b1;
      newOrder1.placeTime = LocalDateTime.of(2021,4,12,17,45,39);
      ShieldingIndividualClientImp.Order newOrder2 = new ShieldingIndividualClientImp.Order();
      newOrder2.status = 0;
      newOrder2.orderId = orderId2;
      newOrder2.foodBox = b2;
      newOrder2.placeTime = LocalDateTime.now();
      
      List<ShieldingIndividualClientImp.Order> orders = new ArrayList<ShieldingIndividualClientImp.Order>();
      orders.add(newOrder1);
      orders.add(newOrder2);
      registeredClient2.setBoxOrders(orders);
      
      //---------for registeredClient3------------
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistration3Request); //add
      registeredClient3.setShieldingIndividual(testCHI3,generalTestPostcode); //add
      registeredClient3.setCateringCompany(closestCateringCompanyName,generalTestPostcode);
      // place order to server
      x = testCHI3;
      z = closestCateringCompanyName;
      w = generalTestPostcode;
      String o3 = "/placeOrder?individual_id="+ x +
              "&catering_business_name=" + z +
              "&catering_postcode=" + w;
      
      String response3 = ClientIO.doPOSTRequest(clientProps.getProperty("endpoint")+o3, content1);
      orderId3 = Integer.parseInt(response3);
      // set desired order status
      String status3 = "/updateOrderStatus?order_id="+orderId3+"&newStatus=delivered";
      String statusRes3 = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + status3);
      assertEquals("True", statusRes3);
      
      ShieldingIndividualClientImp.MessagingFoodBox b3 = new Gson().fromJson(box1, listType);
      ShieldingIndividualClientImp.Order newOrder3 = new ShieldingIndividualClientImp.Order();
      newOrder3.status = 3;
      newOrder3.orderId = orderId3; //orginally 1
      newOrder3.foodBox = b3;
      newOrder3.placeTime = LocalDateTime.of(2021,4,12,17,45,39);
      
      List<ShieldingIndividualClientImp.Order> orders3 = new ArrayList<ShieldingIndividualClientImp.Order>();
      orders3.add(newOrder3);
      registeredClient3.setBoxOrders(orders3);
      //-------------Set invalid order number-------------
      
      while (invalidOrderId == orderId1 || invalidOrderId == orderId2 || invalidOrderId == orderId3) {
        invalidOrderId += 1;
      }
      
  
      //------------- Edit Box ----------------
//      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + editBoxUserRegistrationRequest);
//      editBoxClient.setShieldingIndividual(editBoxUserCHI,generalTestPostcode);
  
      //------------- Cancel Order ----------------
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + cancelOrderUserRegistrationRequest);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + cancelOrderCateringCompanyRegistrationRequest);
      cancelOrderClient.setShieldingIndividual(cancelOrderUserCHI, cancelOrderCateringCompanyPostCode);
      cancelOrderClient.setBoxOrders(cancelOrderClient.createDummyTestOrderList());
      // place order using the dummy ids
      Gson gson = new Gson();
      String cancelOrderPlaceOrderData = "{\"contents\":" + gson.toJson(cancelOrderClient.getBoxOrders().get(0).foodBox.contents) + "}";
      String cancelOrderPlaceOrderRequest = clientProps.getProperty("endpoint") + "/placeOrder?individual_id="+ cancelOrderUserCHI +
              "&catering_business_name=" + cancelOrderCateringCompanyName +
              "&catering_postcode=" + cancelOrderCateringCompanyPostCode;
      // update server with correct dummy orders' status
      for(int i = 0; i <= 2; i++){
        //update correct ids
        cancelOrderClient.getBoxOrders().get(i).orderId = Integer.parseInt(ClientIO.doPOSTRequest(cancelOrderPlaceOrderRequest,cancelOrderPlaceOrderData));
        String cancelOrderUpdateStatusRequest = "/updateOrderStatus?order_id="+ cancelOrderClient.getBoxOrders().get(i).orderId +
                "&newStatus=" + cancelOrderClient.convertIntStatusToString(i); //probelmmmmmmmm
        cancelOrderClient.setOrderListStatus(i,i);
        ClientIO.doGETRequest(clientProps.getProperty("endpoint") + cancelOrderUpdateStatusRequest);
      }
      
      //-------------Request Order Status----------------
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + requestOrderUserRegistrationRequest);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + requestOrderCateringCompanyRegistrationRequest);
      requestOrderClient.setShieldingIndividual(requestOrderUserCHI, requestOrderCateringCompanyPostCode);
      requestOrderClient.setBoxOrders(requestOrderClient.createDummyTestOrderList());
      // place order using the dummy ids
      String requestOrderPlaceOrderData = "{\"contents\":" + gson.toJson(requestOrderClient.getBoxOrders().get(0).foodBox.contents) + "}";
      String requestOrderPlaceOrderRequest = clientProps.getProperty("endpoint") + "/placeOrder?individual_id="+ requestOrderUserCHI +
              "&catering_business_name=" + requestOrderCateringCompanyName +
              "&catering_postcode=" + requestOrderCateringCompanyPostCode;
      // update server with correct dummy orders' status
      for(int i = 0; i <= 2; i++){
        //update correct ids
        requestOrderClient.getBoxOrders().get(i).orderId = Integer.parseInt(ClientIO.doPOSTRequest(requestOrderPlaceOrderRequest,requestOrderPlaceOrderData));
        String requestOrderUpdateStatusRequest = "/updateOrderStatus?order_id="+ requestOrderClient.getBoxOrders().get(i).orderId +
                "&newStatus=" + requestOrderClient.convertIntStatusToString(i);
        requestOrderClient.setOrderListStatus(i,i);
        ClientIO.doGETRequest(clientProps.getProperty("endpoint") + requestOrderUpdateStatusRequest);
      }
      

    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }


  @Test
  public void testShieldingIndividualNewRegistration() {
    
    //test new client registration
    assertFalse(newClient.isRegistered());
    assertTrue(newClient.registerShieldingIndividual(newCHI));
    newClient.setShieldingIndividual(newCHI,generalTestPostcode);
    assertTrue(newClient.isRegistered());
    assertEquals(newCHI,newClient.getCHI(),"Newly registered user should have identical CHI");

    // test already registered client
    assertTrue(newClient.registerShieldingIndividual(newCHI));
    assertTrue(registeredClient.registerShieldingIndividual(registeredCHI));
    assertTrue(registeredClient.isRegistered());
    assertEquals(registeredCHI,registeredClient.getCHI(),"Identical CHI required.");
    
    //test invalid CHI
  }
  
  @Test
  public void testShowFoodBoxes() {
    // test none
    List<String> noneFoodBoxes = new ArrayList<String>(Arrays.asList("1","3","4"));
    assertEquals(3, registeredClient.showFoodBoxes("none").size(),"No preference should return 3 food boxes.");
    assertEquals(noneFoodBoxes,registeredClient.showFoodBoxes("none"),"No preference gives food box 1,3,4");
    
    // test vegan
    List<String> veganFoodBoxes = new ArrayList<String>(Arrays.asList("5"));
    assertEquals(1,registeredClient.showFoodBoxes("vegan").size(),"Vegan preference should return 1 food box");
    assertEquals(veganFoodBoxes,registeredClient.showFoodBoxes("vegan"),"Vegan preference gives food box 5" );
    
    //test pollotarian
    List<String> polloFoodBoxes = new ArrayList<String>(Arrays.asList("2"));
    assertEquals(1,registeredClient.showFoodBoxes("pollotarian").size(),"Pollotarian preference should return 1 food box");
    assertEquals(polloFoodBoxes,registeredClient.showFoodBoxes("pollotarian"),"Pollotarian preference gives food box 2" );
    
    //test invalid user
    assertNull(newClient.showFoodBoxes("none"),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testPlaceOrder() {
    
    // test registered user that has placed order: If the latest one is within one week
    String box1 = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
                    "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}],\"delivered_by\":\"catering\"," +
                    "\"diet\":\"none\",\"id\":1,\"name\":\"box a\"}";
    Type listType = new TypeToken<ShieldingIndividualClientImp.MessagingFoodBox>() {} .getType();
    ShieldingIndividualClientImp.MessagingFoodBox b1 = new Gson().fromJson(box1, listType);
    registeredClient2.setMarked(b1);
    assertFalse(registeredClient2.placeOrder());
    // test registered user that has placed order: If the latest one is not within one week
    registeredClient3.setMarked(b1);
    assertTrue(registeredClient3.placeOrder());
    
    // test registered user that hasn't picked food box
    registeredClient.setMarked(null);
    assertFalse(registeredClient.placeOrder());
    registeredClient3.setMarked(null);
    assertFalse(registeredClient3.placeOrder());
    
    // test registered user that has picked food box and hasn't placed order
    registeredClient.setMarked(b1);
    assertTrue(registeredClient.placeOrder());
    
    // test unregistered user
    assertFalse(newClient.placeOrder(), "Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testEditOrder() {
    // prepare order
    String boxEdited = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":0},{\"id\":2,\"name\":\"tomatoes\"," +
            "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}],\"delivered_by\":\"catering\"," +
            "\"diet\":\"none\",\"id\":1,\"name\":\"box a\"}"; // change cucumber quantity from 1 to 0
    Type listType = new TypeToken<ShieldingIndividualClientImp.MessagingFoodBox>() {} .getType();
    ShieldingIndividualClientImp.MessagingFoodBox b1 = new Gson().fromJson(boxEdited, listType);
    ShieldingIndividualClientImp.Order order = new ShieldingIndividualClientImp.Order();
    order.status = 0;
    order.foodBox = b1;
    order.placeTime = LocalDateTime.of(2021,4,25,17,45,39);
    
    // registered user with no order history / registered user didn't use own order id
    assertFalse(registeredClient.editOrder(orderId2));
    // registered user with order history that could be edited; but haven't modify existing order
    registeredClient2.setToBeEdited(null);
    assertFalse(registeredClient2.editOrder(orderId2));
    // registered user with order history that could be edited; but modify non existing order
    assertFalse(registeredClient2.editOrder(invalidOrderId));
    // registered user with order history that could be edited; try to edit but modified another order
    order.orderId = orderId1;
    registeredClient2.setToBeEdited(order);
    assertFalse(registeredClient2.editOrder(orderId2));
    // registered user with order history that could be edited; successfully edit
    order.orderId = orderId2;
    registeredClient2.setToBeEdited(order);
    assertTrue(registeredClient2.editOrder(orderId2));
    ShieldingIndividualClientImp.MessagingFoodBox localContent = registeredClient2.getBoxOrders().get(1).foodBox;
    assertTrue(localContent.contents.get(0).quantity == 0);
    // registered user with order history that could not be edited
    order.orderId = orderId3;
    order.status = 3;
    registeredClient3.setToBeEdited(order);
    assertFalse(registeredClient3.editOrder(orderId3));
    assertNull(registeredClient2.getToBeEdited());
    // test unregistered user
    assertFalse(newClient.editOrder(orderId2),"Unregistered user shouldn't be able to use this method.");

    // 1. registered user without setting item quantity
//    editBoxClient.setToBeEdited(null);
//    assertFalse(editBoxClient.editOrder(editBoxOrderNum),"Need to set item quantity before updating server.");
    
    // 2. registered user after setting item quantity
    // 2.1 Different order content in toBeEdited and server
    
    // 2.2 Call EditOrder()
    
    // 2.3 Same order content in toBeEdited and server
    
  
    // 3. test unregistered user
//    assertFalse(newClient.editOrder(editBoxOrderNum),"Unregistered user shouldn't be able to use this method.");

  }
  
  
  @Test
  public void testCancelOrder() {
    // test registered user with invalid order number
    assertFalse(cancelOrderClient.cancelOrder(500),"Cancelling Order with invalid order number should be invalid operation.");
    
    // test registered user with invalid order status
    assertFalse(cancelOrderClient.cancelOrder(cancelOrderClient.getBoxOrders().get(2).orderId),"Cancelling Order with valid order status should be valid operation.");
  
    // test registered user cancel order successfully
    // reminder that dispatched cannot be cancelled as well
    for(int i = 0; i < 2; i++){
      assertTrue(cancelOrderClient.cancelOrder(cancelOrderClient.getBoxOrders().get(i).orderId),"Cancelling Order with invalid order status should be invalid operation.");
    }
    
    // test unregistered user
    assertFalse(newClient.cancelOrder(50),"Unregistered user shouldn't be able to use this method.");
  }
  
  
  @Test
  public void testRequestOrderStatus() { //required placed order to check order status
    //test registered user with invalid order number
    assertFalse(requestOrderClient.requestOrderStatus(10000),"Invalid order shouldn't request status successful.");
    
    //test registered user with valid order number
    for(int i = 0; i <= 1; i++) {
      assertTrue(requestOrderClient.requestOrderStatus(requestOrderClient.getBoxOrders().get(i).orderId));
    }
    
    //test unregistered user
    assertFalse(newClient.requestOrderStatus(100),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetCateringCompanies() {
    //test registered user
    assertTrue(registeredClient.getCateringCompanies().size() >=0, "Number of catering companies should be bigger than 0");
    
    //test unregistered user
    assertNull(newClient.getCateringCompanies(),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetDistance() {
    // test same postcode
    String postCode = "EH1_9RA";
    assertEquals(0,registeredClient.getDistance(postCode,postCode),"Same postcode returns 0" );
    
    // test a set of reversed postcodes
    String postCode1 = "EH1_9RA";
    String postCode2 = "EH1_1RA";
    assertEquals(registeredClient.getDistance(postCode1,postCode2),registeredClient.getDistance(postCode2,postCode1),"Same postcode distance even though reversed." );
    
    // test different postcode's distance non-zero
    assertNotEquals(0,registeredClient.getDistance(postCode1,postCode2),"Different postcodes give non-zero distance.");
  
    //test unregistered user
    assertEquals(-1,newClient.getDistance(postCode1,postCode2),"Should return -1 when unregistered user uses this method." );
  
  }
  
  @Test
  public void testGetFoodBoxNumber() {
    // test registered user
    assertEquals(5,registeredClient.getFoodBoxNumber(),"Should return 5 food boxes");
    
    // test unregistered user
    assertEquals(-1,newClient.getFoodBoxNumber(),"Should return -1 when unregistered user uses this method.");
  }
  
  @Test
  public void testGetDietaryPreferenceForFoodBox() {
    // test registered user operations: 1 - none; 2 - pollo; 3,4 - none; 5 -vegan;
    assertEquals("none",registeredClient.getDietaryPreferenceForFoodBox(1),"Should return none");
    assertEquals("pollotarian",registeredClient.getDietaryPreferenceForFoodBox(2),"Should return pollotarian");
    assertEquals("none",registeredClient.getDietaryPreferenceForFoodBox(3),"Should return none");
    assertEquals("none",registeredClient.getDietaryPreferenceForFoodBox(4),"Should return none");
    assertEquals("vegan",registeredClient.getDietaryPreferenceForFoodBox(5),"Should return vegan");
    
    // test unregistered user
    assertNull(newClient.getDietaryPreferenceForFoodBox(5),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetItemsNumberForFoodBox() {
    // test registered user: 1 - 3; 2 - 3; 3 - 3; 4 - 4; 5 - 3;
    assertEquals(3, registeredClient.getItemsNumberForFoodBox(1),"Food box 1 should return 3 items.");
    assertEquals(3, registeredClient.getItemsNumberForFoodBox(2),"Food box 2 should return 3 items.");
    assertEquals(3, registeredClient.getItemsNumberForFoodBox(3),"Food box 3 should return 3 items.");
    assertEquals(4, registeredClient.getItemsNumberForFoodBox(4),"Food box 4 should return 4 items.");
    assertEquals(3, registeredClient.getItemsNumberForFoodBox(5),"Food box 5 should return 3 items.");
    
    // test unregistered user
    assertEquals(-1, newClient.getItemsNumberForFoodBox(5),"Unregistered user shouldn't be able to use this method.");
  
  
  }
  
  @Test
  public void testGetItemIdsForFoodBox() {
    //test registered user: 1 - [1,2,6]; 2 - [1,3,7]; 3 - [3,4,8]; 4 - [13,11,8,9]; 5 - [9,11,12]
    List<Integer> foodBox1 = new ArrayList<Integer>(Arrays.asList(1,2,6));
    assertEquals(foodBox1,registeredClient.getItemIdsForFoodBox(1),"Food box 1 should return id 1,2,6.");
    List<Integer> foodBox2 = new ArrayList<Integer>(Arrays.asList(1,3,7));
    assertEquals(foodBox2,registeredClient.getItemIdsForFoodBox(2),"Food box 2 should return id 1,3,7.");
    List<Integer> foodBox3 = new ArrayList<Integer>(Arrays.asList(3,4,8));
    assertEquals(foodBox3,registeredClient.getItemIdsForFoodBox(3),"Food box 3 should return id 3,4,8.");
    List<Integer> foodBox4 = new ArrayList<Integer>(Arrays.asList(13,11,8,9));
    assertEquals(foodBox4,registeredClient.getItemIdsForFoodBox(4),"Food box 4 should return id 13,11,8,9.");
    List<Integer> foodBox5 = new ArrayList<Integer>(Arrays.asList(9,11,12));
    assertEquals(foodBox5,registeredClient.getItemIdsForFoodBox(5),"Food box 5 should return id 9,11,12.");
  
    //test unregistered user
    assertNull(newClient.getItemIdsForFoodBox(2),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetItemNameForFoodBox() {
    //test registered user: (1,1) - cucumbers; (3,2) - onions; (4,3) - carrots; (13,4) - cabbage; (12,5) - mango
    assertEquals("cucumbers",registeredClient.getItemNameForFoodBox(1,1),"Cucumbers for item 1 of box 1");
    assertEquals("onions",registeredClient.getItemNameForFoodBox(3,2),"Onions for item 3 and box 2");
    assertEquals("carrots",registeredClient.getItemNameForFoodBox(4,3),"Carrots for item 4 and box 3");
    assertEquals("cabbage",registeredClient.getItemNameForFoodBox(13,4),"Cabbage for item 13 and box 4");
    assertEquals("mango",registeredClient.getItemNameForFoodBox(12,5),"Mango for item 12 and box 5");
  
    //test ujnregistered user
    assertNull(newClient.getItemNameForFoodBox(1,6),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetItemQuantityForFoodBox() {
    //test registered user: (1,1) - 1; (3,2) - 1; (4,3) - 2; (13,4) - 1; (12,5) - 1
    assertEquals(1,registeredClient.getItemQuantityForFoodBox(1,1),"Quantity is 1 for item 1 of box 1");
    assertEquals(1,registeredClient.getItemQuantityForFoodBox(3,2),"Quantity is 1 for item 3 of box 2");
    assertEquals(2,registeredClient.getItemQuantityForFoodBox(4,3),"Quantity is 2 for item 4 of box 3");
    assertEquals(1,registeredClient.getItemQuantityForFoodBox(13,4),"Quantity is 1 for item 13 of box 4");
    assertEquals(1,registeredClient.getItemQuantityForFoodBox(12,5),"Quantity is 1 for item 12 of box 5");
  
    //test unregistered user
    assertEquals(-1,newClient.getItemQuantityForFoodBox(1,6),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testPickFoodBox() {
    //test registered user with invalid food box
    assertFalse(registeredClient.pickFoodBox(registeredClient.getFoodBoxNumber()+1),"Invalid food box should be unsuccessful.");
    
    //test registered user with valid food box
    for (int i=1; i<=5; i++) {
      assertTrue(registeredClient.pickFoodBox(i), "Valid food box should be placed successfully.");
    }
    
    //test unregistered user with valid food box
    for (int i=1; i<=5; i++){
      assertFalse(newClient.pickFoodBox(i),"Unregistered user shouldn't be able to use this method.");
    }
  }
  
  @Test
  public void testChangeItemQuantityForPickedFoodBox() {
    // haven't picked any box (marked == null)
    registeredClient.setMarked(null);
    assertFalse(registeredClient.changeItemQuantityForPickedFoodBox(2, 1), "Should pick food box first");
    
    // pick food box 1: try to change item not exists
    registeredClient.pickFoodBox(1);
    assertFalse(registeredClient.changeItemQuantityForPickedFoodBox(3,1), "No such item in this food box");
    // pick food box 1: try to set existed item quantity more than default
    assertFalse(registeredClient.changeItemQuantityForPickedFoodBox(2,4), "Should only reduce quantity");
    // success scenario
    assertTrue(registeredClient.changeItemQuantityForPickedFoodBox(2,1));
    Collection<ShieldingIndividualClientImp.Content> cts = registeredClient.getMarked().contents;
    int changedQ = -1;
    for (ShieldingIndividualClientImp.Content c: cts) {
      if (c.id == 2) {
        changedQ = c.quantity;
        break;
      }
    }
    assertEquals(1,changedQ,"Should change to the reset quantity");
    
    //test unregistered user
    assertFalse(newClient.changeItemQuantityForPickedFoodBox(2,1),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetOrderNumbers() {
    // no order history
    assertEquals(0,registeredClient.getOrderNumbers().size(),"No order history");
    // check if returned correctly
    Collection<Integer> ids = new ArrayList<Integer>(Arrays.asList(orderId1,orderId2));
    assertEquals(ids, registeredClient2.getOrderNumbers(),"Incorrect number of orders");
    
    //test unregistered user
    assertNull(newClient.getOrderNumbers(),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetStatusForOrder() { // query sever
    // no order history
    assertNull(registeredClient.getStatusForOrder(orderId1),"Haven't ordered yet");
    assertNull(registeredClient.getStatusForOrder(orderId2),"Haven't ordered yet");
    // un-found order number
    assertNull(registeredClient2.getStatusForOrder(invalidOrderId),"No such order");
    // check if returned correctly
    assertEquals("delivered", registeredClient2.getStatusForOrder(orderId1));
    assertEquals("placed", registeredClient2.getStatusForOrder(orderId2));
    
    //test unregistered user
    assertNull(newClient.getStatusForOrder(orderId1),"Unregistered user shouldn't be able to use this method.");
    assertNull(newClient.getStatusForOrder(orderId2),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetItemIdsForOrder() {
    // no order history
    assertNull(registeredClient.getItemIdsForOrder(orderId1),"Haven't ordered yet");
    assertNull(registeredClient.getItemIdsForOrder(orderId2),"Haven't ordered yet");
    // un-found order number
    assertNull(registeredClient2.getItemIdsForOrder(invalidOrderId),"No such order");
    // check if returned correctly
    Collection<Integer> ids = new ArrayList<Integer>(Arrays.asList(1,2,6));
    assertEquals(ids, registeredClient2.getItemIdsForOrder(orderId1));
    assertEquals(ids, registeredClient2.getItemIdsForOrder(orderId2));
    
    //test unregistered user
    assertNull(newClient.getItemIdsForOrder(orderId1),"Unregistered user shouldn't be able to use this method.");
    assertNull(newClient.getItemIdsForOrder(orderId2),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetItemNameForOrder() {
    // no order history
    assertNull(registeredClient.getItemNameForOrder(1,orderId1),"Haven't ordered yet");
    assertNull(registeredClient.getItemNameForOrder(2,orderId1),"Haven't ordered yet");
    // un-found order number
    assertNull(registeredClient2.getItemNameForOrder(1,invalidOrderId),"No such order");
    // un-found order item
    assertNull(registeredClient2.getItemNameForOrder(4,orderId1),"No such item");
    assertNull(registeredClient2.getItemNameForOrder(7,orderId2),"No such item");
    // success scenario
    assertEquals("cucumbers", registeredClient2.getItemNameForOrder(1,orderId1),"Incorrect item");
    assertEquals("tomatoes", registeredClient2.getItemNameForOrder(2,orderId2),"Incorrect item");
    assertEquals("pork", registeredClient2.getItemNameForOrder(6,orderId1),"Incorrect item");
    
    //test unregistered user
    assertNull(newClient.getItemNameForOrder(1,orderId1),"Unregistered user shouldn't be able to use this method.");
    assertNull(newClient.getItemNameForOrder(2,orderId1),"Unregistered user shouldn't be able to use this method.");
  }
  
  
  @Test
  public void testGetItemQuantityForOrder() {
    // no order history
    assertEquals(-1,registeredClient.getItemQuantityForOrder(1,orderId1),"Haven't ordered yet");
    assertEquals(-1,registeredClient.getItemQuantityForOrder(2,orderId1),"Haven't ordered yet");
    // un-found order number
    assertEquals(-1,registeredClient2.getItemQuantityForOrder(1,invalidOrderId),"No such order");
    // un-found order item
    assertEquals(-1,registeredClient2.getItemQuantityForOrder(4,orderId1),"No such item");
    assertEquals(-1,registeredClient2.getItemQuantityForOrder(7,orderId2),"No such item");
    // success scenario
    assertEquals(1, registeredClient2.getItemQuantityForOrder(1,orderId1),"Incorrect quantity");
    assertEquals(2, registeredClient2.getItemQuantityForOrder(2,orderId2),"Incorrect quantity");
    assertEquals(1, registeredClient2.getItemQuantityForOrder(6,orderId1),"Incorrect quantity");
    
    //test unregistered user
    assertEquals(-1,newClient.getItemQuantityForOrder(1,orderId1),"Unregistered user shouldn't be able to use this method.");
    assertEquals(-1,newClient.getItemQuantityForOrder(2,orderId1),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testSetItemQuantityForOrder() {
    
    // no order history
    assertFalse(registeredClient.setItemQuantityForOrder(1,orderId2,0),"Haven't ordered yet");
    assertFalse(registeredClient.setItemQuantityForOrder(2,orderId2,0),"Haven't ordered yet");
    // un-found order number
    assertFalse(registeredClient2.setItemQuantityForOrder(1,invalidOrderId,0),"No such order");
    // un-found order item
    assertFalse(registeredClient2.setItemQuantityForOrder(4,orderId2,0),"No such item");
    assertFalse(registeredClient2.setItemQuantityForOrder(7,orderId2,0),"No such item");
    // invalid quantity
    assertFalse(registeredClient2.setItemQuantityForOrder(1,orderId2,4),"Quantity can only be reduced");
    assertFalse(registeredClient2.setItemQuantityForOrder(6,orderId2,4),"Quantity can only be reduced");
    // order that could not be successfully set (invalid for editing)
    assertFalse(registeredClient2.setItemQuantityForOrder(1,orderId1,0),"Order couldn't be edited");
    assertFalse(registeredClient2.setItemQuantityForOrder(6,orderId1,0),"Order couldn't be edited");
    // success scenario
    assertTrue(registeredClient2.setItemQuantityForOrder(1,orderId2,0),"Incorrect item");
    assertTrue(registeredClient2.setItemQuantityForOrder(2,orderId2,1),"Incorrect item");
    assertTrue(registeredClient2.setItemQuantityForOrder(6,orderId2,0),"Incorrect item");
    // the updates to latest
    // invalid user
    assertFalse(newClient.setItemQuantityForOrder(1,2,0),"New client shouldn't have ordered");
    assertFalse(newClient.setItemQuantityForOrder(2,2,1),"New client shouldn't have ordered");
  }
  
  @Test
  public void testGetClosestCateringCompany() {
    //test registered user
    System.out.println(closestCateringCompanyClient.getClosestCateringCompany());
    assertEquals(closestCateringCompanyName,closestCateringCompanyClient.getClosestCateringCompany(),"Closest Catering Company is incorrect.");
    
    // test unregistered user
    assertNull(newClient.getClosestCateringCompany(),"Unregistered user shouldn't be able to use this method.");
  }
  
  @Test
  public void testPickOrderToEdit() {
    // registered user with no order history / registered user didn't use own order id
    assertFalse(registeredClient.pickOrderToEdit(orderId2),"Haven't ordered yet");
    // registered user with order history that could be edited; but pick non existing order
    assertFalse(registeredClient2.pickOrderToEdit(invalidOrderId),"Invalid order number");
    // registered user with order history that could be edited; successfully edit
    assertTrue(registeredClient2.pickOrderToEdit(orderId2),"Fail success scenario");
    assertNotNull(registeredClient2.getToBeEdited(), "Fail to stage required order");
    Gson gson = new Gson();
    String staging = gson.toJson(registeredClient2.getToBeEdited());
    String local = gson.toJson(registeredClient2.getBoxOrders().get(1));
    assertEquals(staging, local,"toBeEdited should have same content with the specified order");
    assertNotEquals(registeredClient2.getToBeEdited(), registeredClient2.getBoxOrders().get(1), "toBeEdit should be a copy not same reference");
    // registered user with order history that could not be edited
    assertFalse(registeredClient3.pickOrderToEdit(orderId3), "No order could be edited");
    // test unregistered user
    assertFalse(newClient.pickOrderToEdit(orderId2),"Unregistered user shouldn't be able to use this method.");
    
    /*
    boolean success = registeredClient2.pickOrderToEdit(2);
    assert success;
    ShieldingIndividualClientImp.Order o = registeredClient2.getToBeEdited();
    assertEquals(2,o.orderId);
    assertEquals(0,o.status);
    assertEquals(LocalDateTime.of(2021,4,25,17,45,39),o.placeTime);
  
    String box1 = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
            "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}],\"delivered_by\":\"catering\"," +
            "\"diet\":\"none\",\"id\":1,\"name\":\"box a\"}";
    Type listType = new TypeToken<ShieldingIndividualClientImp.MessagingFoodBox>() {} .getType();
    ShieldingIndividualClientImp.MessagingFoodBox b1 = new Gson().fromJson(box1, listType);
    assertEquals(o.foodBox.id, b1.id);
    boolean check = registeredClient2.setItemQuantityForOrder(2,2,1);
    assert check;
    int amount = -1;
    Collection<ShieldingIndividualClientImp.Content> cts = registeredClient2.getToBeEdited().foodBox.contents;
    for (ShieldingIndividualClientImp.Content c: cts) {
      if (c.id == 2) amount = c.quantity;
    }
    assertEquals(1,amount);
    
    assertEquals(2,registeredClient2.getItemQuantityForOrder(2,2));
    
     */
  }
  
}