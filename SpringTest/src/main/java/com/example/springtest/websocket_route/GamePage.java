package com.example.springtest.websocket_route;

import Game.Game;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@MessageMapping("/game")
public class GamePage {

    @SendTo("/topic/gameUpdate")
    public static Game broadcastGameUpdate(Game game) {
        return game;
    }
}
