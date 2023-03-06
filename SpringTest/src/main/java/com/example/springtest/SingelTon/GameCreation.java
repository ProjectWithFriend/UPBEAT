package com.example.springtest.SingelTon;


import Game.*;

import java.util.HashMap;
import java.util.Map;

public class GameCreation {
    private static Game gameCreation;
    private static final Map<String[],Game> map = new HashMap<>();

    public static Game getGame(String nameP1,String nameP2){
        if(map.isEmpty() || !map.containsKey(new String[]{nameP1,nameP2})){
            gameCreation = GameUtils.createGame(nameP1, nameP2);
            map.put(new String[]{nameP1,nameP2},gameCreation);
            return gameCreation;
        }else{
            if(map.get(new String[]{nameP1,nameP2}) == null){
                gameCreation = GameUtils.createGame(nameP1, nameP2);
                map.put(new String[]{nameP1,nameP2},gameCreation);
                return gameCreation;
            }
        }
        return null;
    }
}
