

package tech.zephon.sailpoint.properties.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;


public class JsonHelper {
    
    ObjectMapper mapper = new ObjectMapper();
    
    
    public String getJson(Object objValue)
    {
        try
        {
            String json = mapper.writeValueAsString(objValue);
            return json;
        }
        catch(Exception e)
        {
            return "";
        }
    }
    
    public Map makeErrorMap(String key, String value)
    {
        Map errorMap = new HashMap();
        errorMap.put(key, value);
        return errorMap;
    }

}
