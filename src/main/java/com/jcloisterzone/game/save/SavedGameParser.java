package com.jcloisterzone.game.save;

import java.io.Writer;
import java.lang.reflect.Type;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
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
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.WsCommandRegistry;
import com.jcloisterzone.wsio.WsMessageCommand;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;

public class SavedGameParser {

    private Gson gson;

    public SavedGameParser() {
        GsonBuilder builder = MessageParser.createGsonBuilder();

        builder.registerTypeAdapter(WsReplayableMessage.class, new JsonSerializer<WsReplayableMessage>() {
            @Override
            public JsonElement serialize(WsReplayableMessage src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject obj = new JsonObject();
                obj.add("type", new JsonPrimitive(src.getClass().getAnnotation(WsMessageCommand.class).value()));
                obj.add("payload", context.serialize(src));
                return obj;
            }
        });
        builder.registerTypeAdapter(WsReplayableMessage.class, new JsonDeserializer<WsReplayableMessage>() {
            @Override
            public WsReplayableMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                JsonObject obj = (JsonObject) json;
                Class<? extends WsMessage> cls = WsCommandRegistry.TYPES.get(obj.get("type").getAsString()).get();
                return context.deserialize(obj.get("payload"), cls);
            }
        });

        gson = builder
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setExclusionStrategies(new SavedGameExclStrat())
            .setPrettyPrinting()
            .create();
    }

    public String toJson(SavedGame src) {
        return gson.toJson(src);
    }

    public void toJson(SavedGame src, Writer writer) {
        gson.toJson(src, writer);
    }

    public SavedGame fromJson(String json) {
        return gson.fromJson(json, SavedGame.class);
    }

    public class SavedGameExclStrat implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
            return (WsReplayableMessage.class.isAssignableFrom(f.getDeclaringClass()) && f.getName().equals("gameId"));
        }

    }

}
