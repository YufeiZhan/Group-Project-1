package shield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
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
  
  // Supermarket Registeration Use Case
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
    validCateringRegistrationClient.setCateringCompany(validCateringCompanyName,validCateringCompanyPostCode); // to make sure it's set for test purpose
    assertTrue(validCateringRegistrationClient.isRegistered(),"Registered company should be registered.");
    assertEquals(validCateringCompanyName,validCateringRegistrationClient.getName(),"Registered company should have a matching name.");
    assertEquals(validCateringCompanyPostCode,validCateringRegistrationClient.getPostCode(),"Registered company should have a matching postcode.");
  }
  
  
  @Test
  public void testRegisterShieldingIndividualUseCase() {
  
  }
  
  @Test
  public void testPlaceOrderUseCase() {
  
  }
  
  
  @Test
  public void testEditOrderUseCase() { // +++++
  
  }
  
  
  @Test
  public void testCancelOrderUseCase() { // -----
  
  }
  
  
  @Test
  public void testRequestOrderStatusUseCase() {
  
  }
  
  
  @Test
  public void testUpdateOrderStatusUseCase() {
  
  }
}
