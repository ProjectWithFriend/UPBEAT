package com.example.springtest.REST_API;

import Game.*;
import com.example.springtest.BusinessLogic.Plan;
import com.example.springtest.Exception.BadRequestException;
import com.example.springtest.RequestBody.CreateGameReq;
import com.example.springtest.SingelTon.GameCreation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class CreateGame {

    //get to game data to submitPlan
    @PostMapping("/createGame")
    public ResponseEntity<Game> createGame(@RequestBody CreateGameReq req) {
        try {
            if (req.getPlayer_2_name() == null || req.getPlayer_1_name() == null) {
                throw new BadRequestException("Player names cannot be null");
            } else {
                Game game = GameCreation.getGame(req.getPlayer_1_name(), req.getPlayer_2_name());
                Plan.setGame(game); //set the game to plan use when submit plan
                return ResponseEntity.ok(game);
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
