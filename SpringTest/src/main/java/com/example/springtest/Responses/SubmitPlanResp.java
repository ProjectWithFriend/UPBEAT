package com.example.springtest.Responses;

import Game.*;
import Region.*;
import Player.*;
import lombok.Data;

import java.util.List;

@Data
public class SubmitPlanResp {
    List<Region> territory;
    double player_1_budget;
    double player_2_budget;
}
