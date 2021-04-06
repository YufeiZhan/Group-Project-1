/**
 *
 */

package shield;

import java.io.IOException;
import java.util.Arrays;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private String endpoint;
  private String name;
  private String postCode;
  
  public CateringCompanyClientImp(String ep, String n, String pc) {
    endpoint = ep;
    name = n;
    postCode = pc;
  }

  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    if (name == null || postCode == null) return false;
    
    // check the format of postCode
    boolean isMatch = postCode.matches("^EH[0-9]{1,2}_[A-Z]{1,2}[0-9]");
    if (!isMatch) return false;
    
    String request = "/registerCateringCompany?business name="+name+"&postcode="+postCode;
    String EXIST = "already registered";
    String SUCCESS = "registered now";
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
  
      // if the name and postCode are valid (already registered/new)
      // then returns true; else, returns false.
      if (response == EXIST || response == SUCCESS){
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  
    return false;
  }

  
  // Question: what should true represent? false?
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    if (status == null) return false;
    
    // check validation of status
    String[] validStatus = new String[]{"packed", "dispatched", "delivered"};
    if (!Arrays.asList(validStatus).contains(status)) return false;
    
    String request = "/updateOrderStatus?order id="+orderNumber+"&newStatus="+status;
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      
      if (response == "true") return true;
      if (response == "false") return false;
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return false;
  }

  @Override
  public boolean isRegistered() {
    try {
      if (registerCateringCompany(name, postCode)) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getPostCode() {
    return postCode;
  }
}
