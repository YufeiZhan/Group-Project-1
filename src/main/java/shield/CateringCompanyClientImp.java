/**
 *
 */

package shield;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private String endpoint;
  private String name;
  private String postCode;

  public CateringCompanyClientImp(String endpoint) { this.endpoint = endpoint; }

  @Override
  public boolean registerCateringCompany(String name, String postCode) {

    return false;
  }

  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    return false;

  }

  @Override
  public boolean isRegistered() {
    return false;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getPostCode() {
    return null;
  }
}
