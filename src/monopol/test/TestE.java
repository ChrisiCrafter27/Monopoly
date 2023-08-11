package monopol.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import monopol.server.Server;
import monopol.utils.Json;

public class TestE {
    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(Json.toString(new TestE().server, true));
    }

    Server server = new Server(12345);
}
