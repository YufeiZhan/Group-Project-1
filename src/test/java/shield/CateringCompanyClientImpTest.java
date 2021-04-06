/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
  private CateringCompanyClient validClient;
  private CateringCompanyClient invalidClient;
  

  
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
    validClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    invalidClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
  }
  
  // ----- Registration Testing -----
  @Test
  @DisplayName("Testing valid input for new catering company registration")
  public void testValidCateringCompanyNewRegistration() {
    String name = generateValidRandomName();
    String postCode = generateValidRandomPostCode();
    
    // +++ Internal server error: change the first assertion once server fixed
    assertTrue(validClient.registerCateringCompany(name, postCode));
    assertTrue(validClient.isRegistered());
    assertEquals(validClient.getName(), name);
    assertEquals(validClient.getPostCode(), postCode);
  }
  
  @Test
  @DisplayName("Testing null name field for catering company registration")
  public void testNullNameCateringCompanyNewRegistration() {
    String postCode = generateValidRandomPostCode();
  
    assertFalse(invalidClient.registerCateringCompany(null,postCode),
            "Catering Company shouldn't been registered with name field being null");

  }
  
  @Test
  @DisplayName("Testing null postcode field for catering company registration")
  public void testNullPostCodeCateringCompanyNewRegistration() {
    String name = generateValidRandomName();;
    
    assertFalse(validClient.registerCateringCompany(name,null),
            "Catering Company shouldn't been registered with postcode field being null");
  }
  
  @Test
  @DisplayName("Testing invalid postcode input for catering company registration")
  public void testInvalidPostCodeCateringCompanyNewRegistration() {
    String name = generateValidRandomName();
    String postCode = "EB16 7AY"; // incorrect "EB" starting
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode should start with 'EH'");
  
    postCode = "eh1 7AY"; // incorrect lower-cased "EH"
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode only upper-cased");
  
    postCode = "EH0 7AY"; // incorrect num after "EH"
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode: [1-17] after 'EH'");
    
    postCode = "EHA 7AY"; // incorrect char after "EH"
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode: [1-17] after 'EH'");
  
    postCode = "EH23 7AY"; // incorrect num after "EH"
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode: [1-17] after 'EH'");
  
    postCode = "EH177AY"; // incorrect postcode without space
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode: a space required in postcode");
  
    postCode = "EH17 0AY"; // incorrect num after space
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode: [1-9] after space");
  
    postCode = "EH17 5Ay"; // incorrect num after space
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode only upper-cased");
  
    postCode = "EH17 5aY"; // incorrect lowercase postcode
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode only upper-cased");
  
    postCode = "EH17 5A2"; // incorrect postcode ending with num
    assertFalse(invalidClient.registerCateringCompany(name,postCode),"Postcode ending with upper-cased chars");
  }
  
  @Test
  @DisplayName("Testing repeated registration for old catering company")
  public void testRepeatedCateringCompanyRegistration() {
    String validClientName = validClient.getName();
    String validClientCode = validClient.getPostCode();
  
    assertTrue(validClient.isRegistered());
    assertTrue(validClient.registerCateringCompany(validClientName,validClientCode),
            "Catering Company should have already been registered");
  }
  
  // ----- Order Updates Testing -----
  
  
  // ----- Testing Helper Method -----
  private String generateValidRandomName(){
    Random rand = new Random();
    return String.valueOf(rand.nextInt(10000));
  }
  
  private String generateValidRandomPostCode(){
    int uppercaseBase = (int) 'A'; // index for uppercase A
    Random rand = new Random();
    int firstRandomNum = rand.nextInt(26);
    int secondRandomNum = rand.nextInt(26);
    
    char firstChar = (char) (uppercaseBase + firstRandomNum);
    char secondChar = (char) (uppercaseBase + secondRandomNum);
    
    String postCode = "EH" + String.valueOf(rand.nextInt(17) + 1) + ' ' +
            String.valueOf(rand.nextInt(9) + 1) + firstChar + secondChar;
            
    return postCode;
  }
  
}
