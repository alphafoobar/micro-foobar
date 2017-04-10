import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonExtractor {

    public static String returnJsonValue(String args, String value) {
        JSONParser parser = new JSONParser();
        System.out.println(args);
        try {
            Object obj = parser.parse(args);
            JSONObject jsonObject = (JSONObject) obj;
            return (String) jsonObject.get(value);
        } catch (ParseException exception) {
            System.out.println("No Object found: " + exception);
        }
        return null;
    }
}
