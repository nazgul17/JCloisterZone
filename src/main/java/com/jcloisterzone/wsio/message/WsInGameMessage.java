package com.jcloisterzone.wsio.message;

public interface WsInGameMessage extends WsMessage {

    String getGameId();
    void setGameId(String gameId);
}
