package com.example.springtest.websocket_route;

import com.example.springtest.Responses.PlayerSlot;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@RestController
@MessageMapping("/ready")
public class ReadyPage {

    private int numberOfConnectedPlayers = 0;
    private int nowEditingPlayer = 0;
    private final PlayerSlot[] playerSlot = new PlayerSlot[2];

    @Autowired
    private SimpMessagingTemplate simpleMessagingTemplate;

    @MessageMapping("/typeName")
    @SendTo("/topic/name")
    public NameResponse typeName(@RequestBody NameRequest nameRequest) {
        NameResponse nameResponse = new NameResponse();
        nameResponse.setNameP1(nameRequest.getNameP1());
        nameResponse.setNameP2(nameRequest.getNameP2());
        return nameResponse;
    }

    @MessageMapping("/changeReady")
    @SendTo("/topic/ready")
    public ReadyResponse changeReady(@RequestBody ReadyRequest readyRequest) {
        ReadyResponse readyResponse = new ReadyResponse();
        readyResponse.setReadyP1(readyRequest.isReadyP1());
        readyResponse.setReadyP2(readyRequest.isReadyP2());
        return readyResponse;
    }


    /*
     * To send message to client to know what player slot is
     */
    @MessageMapping("/lockPlayerSlot")
    public void playerSlot(final Principal principal) {
        simpleMessagingTemplate.convertAndSendToUser(principal.getName(), "/topic/playerSlot", playerSlot[nowEditingPlayer]);
    }

    @MessageMapping("/start")
    @SendTo("/topic/gameStart")
    public boolean gameStart() {
        return true;
    }

    private void autoSelectSlot(Principal principal) {
        if (playerSlot[0] == null) {
            playerSlot[0] = new PlayerSlot();
            playerSlot[0].setPlayerSlot(1);
            playerSlot[0].setUuid(principal.getName());
            nowEditingPlayer = 0;
        } else if (playerSlot[1] == null) {
            playerSlot[1] = new PlayerSlot();
            playerSlot[1].setPlayerSlot(2);
            playerSlot[1].setUuid(principal.getName());
            nowEditingPlayer = 1;
        }
    }

    private void autoRemoveSlot(Principal principal) {
        if (playerSlot[0] != null && playerSlot[0].getUuid().equals(principal.getName())) {
            playerSlot[0] = null;
        } else if (playerSlot[1] != null && playerSlot[1].getUuid().equals(principal.getName())) {
            playerSlot[1] = null;
        }
    }

    @EventListener(SessionConnectedEvent.class)
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        numberOfConnectedPlayers++;
        autoSelectSlot(event.getUser());
        System.out.println("Number of connected players: " + numberOfConnectedPlayers);
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        numberOfConnectedPlayers--;
        autoRemoveSlot(event.getUser());
        System.out.println("Number of connected players: " + numberOfConnectedPlayers);
    }
}

@Data
class ReadyRequest {
    private boolean readyP1;
    private boolean readyP2;

    public boolean isReadyP1() {
        return readyP1;
    }

    public void setReadyP1(boolean readyP1) {
        this.readyP1 = readyP1;
    }

    public boolean isReadyP2() {
        return readyP2;
    }

    public void setReadyP2(boolean readyP2) {
        this.readyP2 = readyP2;
    }
}

@Data
class ReadyResponse {
    private boolean readyP1;
    private boolean readyP2;

    public boolean isReadyP1() {
        return readyP1;
    }

    public void setReadyP1(boolean readyP1) {
        this.readyP1 = readyP1;
    }

    public boolean isReadyP2() {
        return readyP2;
    }

    public void setReadyP2(boolean readyP2) {
        this.readyP2 = readyP2;
    }
}

@Data
class NameRequest {
    private String nameP1;
    private String nameP2;

    public String getNameP1() {
        return nameP1;
    }

    public void setNameP1(String nameP1) {
        this.nameP1 = nameP1;
    }

    public String getNameP2() {
        return nameP2;
    }

    public void setNameP2(String nameP2) {
        this.nameP2 = nameP2;
    }

}

@Data
class NameResponse {
    private String nameP1;
    private String nameP2;

    public String getNameP1() {
        return nameP1;
    }

    public void setNameP1(String nameP1) {
        this.nameP1 = nameP1;
    }

    public String getNameP2() {
        return nameP2;
    }

    public void setNameP2(String nameP2) {
        this.nameP2 = nameP2;
    }
}