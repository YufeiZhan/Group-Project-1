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
  
  public CateringCompanyClientImp(String endpoint) {
    // if name and postCode are null, then it hasn't been registered
    this.endpoint = endpoint;
    name = null;
    postCode = null;
    
  }

  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    assert name != null;
    assert postCode != null;
    if (name == null || postCode == null) return false;
    // when registering, set name and postCode (need postpone, only if success)
    this.name = name;
    this.postCode = postCode;
    
    // current version: new id to be fixed
    // server would return "id to be specified" --> name or postcode invalid
    // when registering twice, even if the parameters are invalid, should return true
    // move null checking inside try to enumerate all cases after get response
    
    // check the format of postCode
    boolean isMatch = postCode.matches("EH([1-9]|(1[0-7]))_[1-9][A-Z][A-Z]");
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
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
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
