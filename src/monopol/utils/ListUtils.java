package monopol.utils;

import monopol.server.ServerPlayer;

import java.util.List;

public class ListUtils {

    public static boolean equals(List<ServerPlayer> list1, List<ServerPlayer> list2) {
        if(list1.size() != list2.size()) return false;
        for(int i = 0; i < list1.size(); i++) {
            if(!list1.get(i).getName().equals(list2.get(i).getName())) return false;
        }
        return true;
    }

}
