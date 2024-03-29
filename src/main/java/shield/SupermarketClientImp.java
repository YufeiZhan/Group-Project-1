/**
 *
 */

package shield;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

public class SupermarketClientImp implements SupermarketClient {
  
  private String endpoint;
  private Supermarket supermarket;
  private List<SupermarketOrder> orderList;
  
  public SupermarketClientImp(String endpoint) {
    this.endpoint = endpoint;
    supermarket = new Supermarket();
    orderList = new ArrayList<>();
  }
  
  private class Supermarket {
    String name = null;
    String postCode = null;
    boolean isRegistered = false;
    }
  
  private class SupermarketOrder {
    final int orderNum;
    final String CHI;
    int status;
    
    public SupermarketOrder(String CHI, int orderNum){
      this.CHI = CHI;
      this.orderNum = orderNum;
      this.status = 0;
    }
  }

  @Override
  public boolean registerSupermarket(String name, String postCode) {
    //check validity of inputs
    if (name == null || postCode == null || !isValidPostCode(postCode)) return false;
    
    if(supermarket.isRegistered == true) return true;
    
    String request = "/registerSupermarket?business_name="+name+"&postcode="+postCode;
    boolean result = false;
    
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("registered new")){
        supermarket.name = name;
        supermarket.postCode = postCode;
        supermarket.isRegistered = true;
        result = true;
      }
      if(response.equals("already registered")){
        result = true;
      }
    } catch (Exception e){
      e.printStackTrace();
    }
    
    return result;
  }

  // **UPDATE2** ADDED METHOD
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    
    //check validity of inputs
    if (CHI == null) return false;
    
    String request = "/recordSupermarketOrder?individual_id="+CHI+"&order_number="+orderNumber+"&supermarket_business_name="+supermarket.name+"&supermarket_postcode="+supermarket.postCode;
    boolean result = false;
    
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      
      if (response.equals("True")){
        result = true;
        SupermarketOrder newOrder = new SupermarketOrder(CHI, orderNumber);
        orderList.add(newOrder);
        System.out.println(orderList);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return result;
  }

  // **UPDATE**
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    //check validity of inputs
    if (status == null) return false;
    
    String request = "/updateSupermarketOrderStatus?order_id="+orderNumber+"&newStatus="+status;
    boolean result = false;
  
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
    
      if (response.equals("True")){
        result = true;
        
        //find the order in the list & update it
        if (orderExist(orderNumber)) {
          SupermarketOrder theOrder = getOrderFromList(orderNumber);
          updateObjectStatus(theOrder, status);
        }
        
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return result;
  }

  @Override
  public boolean isRegistered() {
    return supermarket.isRegistered;
  }

  @Override
  public String getName() {
    return supermarket.name;
  }

  @Override
  public String getPostCode() {
    return supermarket.postCode;
  }
  
  
  // ------------------------------------ Public Testing Helper Methods ------------------------------------
  public List<SupermarketOrder> getOrderList() {return orderList;}
  
  public void setSupermarket(String name, String postcode){
    supermarket.name = name;
    supermarket.postCode = postcode;
    supermarket.isRegistered = true;
  }
  public boolean orderExist(int orderNum){
    for (SupermarketOrder order: orderList){
      if (order.orderNum == orderNum){
        return true;
      }
    }
    return false;
  }
  
  public int getOrderStatus(int orderNum){
    for (SupermarketOrder order: orderList){
      if (order.orderNum == orderNum){
        return order.status;
      }
    }
    return -1;
  }
  
  public boolean setOrder(String CHI, int orderNum){
    
    assert CHI != null;
    
    if (orderExist(orderNum)){
      return false;
    } else{
      orderList.add(new SupermarketOrder(CHI, orderNum));
      return true;
    }
  }
  
  
  // --------------------------------------- Private Helper Methods ---------------------------------------
  private SupermarketOrder getOrderFromList(int orderNum){
    for (SupermarketOrder order: orderList){
      if(order.orderNum == orderNum){
        return order;
      }
    }
    return null;
  }
  
  private boolean updateObjectStatus(SupermarketOrder order, String status){
    assert order != null;
    assert status != null;
    
    int currentStatus = getOrderStatus(order.orderNum);
    int newStatus = 0;
    switch (status){
      case "packed":
        newStatus = 1;
        break;
      case "dispatched":
        newStatus = 2;
        break;
      case "delivered":
        newStatus = 3;
        break;
      default:
        break;
    }

    if (newStatus > currentStatus){
      order.status = newStatus;
      return true;
    } else{
      return false;
    }
  }
  
  private boolean isValidPostCode(String postcode){
    assert postcode != null;
    return postcode.matches("EH([1-9]|(1[0-7]))_[1-9][A-Z][A-Z]");
  }
  
}
