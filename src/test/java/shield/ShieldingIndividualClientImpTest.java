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
  private String registeredCHI;
  private String registeredValidOrderNum;
  private int registeredInvalidOrderNum;
  
  
//  private ShieldingIndividualClientImp placedOrderClient;
//  private String placedOrderCHI;

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
    registeredCHI = "0505150000";
    String userRegistrationRequest = "/registerShieldingIndividual?CHI="+registeredCHI;
//    String registeredCateringCompanyName = "tempCateringCompanyForTestInShieldingClient";
//    String registeredCCPostCode = "EH16_5AY";
//    String cateringCompanyRegistrationRequest = "registerCateringCompany?business_name="+registeredCateringCompanyName+"&postcode="+registeredCCPostCode;
//    registeredInvalidOrderNum = 10000;
//    String placedOrderRequest = "/placeOrder?individual_id="+registeredCHI+"&catering_business_name="+registeredCateringCompanyName+"&catering_postcode="+registeredCCPostCode;

    
  
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistrationRequest);
//      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + cateringCompanyRegistrationRequest);
//      registeredValidOrderNum = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + placedOrderRequest);
      registeredClient.setShieldingIndividual(registeredCHI);
//      registeredClient.setStagedFoodBox();
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
  public void testRequestOrderStatus() { //required placed order to check order status
    //test registered user with invalid order number
    
    //test registered user with valid order number
    
    //test unregistered user
    assertFalse(newClient.requestOrderStatus(100),"Unregistered new shouldn't be able to use this method.");
  }
  
  @Test
  public void testGetCateringCompanies() {
    //test registered user
    assertTrue(registeredClient.getCateringCompanies().size() >=0, "Number of catering companies should be bigger than 0");
    
    //test unregistered user
    assertNull(newClient.getCateringCompanies(),"Unregistered new shouldn't be able to use this method.");
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
    assertNull(newClient.getDietaryPreferenceForFoodBox(5),"Unregistered new shouldn't be able to use this method.");
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
    assertEquals(-1, newClient.getItemsNumberForFoodBox(5),"Unregistered new shouldn't be able to use this method.");
  
  
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
    assertNull(newClient.getItemIdsForFoodBox(2),"Unregistered new shouldn't be able to use this method.");
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
    assertNull(newClient.getItemNameForFoodBox(1,6),"Unregistered new shouldn't be able to use this method.");
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
    assertEquals(-1,newClient.getItemQuantityForFoodBox(1,6),"Unregistered new shouldn't be able to use this method.");
  }
  
  @Test
  public void testPickFoodBox() {
    //test registered user with invalid food box
    assertFalse(registeredClient.pickFoodBox(10),"Invalid food box should be unsuccessful.");
    
    //test registered user with valid food box
    for (int i=1; i<=5; i++) {
      assertTrue(registeredClient.pickFoodBox(i), "Valid food box should be placed successfully.");
    }
    
    //test unregistered user with valid food box
    for (int i=1; i<=5; i++){
      assertFalse(newClient.pickFoodBox(i),"Unregistered new shouldn't be able to use this method.");
    }
    
  
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
