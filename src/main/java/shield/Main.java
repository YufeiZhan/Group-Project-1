/*
 * main program
 *
 */

package shield;

import java.time.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Main {
  public static void main(String[] args) {
 
    //之前register过的 重新new instance之后，就没办法retrieve info
    //所以isregisered 却没有办法通过server reset user的info
  
    ShieldingIndividualClientImp test = new ShieldingIndividualClientImp("http://0.0.0.0:5000/");
   // test.registerShieldingIndividual("1212120163");
   // assert test.isRegistered();
    //LocalDateTime t = LocalDateTime.of(2021,4,12,12,1,0);
   //if (test.isInSameWeek(t)) System.out.println(true);
    
    //System.out.println(test.isInSameWeek(t));
    /*
    try {
      String content1 = "{\"contents\":[{\"id\":1,\"name\":\"cucumbers\",\"quantity\":1},{\"id\":2,\"name\":\"tomatoes\"," +
              "\"quantity\":2},{\"id\":6,\"name\":\"pork\",\"quantity\":1}]}";
      String x = "0504130000";
      String z = "tempCateringCompanyForTestInShieldingClient";
      String w = "EH16_5AY";
      String o2 = "/placeOrder?individual_id="+ x +
              "&catering_business_name=" + z +
              "&catering_postcode=" + w;
      //-------------update server for order 1----------------
      String response1 = ClientIO.doPOSTRequest("http://0.0.0.0:5000/"+o2, content1);
      int orderId = Integer.parseInt(response1); //TODO: save orderId as attributes of this class for future reference
      
      String status1 = "/updateOrderStatus?order_id="+orderId+"&newStatus=dispatched";
      String statusRes1 = ClientIO.doGETRequest("http://0.0.0.0:5000/" + status1);
      assert statusRes1.equals("True");
      String suc = ClientIO.doGETRequest("http://0.0.0.0:5000/" +"/cancelOrder?order_id="+orderId);
      System.out.println(suc);
    } catch (Exception e) {
      System.out.println("error");
    }
    */
     */
    /*
    boolean success = test.pickFoodBox(2);
    System.out.println(success);
    boolean check = test.placeOrder();
    System.out.println(check);
    */
    
    /*
    //----------------placeOrder() fail if no food box is staged----------------
    //----------------changeItemQuantityForPickedFoodBox() failed same----------------
    boolean s = test.placeOrder();
    boolean c = test.changeItemQuantityForPickedFoodBox(1,1);
    assert !s;
    assert !c;
  
    //----------------------------showFoodBoxes()-------------------------------
    //--------------------getDietaryPreferenceForFoodBox()----------------------
    String tp;
    Collection<String> boxes = test.showFoodBoxes("none");
    System.out.println("Preference: none");
    for (String id: boxes) {
      tp = test.getDietaryPreferenceForFoodBox(Integer.parseInt(id));
      tp = tp.toLowerCase();
      assert tp.equals("none");
    }
  
    boxes = test.showFoodBoxes("pollotarian");
    System.out.println("Preference: pollotarian");
    for (String id: boxes) {
      tp = test.getDietaryPreferenceForFoodBox(Integer.parseInt(id));
      tp = tp.toLowerCase();
      assert tp.equals("pollotarian");
    }
  
    boxes = test.showFoodBoxes("vegan");
    System.out.println("Preference: vegan");
    for (String id: boxes) {
      tp = test.getDietaryPreferenceForFoodBox(Integer.parseInt(id));
      tp = tp.toLowerCase();
      assert tp.equals("vegan");
    }
  
    //--------------------------getFoodBoxNumber()---------------------------
    int num = test.getFoodBoxNumber();
    assert num == 5;
  
    //--------------------------getItemsNumberForFoodBox()---------------------------
  
    int itemNum1 = test.getItemsNumberForFoodBox(1);
    assert itemNum1 == 3;
    int itemNum2 = test.getItemsNumberForFoodBox(2);
    assert itemNum2 == 3;
    int itemNum3 = test.getItemsNumberForFoodBox(3);
    assert itemNum3 == 3;
    int itemNum4 = test.getItemsNumberForFoodBox(4);
    assert itemNum4 == 4;
    int itemNum5 = test.getItemsNumberForFoodBox(5);
    assert itemNum5 == 3;
  
    //--------------------------getItemIdsForFoodBox()---------------------------
    //--------------------------getItemNameForFoodBox()---------------------------
    //--------------------------getItemQuantityForFoodBox()---------------------------
    Collection<Integer> itemIds1 = test.getItemIdsForFoodBox(1);
    assert itemIds1.size() == itemNum1;
    Collection<Integer> itemIds2 = test.getItemIdsForFoodBox(2);
    assert itemIds2.size() == itemNum2;
    Collection<Integer> itemIds3 = test.getItemIdsForFoodBox(3);
    assert itemIds3.size() == itemNum3;
    Collection<Integer> itemIds4 = test.getItemIdsForFoodBox(4);
    assert itemIds4.size() == itemNum4;
    Collection<Integer> itemIds5 = test.getItemIdsForFoodBox(5);
    assert itemIds5.size() == itemNum5;
  
    System.out.println("box1:");
    for (Integer i: itemIds1) {
      System.out.println("id:" + i);
      System.out.println("name: " + test.getItemNameForFoodBox(i,1));
      System.out.println("quantity: " + test.getItemQuantityForFoodBox(i,1));
    }
    System.out.println("box2:");
    for (Integer i: itemIds2) {
      System.out.println("id:" + i);
      System.out.println("name: " + test.getItemNameForFoodBox(i,2));
      System.out.println("quantity: " + test.getItemQuantityForFoodBox(i,2));
    }
    System.out.println("box3:");
    for (Integer i: itemIds3) {
      System.out.println("id:" + i);
      System.out.println("name: " + test.getItemNameForFoodBox(i,3));
      System.out.println("quantity: " + test.getItemQuantityForFoodBox(i,3));
    }
    System.out.println("box4:");
    for (Integer i: itemIds4) {
      System.out.println("id:" + i);
      System.out.println("name: " + test.getItemNameForFoodBox(i,4));
      System.out.println("quantity: " + test.getItemQuantityForFoodBox(i,4));
    }
    System.out.println("box5:");
    for (Integer i: itemIds5) {
      System.out.println("id:" + i);
      System.out.println("name: " + test.getItemNameForFoodBox(i,5));
      System.out.println("quantity: " + test.getItemQuantityForFoodBox(i,5));
    }
  
    //--------------------------pickFoodBox()---------------------------
    test.pickFoodBox(1);
    assert test.getMarked().id == 1;
    //System.out.println("e1");
    test.pickFoodBox(2);
    assert test.getMarked().id == 2;
    //System.out.println("e2");
    test.pickFoodBox(3);
    assert test.getMarked().id == 3;
    //System.out.println("e3");
    test.pickFoodBox(4);
    assert test.getMarked().id == 4;
    //System.out.println("e4");
    test.pickFoodBox(5);
    assert test.getMarked().id == 5;
    //System.out.println("e5");
  
    //----------------changeItemQuantityForPickedFoodBox()-----------------------
    boolean addQ = test.changeItemQuantityForPickedFoodBox(9,3);
    assert !addQ;
    boolean minusQ = test.changeItemQuantityForPickedFoodBox(9,0);
    assert minusQ;
  
    Collection<ShieldingIndividualClientImp.Content> cs = test.getMarked().contents;
    int res = 1;
    for (ShieldingIndividualClientImp.Content ct: cs) {
      if (ct.id == 9) {
        res = ct.quantity;
        break;
      }
    }
    System.out.println("e7");
    assert res == 0;
  
    System.out.println("ed1");
    boolean place = test.placeOrder();
    assert place;
    System.out.println("ed2");
    assert test.getMarked() == null;
    System.out.println("ed3");
    boolean place2 = test.placeOrder();  //within 7 days
    System.out.println("ed4");
    assert place2 == false;
    
    
    
     */
    /*
    //--------------------Orders related need to set order list-----------------------
    ShieldingIndividualClientImp.Order orderPlaced = test.getLatest();
    int orderId = orderPlaced.orderId;
    Collection<Integer> orderIds = test.getOrderNumbers();
    assert orderIds.contains(orderId);
    //--------------------getStatusForOrder()-----------------------
    System.out.println("order status: " +  test.getStatusForOrder(orderId));
    //--------------------getItemIdsForOrder()-----------------------
    Collection<Integer> ids = test.getItemIdsForOrder(orderId);
    for (Integer i:ids) {
      System.out.println("order item id: "+i);
      System.out.println("order item name: "+test.getItemNameForOrder(i,orderId));
      System.out.println("order item quantity: " + test.getItemQuantityForOrder(i,orderId));
    }
    //--------------------getStatusForOrder()-----------------------
  */
  
  
  
  
    //for(String e: l){
    //System.out.println(e);
    //}
    //System.out.println("e");
    //String a = "EH1_9RG";
    //System.out.println(a.matches("EH([1-9]|(1[0-7]))_[1-9][A-Z][A-Z]"));
    //test.registerShieldingIndividual("1212120103");

//  DummyShieldingIndividualClientImp individual =  new DummyShieldingIndividualClientImp("http://0.0.0.0:5000/");
//  Collection<String> boxes = individual.showFoodBoxes("vegan");
//  System.out.println(boxes);
  
  
  
    return;

//    SupermarketClientImp supermarket = new SupermarketClientImp();

  }
}
