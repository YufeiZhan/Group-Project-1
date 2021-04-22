/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

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
  private CateringCompanyClientImp orderedClient;



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
    
    registeredClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    registeredName = "tempCateringCompany";
    registeredPostCode = "EH17_9ZZ";
    String registrationRequest = "/registerCateringCompany?business_name="+registeredName+"&postcode="+registeredPostCode;
    
//    orderedClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
//    String orderedName = "tempCateringCompany2";
//    String orderedPostCode = "EH1_1AA";
//    String registrationRequest2 = "/registerCateringCompany?business_name="+orderedName+"&postcode="+orderedPostCode;
//    String orderRequest = 'updateOrderStatus?order_id=42&newStatus=packed'
    
  
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + registrationRequest);
      registeredClient.setCateringCompany(registeredName,registeredPostCode);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // ---------------------------------------- Registration Testing ----------------------------------------
  @Test
  @DisplayName("Testing input for new catering company registration")
  public void testCateringCompanyNewRegistration() {
    String name = generateValidRandomName();

//    //testing invalid parameters
//    assertFalse(newClient.registerCateringCompany(null,postCode),
//            "Catering Company shouldn't been registered with name field being null");
//    assertFalse(newClient.registerCateringCompany(name,null),
//            "Catering Company shouldn't been registered with name field being null");
    assertFalse(newClient.isRegistered());
    
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
    
    assertTrue(registeredClient.registerCateringCompany(registeredName,registeredPostCode),
            "Catering Company should have already been registered and return true");
    assertTrue(registeredClient.isRegistered());
    assertEquals(registeredClient.getName(), registeredName);
    assertEquals(registeredClient.getPostCode(), registeredPostCode);
  }
  
  
//  @Test
//  @DisplayName("Testing valid marginal postcode input for catering company registration")
//  public void testValidPostCodeCateringCompanyNewRegistration() {
//    String name = generateValidRandomName();
//
//    String postCode = "EH1_5GG";
//    assertTrue(newClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//    postCode = "EH17_5GG";
//    assertTrue(invalidClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//    postCode = "EH15_1GG";
//    assertTrue(invalidClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//    postCode = "EH15_9GG";
//    assertTrue(invalidClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//    postCode = "EH15_5AG";
//    assertTrue(invalidClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//    postCode = "EH15_5AA";
//    assertTrue(invalidClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//    postCode = "EH15_5ZG";
//    assertTrue(invalidClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//    postCode = "EH15_5ZZ";
//    assertTrue(invalidClient.registerCateringCompany(name,postCode),"Valid Marginal Postcode");
//  }
  

  // ----------------------------------- Order Updates Testing -----------------------------------


  // ----------------------------------- Testing Helper Method -----------------------------------
  private String generateValidRandomName(){
    Random rand = new Random();
    return ("testCateringCompany"+rand.nextInt(10000));
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
