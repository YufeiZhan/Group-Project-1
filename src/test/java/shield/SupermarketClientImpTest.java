/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class SupermarketClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  
  private SupermarketClientImp nonRegisteredClient;
  
  private SupermarketClientImp registeredClient;
  private String registeredCHI;
  private int registeredOrderNum;
  
  private SupermarketClientImp placedOrderClient;
  private String placedCHI;
  private int placedOrderNum;
  

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
    
    // Client to test registration
    nonRegisteredClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));

    // Client to test registration and place order
    registeredClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    registeredCHI = generateValidCHI();
    registeredOrderNum = generateValidRandomInteger();
    String registerRequest = "/registerSupermarket?business_name="+generateValidSupermarketName()+"&postcode=" + generateValidRandomPostCode();
    String userRegisterRequest2 = "/registerShieldingIndividual?CHI="+ registeredCHI;
    
    //Client to test place order
    placedOrderClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    String placedSupermarketName = generateValidSupermarketName();
    String placedSupermarketPostcode = generateValidRandomPostCode();
    placedCHI = generateValidCHI();
    placedOrderNum = generateValidRandomInteger();
    String registerRequest2 = "/registerSupermarket?business_name="+placedSupermarketName+"&postcode=" + placedSupermarketPostcode;
    String userRegisterRequest = "/registerShieldingIndividual?CHI="+ placedCHI;
    String recordOrderRequest = "/recordSupermarketOrder?individual_id="+ placedCHI +"&order_number="+ placedOrderNum +"&supermarket_business_name="+placedSupermarketName+"&supermarket_postcode="+placedSupermarketPostcode;
    
    
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + registerRequest);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + registerRequest2);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegisterRequest);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegisterRequest2);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + recordOrderRequest);
      placedOrderClient.setOrder(placedCHI,placedOrderNum);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @Test
//  @DisplayName()
  public void testConstructorInitialization() {
    assertFalse(nonRegisteredClient.isRegistered(), "Supermarket shouldn't be registered before registration");
    assertNull(nonRegisteredClient.getName(),"Supermarket shouldn't have a name before registration");
    assertNull(nonRegisteredClient.getPostCode(),"Supermarket shouldn't have a postcode before registration");
  }


  @Test
  public void testSupermarketNewRegistration() {
    String newName = generateValidSupermarketName();
    String newPostCode = generateValidRandomPostCode();

    //Test new registration
    assertFalse(nonRegisteredClient.isRegistered(),"Should not be registered before registration.");
    assertTrue(nonRegisteredClient.registerSupermarket(newName,newPostCode),"Supermarket should be successfully registered");
    assertTrue(nonRegisteredClient.isRegistered(),"Supermarket should be registered.");
    assertEquals(newName, nonRegisteredClient.getName(), "Registered name should match.");
    assertEquals(newPostCode,nonRegisteredClient.getPostCode(),  "Registered postcode should match.");

    //Test already registered
    assertTrue(nonRegisteredClient.registerSupermarket(newName,newPostCode),"Supermarket should be already registered and return true");
    //TODO: change this function
    assertTrue(registeredClient.registerSupermarket(registeredCHI, registeredClient.getPostCode()),
            "supermarket should be already registered and return true.");
    
    //++Test invalid input
  }
  
  @Test
  public void testRecordSupermarketOrder() {
    //Test precondition on placing order
    
    //Test make new order
    assertFalse(registeredClient.orderExist(registeredOrderNum),"Unplaced order should not exist in the order list.");
    assertTrue(registeredClient.recordSupermarketOrder(registeredCHI, registeredOrderNum),"First valid record supermarket order should return True.");
    assertTrue(registeredClient.orderExist(registeredOrderNum), "Placed order should exist in the order list.");

    //Test already placed order
    assertFalse(registeredClient.recordSupermarketOrder(registeredCHI, registeredOrderNum),"Repeated record should return False.");
    assertFalse(placedOrderClient.recordSupermarketOrder(placedCHI, placedOrderNum),"Repeated record should return False.");
    assertTrue(placedOrderClient.orderExist(placedOrderNum));

    //++Test order failure
    
    
    //++Test invalid input
  }
  
  @Test
  public void testOrderStatusUpdate() {
    
    //Test newly placed order status
    assertEquals(0,placedOrderClient.getOrderStatus(placedOrderNum),"New order should in placed status (0)");
    assertFalse(placedOrderClient.updateOrderStatus(placedOrderNum,"placed"),"Status update can never be with placed status (0)");
    
    
//    Test order status update in order
    assertTrue(placedOrderClient.updateOrderStatus(placedOrderNum,"packed"),"Successful update from 0 to 1");
    assertEquals(1,placedOrderClient.getOrderStatus(placedOrderNum),"Updated order should in packed status (1)");
    assertFalse(placedOrderClient.updateOrderStatus(placedOrderNum,"packed"),"Status update can't stay the same (1)");
    assertTrue(placedOrderClient.updateOrderStatus(placedOrderNum,"dispatched"),"Successful update from 1 to 2");
    assertEquals(2,placedOrderClient.getOrderStatus(placedOrderNum),"Updated order should in dispatched status (2)");
    assertFalse(placedOrderClient.updateOrderStatus(placedOrderNum,"packed"),"Status update can't return back from 2 to 1");
    assertFalse(placedOrderClient.updateOrderStatus(placedOrderNum,"dispatched"),"Status update can't stay the same (2)");
    assertTrue(placedOrderClient.updateOrderStatus(placedOrderNum,"delivered"),"Successful update from 2 to 3");
    assertEquals(3,placedOrderClient.getOrderStatus(placedOrderNum),"Updated order should in delivered status (3)");
    assertFalse(placedOrderClient.updateOrderStatus(placedOrderNum,"dispatched"),"Status update can't return back from 3 to 2");
    assertFalse(placedOrderClient.updateOrderStatus(placedOrderNum,"delivered"),"Status update can't stay the same (3)");
    
    //++Test invalid input + precondition
  }
  
  
  
    // --------------------------------------- Testing Helper Method ---------------------------------------
  
  private String generateValidSupermarketName(){
    Random rand = new Random();
    String result = "supermarket"+rand.nextInt(10000);
    
    return result;
  }

  private String generateValidRandomPostCode(){
    int uppercaseBase = (int) 'A'; // index for uppercase A
    Random rand = new Random();
    int firstRandomNum = rand.nextInt(26);
    int secondRandomNum = rand.nextInt(26);

    char firstChar = (char) (uppercaseBase + firstRandomNum);
    char secondChar = (char) (uppercaseBase + secondRandomNum);

    String postCode = "EH" + String.valueOf(rand.nextInt(17) + 1) + '_' +
            String.valueOf(rand.nextInt(9) + 1) + firstChar + secondChar;

    return postCode;
  }
  
  private int generateValidRandomInteger(){
    Random rand = new Random();
    return rand.nextInt(10000);
  }
  
  private String generateValidCHI(){
    int uppercaseBase = (int) 'A'; // index for uppercase A
    Random rand = new Random();
    int day = rand.nextInt(30)+1;
    int month = rand.nextInt(12)+1;
    int year = rand.nextInt(90)+10; //only returns 10-99 for simplicity
    String result = "" + day + month + year;
    
    for (int i=0; i<=4; i++){
      int randomInt = rand.nextInt(26);
      char randomChar = (char) (uppercaseBase + randomInt);
      result = result + randomChar;
    }
    
    return result;
  }
}
