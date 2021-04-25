package shield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class SystemTests {
  
  // =============================================== System Test Set-Up ===============================================
  
  private final static String clientPropsFilename = "client.cfg";
  
  private Properties clientProps;
  
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
  

  // Supermarket Registration Use Case
  SupermarketClientImp invalidSupermarketRegistrationClient;
  String invalidSupermarketName;
  String invalidSupermarketPostCode;
  SupermarketClientImp newSupermarketRegistrationClient;
  String newSupermarketName;
  String newSupermarketPostCode;
  
  
  // Catering Company Registration Use Case
  CateringCompanyClientImp invalidCateringRegistrationClient;
  String invalidCateringCompanyName;
  String invalidCateringCompanyPostCode;
  CateringCompanyClientImp validCateringRegistrationClient;
  String validCateringCompanyName;
  String validCateringCompanyPostCode;
  
  // Shielding Individual Registration Use Case
  ShieldingIndividualClientImp invalidShieldingIndividualRegistrationClient;
  String invalidShieldingIndividualCHI;
  ShieldingIndividualClientImp validShieldingIndividualRegistrationClient;
  String validShieldingIndividualCHI;
  
  // Cancel Order Use Case
  ShieldingIndividualClientImp cancelOrderShieldingIndividualClient1;
  ShieldingIndividualClientImp cancelOrderShieldingIndividualClient2;
  ShieldingIndividualClientImp cancelOrderShieldingIndividualClient3;
  String cancelOrderShieldingIndividualCHI1;
  String cancelOrderShieldingIndividualCHI2;
  String cancelOrderShieldingIndividualCHI3;
  String cancelOrderShieldingIndividualPostcode1;
  String cancelOrderShieldingIndividualPostcode2;
  String cancelOrderShieldingIndividualPostcode3;
  CateringCompanyClientImp cancelOrderCateringClient;
  String cancelOrderCateringName;
  String cancelOrderCateringPostcode;
  
  
  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);
    
    // ---- Register Supermarket ----
    // invalid postcode
    invalidSupermarketRegistrationClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    invalidSupermarketName = "invalidSupermarktgRegistrationClientNameForSystemTest";
    invalidSupermarketPostCode = "EH19_5AY";
    // valid supermarket
    newSupermarketRegistrationClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    newSupermarketName = "newSupermarktgRegistrationClientNameForSystemTest";
    newSupermarketPostCode = "EH13_5AY";
    
    
    // ---- Register Catering Company ----
    invalidCateringRegistrationClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    invalidCateringCompanyName = "invalidCateringRegistrationClientNameForSystemTest";
    invalidCateringCompanyPostCode = "EH_5AY";
    validCateringRegistrationClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    validCateringCompanyName = "validCateringRegistrationClientNameForSystemTest";
    validCateringCompanyPostCode = "EH16_5AY";
    

    // ---- Register Shielding Individual ----
    invalidShieldingIndividualRegistrationClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    invalidShieldingIndividualCHI = "03080912f3234";
    validShieldingIndividualRegistrationClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    validShieldingIndividualCHI = "0308091234";
    
    
    // ---- Cancel Order ----
    cancelOrderCateringClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    cancelOrderShieldingIndividualClient1 = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cancelOrderShieldingIndividualClient2 = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cancelOrderShieldingIndividualClient3 = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cancelOrderShieldingIndividualCHI1 = "1010100000";
    cancelOrderShieldingIndividualCHI2 = "1111110000";
    cancelOrderShieldingIndividualCHI3 = "1212120000";
    cancelOrderShieldingIndividualPostcode1 = "EH1_1AA";
    cancelOrderShieldingIndividualPostcode2 = "EH1_1AB";
    cancelOrderShieldingIndividualPostcode3 = "EH1_1AC";
    cancelOrderCateringName = "cancelOrderCateringCompanyNameForSystemTest";
    cancelOrderCateringPostcode = "EH2_1AA";
    

    // try {
    
    // } catch (IOException e) {
    //   e.printStackTrace();
    // }
  }
  
  
  // =============================================== System Tests ===============================================
  @Test
  public void testRegisterSupermarketUseCase() {
    //test invalid supermarket registration
    assertFalse(invalidSupermarketRegistrationClient.registerSupermarket(invalidSupermarketName,invalidSupermarketPostCode),"Supermarket with invalid postcode should not register.");
    assertFalse(invalidSupermarketRegistrationClient.isRegistered(),"Unregistered supermarket should not be registered.");
    assertNull(invalidSupermarketRegistrationClient.getName(),"Unregistered supermarket should not have a name.");
    assertNull(invalidSupermarketRegistrationClient.getPostCode(),"Unregistered supermarket should not have a postcode.");
    assertEquals(0, invalidSupermarketRegistrationClient.getOrderList().size(), "Unregistered supermarket should have empty order list.");
    //test valid supermarket registration
    //new registration
    assertFalse(newSupermarketRegistrationClient.isRegistered(), "New Supermarket with valid postcode should register successfully");
    assertTrue(newSupermarketRegistrationClient.registerSupermarket(newSupermarketName,newSupermarketPostCode),"Should registered successfully");
    assertEquals(newSupermarketName, newSupermarketRegistrationClient.getName(),"Should match the name for registration");
    assertEquals(newSupermarketPostCode,newSupermarketRegistrationClient.getPostCode(),"Should match the post code for registration");
    assertEquals(0, newSupermarketRegistrationClient.getOrderList().size(), "New supermarket should have empty order list.");
    //registered registration
    assertTrue(newSupermarketRegistrationClient.isRegistered(), "Should have been registered");
    assertTrue(newSupermarketRegistrationClient.registerSupermarket(newSupermarketName,newSupermarketPostCode),"Should registered successfully");
    
  }
  
  
  @Test
  public void testRegisterCateringCompanyUseCase() {
    //test invalid catering company registration
    assertFalse(invalidCateringRegistrationClient.registerCateringCompany(invalidCateringCompanyName,invalidCateringCompanyPostCode),"Company with invalid postcode should not register.");
    assertFalse(invalidCateringRegistrationClient.isRegistered(),"Unregistered company should not be registered.");
    assertNull(invalidCateringRegistrationClient.getName(),"Unregistered company should not have a name.");
    assertNull(invalidCateringRegistrationClient.getPostCode(),"Unregistered company should not have a postcode.");
    
    //test valid catering company registration
    assertTrue(validCateringRegistrationClient.registerCateringCompany(validCateringCompanyName,validCateringCompanyPostCode),"Company with valid postcode should register successfully.");
    // TODO: when run the test for the second time, turn this setter on to ensure consistency with the server
//    validCateringRegistrationClient.setCateringCompany(validCateringCompanyName,validCateringCompanyPostCode);    assertTrue(validCateringRegistrationClient.isRegistered(),"Registered company should be registered.");
    assertEquals(validCateringCompanyName,validCateringRegistrationClient.getName(),"Registered company should have a matching name.");
    assertEquals(validCateringCompanyPostCode,validCateringRegistrationClient.getPostCode(),"Registered company should have a matching postcode.");
  }
  
  
  @Test
  public void testRegisterShieldingIndividualUseCase() {
    // test invalid shielding individual registration
    assertFalse(invalidShieldingIndividualRegistrationClient.registerShieldingIndividual(invalidShieldingIndividualCHI), "User with Invalid CHI should not register successfully");
    assertFalse(invalidShieldingIndividualRegistrationClient.isRegistered(), "Unregistered User should not be registered");
    assertNull(invalidShieldingIndividualRegistrationClient.getCHI(), "unregistered User should not be recorded CHI");
    // test valid shielding individual registration
    // new registration
    assertFalse(validShieldingIndividualRegistrationClient.isRegistered(), "New user should not have registered");
    assertTrue(validShieldingIndividualRegistrationClient.registerShieldingIndividual(validShieldingIndividualCHI), "User with valid CHi should register successfully");
    assertEquals(validShieldingIndividualCHI, validShieldingIndividualRegistrationClient.getCHI(), "CHI should match registering CHI");
    // registered registration
    assertTrue(validShieldingIndividualRegistrationClient.isRegistered(),"Registered user should have registered");
    assertTrue(validShieldingIndividualRegistrationClient.registerShieldingIndividual(validShieldingIndividualCHI),"Registered user with valid CHi should register successfully");
  }
  
  @Test
  public void testPlaceOrderUseCase() {
    //====== Main: registered user place an order of a none preference box without amending
    // Pre stage: registration
    ShieldingIndividualClientImp placeOrderClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    String placeOrderClientCHI = "0707071217";
    String placeOrderClientPreference = "none";
    boolean registerSuccess = placeOrderClient.registerShieldingIndividual(placeOrderClientCHI);
    assertTrue(registerSuccess, "Fail to register new shielding individual client");
    // show food box
    Collection<String> preferedBoxes = placeOrderClient.showFoodBoxes(placeOrderClientPreference);
    // pick food box: choose any element from the above collection
    Iterator<String> foodBoxIds = preferedBoxes.iterator();
    int foodBoxId = Integer.parseInt(foodBoxIds.next());
    boolean pickBoxSuccess = placeOrderClient.pickFoodBox(foodBoxId);
    
    assertTrue(pickBoxSuccess, "Fail to pick a box for placing order");
    assertNotNull(placeOrderClient.getMarked(), "Fail to stage a box to marked");
    assertEquals(foodBoxId, placeOrderClient.getMarked().id, "Fail to stage the correct box to marked");
  
    //----------OPTIONAL: Amending picked food box-----------
    int itemNum = placeOrderClient.getItemsNumberForFoodBox(foodBoxId);
    Collection<Integer> itemIds = placeOrderClient.getItemIdsForFoodBox(foodBoxId);
    assertEquals(itemNum, itemIds.size(), "Inconsistent in getting the number of item ids");
    int itemIdEdit = itemIds.iterator().next();
    String itemName = placeOrderClient.getItemNameForFoodBox(itemIdEdit, foodBoxId);
    int itemQ = placeOrderClient.getItemQuantityForFoodBox(itemIdEdit, foodBoxId);
    System.out.println(itemQ);
    // add quantity => invalid
    assertFalse(placeOrderClient.changeItemQuantityForPickedFoodBox(itemIdEdit, itemQ+1));
    // reduce quantity => valid
    assertTrue(placeOrderClient.changeItemQuantityForPickedFoodBox(itemIdEdit, itemQ-1));
  
    //----------End Amending / Reading for Place Order-------
    
    // place order
    assertTrue(placeOrderClient.placeOrder(), "Fail to place order");
    assertNull(placeOrderClient.getMarked(), "Fail to clear marked");
    ShieldingIndividualClientImp.Order latest = placeOrderClient.getBoxOrders().get(placeOrderClient.getBoxOrders().size()-1);
    assertEquals(foodBoxId, latest.foodBox.id, "Fail to order the picked box");
    
    // this user should not be able to place another order if order again in the same week
    assertTrue(placeOrderClient.pickFoodBox(foodBoxId),"Should still be able to pick food box");
    assertFalse(placeOrderClient.placeOrder(), "Registered use could only order once a week");
    
    // Alternative 1: this user then cancel this order; then he is valid for placing another order
    
  }
  
  
  @Test
  public void testEditOrderUseCase() {
    // get order number
    
    // set quantity to order
    
    // edit
  }
  
  
  @Test
  public void testCancelOrderUseCase() { // -----
    //1. user and catering company register first
    assertTrue(cancelOrderShieldingIndividualClient1.registerShieldingIndividual(cancelOrderShieldingIndividualCHI1),
            "New registration with valid CHI should be successful");
    assertTrue(cancelOrderShieldingIndividualClient2.registerShieldingIndividual(cancelOrderShieldingIndividualCHI2),
            "New registration with valid CHI should be successful");
    assertTrue(cancelOrderShieldingIndividualClient3.registerShieldingIndividual(cancelOrderShieldingIndividualCHI3),
            "New registration with valid CHI should be successful");
//    TODO: when run the test for the second time, turn this setter on to ensure consistency with the server
//    cancelOrderShieldingIndividualClient1.setShieldingIndividual(cancelOrderShieldingIndividualCHI1,cancelOrderShieldingIndividualClient1.getShieldingIndividualPostcode());
//    cancelOrderShieldingIndividualClient2.setShieldingIndividual(cancelOrderShieldingIndividualCHI2,cancelOrderShieldingIndividualClient2.getShieldingIndividualPostcode());
//    cancelOrderShieldingIndividualClient3.setShieldingIndividual(cancelOrderShieldingIndividualCHI3,cancelOrderShieldingIndividualClient3.getShieldingIndividualPostcode());
    
    //2. user1 pick food box1 & place order1 & cancel order in "placed" status
    assertTrue(cancelOrderCateringClient.registerCateringCompany(cancelOrderCateringName,cancelOrderCateringPostcode));
    //    TODO: when run the test for the second time, turn this setter on to ensure consistency with the server
//    cancelOrderShieldingIndividualClient1.setCateringCompany(cancelOrderCateringName,cancelOrderCateringPostcode);
    assertTrue(cancelOrderShieldingIndividualClient1.pickFoodBox(1),"Registered user should be able to pick a food box order");
    assertTrue(cancelOrderShieldingIndividualClient1.placeOrder(),"Registered user should place order successfully after picking a food box.");
    int orderNum1 = cancelOrderShieldingIndividualClient1.getBoxOrders().get(0).orderId;
    assertTrue(cancelOrderShieldingIndividualClient1.cancelOrder(orderNum1));
    assertTrue(cancelOrderShieldingIndividualClient1.requestOrderStatus(orderNum1));
    assertEquals("cancelled",cancelOrderShieldingIndividualClient1.getStatusForOrder(orderNum1));
    
    //3. place order2 & update order 2 to "packed" & cancel order in "packed" status
    assertTrue(cancelOrderShieldingIndividualClient2.pickFoodBox(1),"Registered user should be able to pick a food box order");
    assertTrue(cancelOrderShieldingIndividualClient2.placeOrder(),"Registered user should place order successfully after picking a food box.");
    int orderNum2 = cancelOrderShieldingIndividualClient2.getBoxOrders().get(0).orderId;
    assertTrue(cancelOrderCateringClient.updateOrderStatus(orderNum2,"packed"),"Company should be able to update placed order to packed status");
    assertTrue(cancelOrderShieldingIndividualClient2.cancelOrder(orderNum2));
    assertTrue(cancelOrderShieldingIndividualClient2.requestOrderStatus(orderNum2));
    assertEquals("cancelled",cancelOrderShieldingIndividualClient2.getStatusForOrder(orderNum2));
    
    //4. place order 3 & update order 3 to "dispatched" & try cancelling order in "dispatched" status
    assertTrue(cancelOrderShieldingIndividualClient3.pickFoodBox(1),"Registered user should be able to pick a food box order");
    assertTrue(cancelOrderShieldingIndividualClient3.placeOrder(),"Registered user should place order successfully after picking a food box.");
    int orderNum3 = cancelOrderShieldingIndividualClient3.getBoxOrders().get(0).orderId;
    assertTrue(cancelOrderCateringClient.updateOrderStatus(orderNum3,"dispatched"),"Company should be able to update placed order to dispatched status");
    assertFalse(cancelOrderShieldingIndividualClient3.cancelOrder(orderNum3), "Dispatched status cannot be cancelled");
    assertTrue(cancelOrderShieldingIndividualClient3.requestOrderStatus(orderNum3));
    assertEquals("dispatched",cancelOrderShieldingIndividualClient3.getStatusForOrder(orderNum3));
    
    //check order 1,2 & try cancelling order in "cancelled" status
    assertFalse(cancelOrderShieldingIndividualClient1.cancelOrder(orderNum1));
    assertFalse(cancelOrderShieldingIndividualClient2.cancelOrder(orderNum2));
  }
  
  
  @Test
  public void testRequestOrderStatusUseCase() {
  
  }
  
  
  @Test
  public void testUpdateOrderStatusUseCase() {
  
  }
}
