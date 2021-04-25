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
    assertTrue(validCateringRegistrationClient.isRegistered(),"Registered company should be registered.");
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
  public void testCancelOrderUseCase() {
  
  }
  
  
  @Test
  public void testRequestOrderStatusUseCase() {
  
  }
  
  
  @Test
  public void testUpdateOrderStatusUseCase() {
  
  }
}
