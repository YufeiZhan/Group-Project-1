/**
 *
 */

package shield;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class CateringCompanyClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  
  private CateringCompanyClientImp newClient;
  
  private CateringCompanyClientImp registeredClient;
  private String registeredName;
  private String registeredPostCode;
  
  //Order Update
  private CateringCompanyClientImp orderedClient;
  private int orderNumber;



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
    
    newClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    
    //registration client
    registeredClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    registeredName = "tempCateringCompany";
    registeredPostCode = "EH17_9ZZ";
    String registrationRequest = "/registerCateringCompany?business_name="+registeredName+"&postcode="+registeredPostCode;
    
    orderedClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    String orderedCHI = "0909990000";
    String userRegistrationRequest =  "/registerShieldingIndividual?CHI=" + orderedCHI;
    String orderedName = "tempCateringCompanyForCCClientOrderUpdate";
    String orderedPostCode = "EH1_1AA";
    String registrationRequest2 = "/registerCateringCompany?business_name="+orderedName+"&postcode="+orderedPostCode;
    String placeOrderData = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
            "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}],\"delivered_by\":\"catering\"," +
            "\"diet\":\"none\",\"id\":1,\"name\":\"box a\"}";
    String placeOrderRequest = clientProps.getProperty("endpoint") + "/placeOrder?individual_id="+ orderedCHI +
            "&catering_business_name=" + orderedName + "&catering_postcode=" + orderedPostCode;
    
  
    try {
      // ---- Catering Company Registration ----
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + registrationRequest);
      registeredClient.setCateringCompany(registeredName,registeredPostCode);
  
      // ---- Catering Company Order Update ----
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistrationRequest);
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + registrationRequest2);
      orderNumber = Integer.parseInt(ClientIO.doPOSTRequest(placeOrderRequest,placeOrderData));
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // ---------------------------------------- Registration Testing ----------------------------------------
  @Test
  @DisplayName("Testing input for new catering company registration")
  public void testCateringCompanyNewRegistration() {
    String name = generateValidRandomName();
    
    //testing unregistered client field
    assertFalse(newClient.isRegistered());
    assertNull(newClient.getName());
    assertNull(newClient.getPostCode());
    
    //testing invalid postcode
    String postCode = "EB16_7AY"; // incorrect "EB" starting
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode should start with 'EH'");
    postCode = "eh1_7AY"; // incorrect lower-cased "EH"
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode only upper-cased");
    postCode = "EH0_7AY"; // incorrect num after "EH"
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode: [1-17] after 'EH'");
    postCode = "EHA_7AY"; // incorrect char after "EH"
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode: [1-17] after 'EH'");
    postCode = "EH18_7AY"; // incorrect num after "EH"
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode: [1-17] after 'EH'");
    postCode = "EH177AY"; // incorrect postcode without underline
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode: a space required in postcode");
    postCode = "EH17_0AY"; // incorrect num after space
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode: [1-9] after space");
    postCode = "EH17_5Ay"; // incorrect num after space
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode only upper-cased");
    postCode = "EH17_5aY"; // incorrect lowercase postcode
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode only upper-cased");
    postCode = "EH17_5A2"; // incorrect postcode ending with num
    assertFalse(newClient.registerCateringCompany(name,postCode),"Postcode ending with upper-cased chars");
    
    //test valid marginal postcode input for registration
    assertTrue(registeredClient.registerCateringCompany(registeredName,registeredPostCode),
            "Catering Company should have already been registered and return true");
    assertTrue(registeredClient.isRegistered());
    assertEquals(registeredClient.getName(), registeredName);
    assertEquals(registeredClient.getPostCode(), registeredPostCode);
  }
  
  

  // ----------------------------------- Order Updates Testing -----------------------------------
  @Test
  @DisplayName("Testing order status update from catering company side")
  public void testUpdateOrderStatus(){
    //test registered user with invalid status
    assertFalse(orderedClient.updateOrderStatus(orderNumber,"placed"),"Status cannot be set to placed");
    assertFalse(orderedClient.updateOrderStatus(orderNumber,"cancelled"),"Status cannot be set to cancelled.");
    
    //test registered user with valid status
    assertTrue(orderedClient.updateOrderStatus(orderNumber,"packed"));
    assertTrue(orderedClient.updateOrderStatus(orderNumber,"dispatched"));
    assertTrue(orderedClient.updateOrderStatus(orderNumber,"delivered"));
    
    //test unregistered user
    assertFalse(newClient.updateOrderStatus(1,"packed"),"Unregistered user shouldn't be able to use this method.");
  }

  // ----------------------------------- Testing Helper Method -----------------------------------
  private String generateValidRandomName(){
    Random rand = new Random();
    return ("testCateringCompanyClient"+rand.nextInt(10000));
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

}
