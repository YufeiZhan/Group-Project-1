/*
 * main program
 *
 */

package shield;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Main {
  public static void main(String[] args) {

    
    //之前register过的 重新new instance之后，就没办法retrieve info
    //所以isregisered 却没有办法通过server reset user的info
    
    //ShieldingIndividualClientImp test = new ShieldingIndividualClientImp("http://0.0.0.0:5000/");
    //test.registerShieldingIndividual("1212120144");
    //assert test.isRegistered();
    
    
    //test.getClosestCateringCompany();
    //test.pickFoodBox(1);
    //boolean check = test.placeOrder();
    //System.out.println(check);
    
  
  
  
    //Collection<String> boxes = test.showFoodBoxes("none");
   // Collection<String> l = test.getCateringCompanies();
    
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
