//import javax.json;
import java.util.Map;
import java.util.HashMap;

final public class WLTools {

   static public void main (String[] args) {

      Map<String, String> argsMap = new HashMap<>();

      for (String arg: args) {
         String[] parts = arg.split("=");
         argsMap.put(parts[0], parts[1]);
      } 

      //for (String key: argsMap.keySet()){
      //    System.out.println("key="+key);
      //    System.out.println("value="+argsMap.get(key));
      //}
  }
}
