package com.example.springtest.Config;

import com.sun.security.auth.UserPrincipal;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class UserHandShakeHandler extends DefaultHandshakeHandler {
    private final Logger LOG = Logger.getLogger(UserHandShakeHandler.class.getName());

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        final String userID = UUID.randomUUID().toString();
        LOG.info("User connected: " + userID);
       return new UserPrincipal(userID);
    }
}
