/**
 *
 */

package shield;

import java.io.IOException;
import java.util.Arrays;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private String endpoint;
  private CateringCompany cateringCompany;
  
  public CateringCompanyClientImp(String endpoint) {
    // if name and postCode are null, then it hasn't been registered
    this.endpoint = endpoint;
    cateringCompany = new CateringCompany();
  }
  
  private class CateringCompany{
    String name = null;
    String postCode = null;
    boolean isRegistered = false;
  }

  @Override
  // format of postCode
  // one ccimp can only register one cc? or multiple cc?
  public boolean registerCateringCompany(String name, String postCode) {
    // current version: new id to be fixed
    // server would return "id to be specified" --> name or postcode invalid
    // when registering twice, even if the parameters are invalid, should return true
    // move null checking inside try to enumerate all cases after get response
    
    // check invalid inputs
    assert name != null;
    assert postCode != null;
    if (name == null || postCode == null) return false;
    if (! isValidPostCode(postCode)) return false; // check postCode format
    if (cateringCompany.isRegistered == true) return true;
    
    String request = "/registerCateringCompany?business_name="+name+"&postcode="+postCode;
    String EXIST = "already registered";
    String SUCCESS = "registered new";
    boolean result = false;
    
    try {
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals(SUCCESS)){
        result = true;
        cateringCompany.name = name;
        cateringCompany.postCode = postCode;
        cateringCompany.isRegistered = true;
      } else if(response.equals(EXIST)){
        result = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  
  // Question: what should true represent? false?
  // Q2: no order info cached in this class
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    // check invalid inputs
    if (status == null) return false;
    String[] validStatus = new String[]{"packed", "dispatched", "delivered"};
    if (!Arrays.asList(validStatus).contains(status)) return false; // check validation of status
    
    String request = "/updateOrderStatus?order_id="+orderNumber+"&newStatus="+status;
    
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      
      if (response.equals("True")) {
        return true;
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return false;
  }

  @Override
  public boolean isRegistered() {
    return cateringCompany.isRegistered;
  }

  @Override
  public String getName() {
    return cateringCompany.name;
  }

  @Override
  public String getPostCode() {
    return cateringCompany.postCode;
  }
  // ------------------------------------ Public Testing Helper Methods -----------------------------------
  public void setCateringCompany(String name, String postcode){
    cateringCompany.name = name;
    cateringCompany.postCode = postcode;
    cateringCompany.isRegistered = true;
  }
  
  // --------------------------------------- Private Helper Methods ---------------------------------------
  private boolean isValidPostCode(String postCode){
    return postCode.matches("EH([1-9]|(1[0-7]))_[1-9][A-Z][A-Z]");
  }
}
