package ai.mindgard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface JSON {
    static String json(Object o)  {
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> T fromJson(String string, Class<T> cls) {
        try {
            return new ObjectMapper().readValue(string, cls);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static String escape(String prompt) {
        var om = new ObjectMapper();
        om.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
        try {
            String quoted = new String(JsonStringEncoder.getInstance().quoteAsUTF8(om.writeValueAsString(prompt)));
            return quoted.substring(2,quoted.length()-2);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
