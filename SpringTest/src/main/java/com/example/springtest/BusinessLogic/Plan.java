package com.example.springtest.BusinessLogic;

import Game.Game;
import Game.GameUtils;
import com.example.springtest.Exception.BadSubmitPlanException;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class Plan {
    @Setter
    private static Game game;

    public static Game submitPlan(String construction_plan, int player_id) throws RuntimeException {
        try {
            if (game.getCurrentPlayer().getID() == player_id) {
                String plan = construction_plan;
                game.submitPlan(plan);
                return game;
            } else {
                throw new RuntimeException("It's not your turn");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    //Mock test
    private static Game mock_game = GameUtils.createGame("player1", "player2");
    public static Game submitPlanMock(String construction_plan, int player_id) throws RuntimeException {
        try {
            if (mock_game.getCurrentPlayer().getID() == player_id) {
                String plan = construction_plan;
                mock_game.submitPlan(plan);
                return mock_game;
            } else {
                throw new RuntimeException("It's not your turn");
            }
        } catch (RuntimeException e) {
            throw new BadSubmitPlanException(e.getMessage());
        }
    }
}
