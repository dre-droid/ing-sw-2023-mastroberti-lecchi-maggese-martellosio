package main.java.it.polimi.ingsw.Server.Socket;

import com.google.gson.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.*;

import java.lang.reflect.Type;

public class StrategyAdapter implements JsonSerializer<StrategyCommonGoal>, JsonDeserializer<StrategyCommonGoal> {
    private static final String CLASSNAME = "CLASSNAME";
    private static final String DATA = "DATA";

    @Override
    public StrategyCommonGoal deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = prim.getAsString();
        int strategyType = Integer.parseInt(className);
        switch (strategyType) {
            case 1:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), Diagonal.class);
            case 2:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), EightofSameType.class);
            case 3:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), FourCornerOfTheSameType.class);
            case 4:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), FourGroupsOfAtLeastFourSameTypeTiles.class);
            case 5:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), FourRowsOfMaxThreeDifferentTypes.class);
            case 6:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), IncreasingOrDecreasingHeight.class);
            case 7:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), SixGroupsOfAtLeastTwoSameTypeTiles.class);
            case 8:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), SquaredShapedGroups.class);
            case 9:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), ThreeColumnsOfMaxThreeDifferentTypes.class);
            case 10:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), TwoColumnsOfDifferentTypes.class);
            case 11:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), TwoLinesOfDifferentTypes.class);
            case 12:
                return jsonDeserializationContext.deserialize(jsonObject.get(DATA), XShapedTiles.class);
            default:
                throw new JsonParseException("Unknown strategy type: " + strategyType);
        }
    }

    @Override
    public JsonElement serialize(StrategyCommonGoal strategyCommonGoal, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        String className = String.valueOf(strategyCommonGoal.getClassID());
        jsonObject.addProperty(CLASSNAME, className);
        jsonObject.add(DATA, jsonSerializationContext.serialize(strategyCommonGoal));
        return jsonObject;
    }
}
