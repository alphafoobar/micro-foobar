import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
/**
 * Created by James on 19/03/2017.
 */
public class JSONExtractor {

    public static String returnJSONValue(String args, String value){
        JSONParser parser = new JSONParser();
        String app= null;
        System.out.println(args);
        try{
            Object obj = parser.parse(args);
            JSONObject JObj = (JSONObject)obj;
            app= (String) JObj.get(value);
            return app;
        }
        catch(ParseException pe){
            System.out.println("No Object found");
            System.out.println(pe);
        }
        return app;
    }
}
