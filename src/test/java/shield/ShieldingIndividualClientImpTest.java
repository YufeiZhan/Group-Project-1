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
  private ShieldingIndividualClientImp newClient;
  private String newCHI;
  
  
  private ShieldingIndividualClientImp registeredClient;
  private String testCHI;
  
  private ShieldingIndividualClientImp registeredClient2; //add
  private String testCHI2; //add

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
    newCHI = "0101110008";
    
  
    registeredClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    testCHI = "0505150000";
    String userRegistrationRequest = "/registerShieldingIndividual?CHI="+testCHI;
  
    registeredClient2 = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));  //add
    testCHI2 = "0505150110"; //add
    String userRegistration2Request = "/registerShieldingIndividual?CHI="+testCHI2; //add
  
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistrationRequest);
      registeredClient.setShieldingIndividual(testCHI);
  
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + userRegistration2Request); //add
      registeredClient2.setShieldingIndividual(testCHI2); //add
      
      //-------------setOrders----------------
      String box1 = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
                    "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}],\"delivered_by\":\"catering\"," +
                    "\"diet\":\"none\",\"id\":1,\"name\":\"box a\"}";
      Type listType = new TypeToken<ShieldingIndividualClientImp.MessagingFoodBox>() {} .getType();
      ShieldingIndividualClientImp.MessagingFoodBox b1 = new Gson().fromJson(box1, listType);
      
      ShieldingIndividualClientImp.Order newOrder1 = new ShieldingIndividualClientImp.Order();
      newOrder1.status = 3;
      newOrder1.orderId = 1;
      newOrder1.foodBox = b1;
      newOrder1.placeTime = LocalDateTime.of(2021,4,12,17,45,39);
      ShieldingIndividualClientImp.Order newOrder2 = new ShieldingIndividualClientImp.Order();
      newOrder2.status = 0;
      newOrder2.orderId = 2;
      newOrder2.foodBox = b1;
      newOrder2.placeTime = LocalDateTime.of(2021,4,20,17,45,39);
      registeredClient.setLatest(newOrder2);
      List<ShieldingIndividualClientImp.Order> orders = new ArrayList<ShieldingIndividualClientImp.Order>();
      orders.add(newOrder1);
      orders.add(newOrder2);
      registeredClient.setBoxOrders(orders);
      //-------------setOrders----------------
      
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
    // haven't picked any box
    assertFalse(registeredClient.changeItemQuantityForPickedFoodBox(2,1),"Should pick food box first");
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
    //invalid user
    assertFalse(newClient.changeItemQuantityForPickedFoodBox(2,1),"Should register first");
  }
  
  //TODO: for following methods, try to tag them and add some orders in boxOrder list before each
  @Test
  public void testGetOrderNumbers() {
    // no order history
    assertNull(registeredClient2.getOrderNumbers(),"No order history");
    // check if returned correctly
    Collection<Integer> ids = new ArrayList<Integer>(Arrays.asList(1,2));
    assertEquals(ids, registeredClient.getOrderNumbers(),"Incorrect number of orders");
    // invalid user
    assertNull(newClient.getOrderNumbers(),"New client shouldn't have ordered");
  }
  
  @Test
  public void testGetStatusForOrder() {
    // no order history
    assertNull(registeredClient2.getStatusForOrder(1),"Haven't ordered yet");
    assertNull(registeredClient2.getStatusForOrder(2),"Haven't ordered yet");
    // un-found order number
    assertNull(registeredClient.getStatusForOrder(3),"No such order");
    assertNull(registeredClient.getStatusForOrder(4),"No such order");
    // check if returned correctly
    assertEquals("delivered", registeredClient.getStatusForOrder(1));
    assertEquals("placed", registeredClient.getStatusForOrder(2));
    // invalid user
    assertNull(newClient.getStatusForOrder(1),"New client shouldn't have ordered");
    assertNull(newClient.getStatusForOrder(2),"New client shouldn't have ordered");
  }
  
  @Test
  public void testGetItemIdsForOrder() {
    // no order history
    assertNull(registeredClient2.getItemIdsForOrder(1),"Haven't ordered yet");
    assertNull(registeredClient2.getItemIdsForOrder(2),"Haven't ordered yet");
    // un-found order number
    assertNull(registeredClient.getItemIdsForOrder(3),"No such order");
    assertNull(registeredClient.getItemIdsForOrder(4),"No such order");
    // check if returned correctly
    Collection<Integer> ids = new ArrayList<Integer>(Arrays.asList(1,2,6));
    assertEquals(ids, registeredClient.getItemIdsForOrder(1));
    assertEquals(ids, registeredClient.getItemIdsForOrder(2));
    // invalid user
    assertNull(newClient.getItemIdsForOrder(1),"New client shouldn't have ordered");
    assertNull(newClient.getItemIdsForOrder(2),"New client shouldn't have ordered");
  }
  
  @Test
  public void testGetItemNameForOrder() {
    // no order history
    assertNull(registeredClient2.getItemNameForOrder(1,1),"Haven't ordered yet");
    assertNull(registeredClient2.getItemNameForOrder(2,1),"Haven't ordered yet");
    // un-found order number
    assertNull(registeredClient.getItemNameForOrder(1,3),"No such order");
    // un-found order item
    assertNull(registeredClient.getItemNameForOrder(4,1),"No such item");
    assertNull(registeredClient.getItemNameForOrder(7,2),"No such item");
    // success scenario
    assertEquals("cucumbers", registeredClient.getItemNameForOrder(1,1),"Incorrect item");
    assertEquals("tomatoes", registeredClient.getItemNameForOrder(2,2),"Incorrect item");
    assertEquals("pork", registeredClient.getItemNameForOrder(6,1),"Incorrect item");
    // invalid user
    assertNull(newClient.getItemNameForOrder(1,1),"New client shouldn't have ordered");
    assertNull(newClient.getItemNameForOrder(2,1),"New client shouldn't have ordered");
  }
  
  
  @Test
  public void testGetItemQuantityForOrder() {
    // no order history
    assertEquals(-1,registeredClient2.getItemQuantityForOrder(1,1),"Haven't ordered yet");
    assertEquals(-1,registeredClient2.getItemQuantityForOrder(2,1),"Haven't ordered yet");
    // un-found order number
    assertEquals(-1,registeredClient.getItemQuantityForOrder(1,3),"No such order");
    // un-found order item
    assertEquals(-1,registeredClient.getItemQuantityForOrder(4,1),"No such item");
    assertEquals(-1,registeredClient.getItemQuantityForOrder(7,2),"No such item");
    // success scenario
    assertEquals(1, registeredClient.getItemQuantityForOrder(1,1),"Incorrect quantity");
    assertEquals(2, registeredClient.getItemQuantityForOrder(2,2),"Incorrect quantity");
    assertEquals(1, registeredClient.getItemQuantityForOrder(6,1),"Incorrect quantity");
    // invalid user
    assertEquals(-1,newClient.getItemQuantityForOrder(1,1),"New client shouldn't have ordered");
    assertEquals(-1,newClient.getItemQuantityForOrder(2,1),"New client shouldn't have ordered");
  }
  
  @Test
  public void testSetItemQuantityForOrder() {
    // no order history
    assertFalse(registeredClient2.setItemQuantityForOrder(1,2,0),"Haven't ordered yet");
    assertFalse(registeredClient2.setItemQuantityForOrder(2,2,0),"Haven't ordered yet");
    // un-found order number
    assertFalse(registeredClient.setItemQuantityForOrder(1,3,0),"No such order");
    // un-found order item
    assertFalse(registeredClient.setItemQuantityForOrder(4,2,0),"No such item");
    assertFalse(registeredClient.setItemQuantityForOrder(7,2,0),"No such item");
    // invalid quantity
    assertFalse(registeredClient.setItemQuantityForOrder(1,2,4),"Quantity can only be reduced");
    assertFalse(registeredClient.setItemQuantityForOrder(6,2,4),"Quantity can only be reduced");
    // order that could not be successfully set (invalid for editing)
    assertFalse(registeredClient.setItemQuantityForOrder(1,1,0),"Order couldn't be edited");
    assertFalse(registeredClient.setItemQuantityForOrder(6,1,0),"Order couldn't be edited");
    // success scenario
    assertTrue(registeredClient.setItemQuantityForOrder(1,2,0),"Incorrect item");
    assertTrue(registeredClient.setItemQuantityForOrder(2,2,1),"Incorrect item");
    assertTrue(registeredClient.setItemQuantityForOrder(6,2,0),"Incorrect item");
    // the updates to latest
    // invalid user
    assertFalse(registeredClient.setItemQuantityForOrder(1,2,0),"New client shouldn't have ordered");
    assertFalse(registeredClient.setItemQuantityForOrder(2,2,1),"New client shouldn't have ordered");
  }
  
  @Test
  public void testGetClosestCateringCompany() {
  }
  
  
  
}
