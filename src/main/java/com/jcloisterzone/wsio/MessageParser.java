package com.jcloisterzone.wsio;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.wsio.message.AbandonGameMessage;
import com.jcloisterzone.wsio.message.ChannelMessage;
import com.jcloisterzone.wsio.message.ChatMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.CommitMessage;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.DeployFlierMessage;
import com.jcloisterzone.wsio.message.DeployMeepleMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.GameUpdateMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.LeaveGameMessage;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.MoveNeutralFigureMessage;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PingMessage;
import com.jcloisterzone.wsio.message.PlaceTileMessage;
import com.jcloisterzone.wsio.message.PongMessage;
import com.jcloisterzone.wsio.message.PostChatMessage;
import com.jcloisterzone.wsio.message.ReturnMeepleMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;
import com.jcloisterzone.wsio.message.UndoMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public final class MessageParser {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final Map<String, Class<? extends WsMessage>> types = new HashMap<>();

    public MessageParser() {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();

        builder.registerTypeAdapter(SetRuleMessage.class, new JsonDeserializer<SetRuleMessage>() {
            @Override
            public SetRuleMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                CustomRule rule = CustomRule.valueOf(obj.get("rule").getAsString());
                return new SetRuleMessage(
                        obj.get("gameId").getAsString(), rule,
                        obj.get("value") == null ? null : rule.unpackValue(obj.get("value").getAsString())
                );
            }
        });
        builder.registerTypeAdapter(Location.class, new JsonSerializer<Location>() {
            @Override
            public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.getMask());
            }
        });
        builder.registerTypeAdapter(Location.class, new JsonDeserializer<Location>() {
            @Override
            public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return Location.create(json.getAsInt());
            }
        });
        builder.registerTypeAdapter(BoardPointer.class, new JsonSerializer<BoardPointer>() {
            @Override
            public JsonElement serialize(BoardPointer src, Type typeOfSrc, JsonSerializationContext context) {
                return context.serialize(src);
            }
        });
        builder.registerTypeAdapter(BoardPointer.class, new JsonDeserializer<BoardPointer>() {
            @Override
            public BoardPointer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("meepleId")) {
                    return context.deserialize(json, MeeplePointer.class);
                }
                if (obj.has("location")) {
                    return context.deserialize(json, FeaturePointer.class);
                }
                return context.deserialize(json, Position.class);
            }
        });

        gson = builder.create();

        registerMsgType(ErrorMessage.class);
        registerMsgType(HelloMessage.class);
        registerMsgType(WelcomeMessage.class);
        registerMsgType(CreateGameMessage.class);
        registerMsgType(JoinGameMessage.class);
        registerMsgType(LeaveGameMessage.class);
        registerMsgType(AbandonGameMessage.class);
        registerMsgType(GameMessage.class);
        registerMsgType(GameSetupMessage.class);
        registerMsgType(TakeSlotMessage.class);
        registerMsgType(LeaveSlotMessage.class);
        registerMsgType(SlotMessage.class);
        registerMsgType(SetExpansionMessage.class);
        registerMsgType(SetRuleMessage.class);
        registerMsgType(StartGameMessage.class);
        registerMsgType(DeployFlierMessage.class);
        registerMsgType(RmiMessage.class);
        registerMsgType(UndoMessage.class);
        registerMsgType(ClientUpdateMessage.class);
        registerMsgType(GameUpdateMessage.class);
        registerMsgType(PostChatMessage.class);
        registerMsgType(ChatMessage.class);
        registerMsgType(ChannelMessage.class);
        registerMsgType(GameOverMessage.class);
        registerMsgType(PingMessage.class);
        registerMsgType(PongMessage.class);
        registerMsgType(ToggleClockMessage.class);
        registerMsgType(ClockMessage.class);
        registerMsgType(CommitMessage.class);
        registerMsgType(PassMessage.class);
        registerMsgType(PlaceTileMessage.class);
        registerMsgType(DeployMeepleMessage.class);
        registerMsgType(ReturnMeepleMessage.class);
        registerMsgType(MoveNeutralFigureMessage.class);
    }

    protected String getCmdName(Class<? extends WsMessage> msgType) {
        return msgType.getAnnotation(WsMessageCommand.class).value();
    }

    private void registerMsgType(Class<? extends WsMessage> type) {
        types.put(getCmdName(type), type);
    }

    public WsMessage fromJson(String payload) {
        String s[] = payload.split(" ", 2); //command, arg
        Class<? extends WsMessage> type = types.get(s[0]);
        if (type == null) {
            throw new IllegalArgumentException("Mapping type is not declared for "+s[0]);
        }
        return gson.fromJson(s[1], type);
    }

    public String toJson(WsMessage arg) {
        return getCmdName(arg.getClass()) + " " + gson.toJson(arg);
    }
}
