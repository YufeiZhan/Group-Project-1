/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClientImp newClient;
  private String newCHI;
  
  
  private ShieldingIndividualClientImp registeredClient;
  private String testCHI;

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
    newCHI = "0101110007";
    
  
    registeredClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    testCHI = "0505150000";
    String userRegistrationRequest = "/registerShieldingIndividual?CHI="+testCHI;
  
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistrationRequest);
      registeredClient.setShieldingIndividual(testCHI);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }


  @Test
  public void testShieldingIndividualNewRegistration() {
    
    //test new client registration
    assertFalse(newClient.isRegistered());
    assertTrue(newClient.registerShieldingIndividual(newCHI));
    assertTrue(newClient.isRegistered());
    assertEquals(newCHI,newClient.getCHI(),"Newly registered user should have identical CHI");

    // test already registered client
    assertTrue(newClient.registerShieldingIndividual(newCHI));
    assertTrue(registeredClient.registerShieldingIndividual(testCHI));
    assertTrue(registeredClient.isRegistered());
    assertEquals(testCHI,registeredClient.getCHI(),"Identical CHI required.");
    
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
    
  }
  
  @Test
  public void testPlaceOrder() {
    // test unregistered user
    
    // test registered user that has placed order
    
    // test registered user that hasn't picked food box
    
    // test registered user that has picked food box and hasn't placed order
    
  }
  
  @Test
  public void testEditOrder() {
    //
  
  }
  
  
  @Test
  public void testCancelOrder() {
    //
  
  }
  
  
  @Test
  public void testRequestOrderStatus() {
  //=
  }
  
  @Test
  public void testGetCateringCompanies() {
  //=
  
  
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
  
    //test invalid user
    assertEquals(-1,newClient.getDistance(postCode1,postCode2),"Should return -1 when unregistered user uses this method." );
  
  }
  
  @Test
  public void testGetFoodBoxNumber() {
    // test valid user
    assertEquals(5,registeredClient.getFoodBoxNumber(),"Should return 5 food boxes");
    
    // test invalid user
    assertEquals(-1,newClient.getFoodBoxNumber(),"Should return -1 when unregistered user uses this method.");
  }
  
  @Test
  public void testGetDietaryPreferenceForFoodBox() {
    // test valid user operations: 1 - none; 2 - pollo; 3,4 - none; 5 -vegan;
    assertEquals("none",registeredClient.getDietaryPreferenceForFoodBox(1),"Should return none");
    assertEquals("pollotarian",registeredClient.getDietaryPreferenceForFoodBox(2),"Should return pollotarian");
    assertEquals("none",registeredClient.getDietaryPreferenceForFoodBox(3),"Should return none");
    assertEquals("none",registeredClient.getDietaryPreferenceForFoodBox(4),"Should return none");
    assertEquals("vegan",registeredClient.getDietaryPreferenceForFoodBox(5),"Should return vegan");
    
    // test invalid user
    assertNull(newClient.getDietaryPreferenceForFoodBox(5),"Unregistered new shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetItemsNumberForFoodBox() {
    // 1 - 3; 2 - 3; 3 - 3; 4 - 4; 5 - 3;
//    assertEquals(3, registeredClient);
  
  }
  
  @Test
  public void testGetItemIdsForFoodBox() {
  
  }
  
  @Test
  public void testGetItemNameForFoodBox() {
  
  }
  
  @Test
  public void testGetItemQuantityForFoodBox() {
  
  }
  
  @Test
  public void testPickFoodBox() {
  //--
  }
  
  @Test
  public void testChangeItemQuantityForPickedFoodBox() {
  //+++++
  }
  
  
  @Test
  public void testGetOrderNumbers() {
  
  }
  
  @Test
  public void testGetStatusForOrder() {
  
  }
  
  @Test
  public void testGetItemIdsForOrder() {
  
  }
  
  @Test
  public void testGetItemNameForOrder() {
  }
  
  
  @Test
  public void testGetItemQuantityForOrder() {
  
  }
  
  @Test
  public void testSetItemQuantityForOrder() {
  
  }
  
  @Test
  public void testGetClosestCateringCompany() {
  
  }
  
  
  
}
