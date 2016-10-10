package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.api.code.Function;
import mcjty.rftoolscontrol.api.parameters.*;
import mcjty.rftoolscontrol.logic.InventoryTools;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import mcjty.rftoolscontrol.logic.running.ProgException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;

import static mcjty.rftoolscontrol.api.parameters.ParameterType.*;

public class Functions {

    public static Random random = new Random(System.currentTimeMillis());

    public static final Function LASTBOOL = Function.builder()
            .id("last_bool")
            .name("last")
            .description("The last opcode result", "converted to a boolean")
            .type(PAR_BOOLEAN)
            .runnable((processor, program) -> {
                return convertToBool(program.getLastValue());
            })
            .build();
    public static final Function LASTINT = Function.builder()
            .id("last_int")
            .name("last")
            .description("The last opcode result", "converted to an integer")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                return convertToInt(program.getLastValue());
            })
            .build();
    public static final Function LASTSTRING = Function.builder()
            .id("last_str")
            .name("last")
            .description("The last opcode result", "converted to a string")
            .type(PAR_STRING)
            .runnable((processor, program) -> {
                return convertToString(program.getLastValue());
            })
            .build();
    public static final Function LASTITEM = Function.builder()
            .id("last_item")
            .name("last")
            .description("The last opcode result as an item", "Can also convert a string representing", "a registry name to an item")
            .type(PAR_ITEM)
            .runnable((processor, program) -> {
                return convertToItem(program.getLastValue());
            })
            .build();
    public static final Function LASTINV = Function.builder()
            .id("last_inv")
            .name("last")
            .description("The last opcode result as an inventory (position)", "Can also convert a string with format", "'<name> S/S' to a position")
            .type(PAR_INVENTORY)
            .runnable((processor, program) -> {
                return convertToInventory(program.getLastValue());
            })
            .build();
    public static final Function LASTSIDE = Function.builder()
            .id("last_side")
            .name("last")
            .description("The last opcode result as a side (position)", "Can also convert a string with format", "'<name> S' to a side")
            .type(PAR_SIDE)
            .runnable((processor, program) -> {
                return convertToSide(program.getLastValue());
            })
            .build();
    public static final Function TICKET = Function.builder()
            .id("ticket")
            .name("ticket")
            .description("The current crafting ticket")
            .type(PAR_STRING)
            .runnable((processor, program) -> {
                return ParameterValue.constant(program.getCraftTicket());
            })
            .build();
    public static final Function CRAFTRESULT = Function.builder()
            .id("craftresult")
            .name("craftresult")
            .description("The current desired crafting result")
            .type(PAR_ITEM)
            .runnable((processor, program) -> {
                return ParameterValue.constant(processor.getCraftResult(program));
            })
            .build();
    public static final Function RANDOMINT = Function.builder()
            .id("random_int")
            .name("random")
            .description("A random integer between 0", "and the last opcode result (exclusive)", "(converted to integer)")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                ParameterValue v = convertToInt(program.getLastValue());
                return ParameterValue.constant(random.nextInt((Integer) v.getValue()));
            })
            .build();
    public static final Function RANDOMFLOAT = Function.builder()
            .id("random_float")
            .name("random")
            .description("A random floating number between 0", "and the last opcode result (exclusive)", "(converted to float)")
            .type(PAR_FLOAT)
            .runnable((processor, program) -> {
                ParameterValue v = convertToFloat(program.getLastValue());
                return ParameterValue.constant(random.nextFloat() * (Float) v.getValue());
            })
            .build();

    private static ParameterValue convertToBool(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(false);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(false);
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return ParameterValue.constant(!((String) v).isEmpty());
            case PAR_INTEGER:
                return ParameterValue.constant(((Integer) v) != 0);
            case PAR_FLOAT:
                return ParameterValue.constant(((Float) v) != 0);
            case PAR_BOOLEAN:
                return value.getParameterValue();
            case PAR_TUPLE:
                return ParameterValue.constant(((Tuple) v).getX() != 0 || ((Tuple) v).getY() != 0);
            case PAR_SIDE:
            case PAR_INVENTORY:
            case PAR_ITEM:
            case PAR_EXCEPTION:
                return ParameterValue.constant(true);
        }
        return ParameterValue.constant(false);
    }

    private static ParameterValue convertToFloat(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(0);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(0);
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return ParameterValue.constant(Float.parseFloat((String) v));
            case PAR_INTEGER:
                return ParameterValue.constant(((Integer) v).floatValue());
            case PAR_FLOAT:
                return value.getParameterValue();
            case PAR_SIDE:
                return ParameterValue.constant(0);
            case PAR_BOOLEAN:
                return ParameterValue.constant(((Boolean) v) ? 1.0f : 0.0f);
            case PAR_INVENTORY:
                break;
            case PAR_ITEM:
                return ParameterValue.constant((float) ((ItemStack) v).stackSize);
            case PAR_EXCEPTION:
            case PAR_TUPLE:
                break;
        }
        return ParameterValue.constant(0);
    }

    private static ParameterValue convertToInt(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(0);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(0);
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                String s = (String) v;
                if (s.startsWith("$")) {
                    return ParameterValue.constant((int) Long.parseLong(s.substring(1), 16));
                } else {
                    return ParameterValue.constant(Integer.parseInt(s));
                }
            case PAR_INTEGER:
                return value.getParameterValue();
            case PAR_FLOAT:
                return ParameterValue.constant(((Float) v).intValue());
            case PAR_SIDE:
                return ParameterValue.constant(0);
            case PAR_BOOLEAN:
                return ParameterValue.constant(((Boolean) v) ? 1 : 0);
            case PAR_INVENTORY:
                break;
            case PAR_ITEM:
                return ParameterValue.constant(((ItemStack) v).stackSize);
            case PAR_EXCEPTION:
            case PAR_TUPLE:
                break;
        }
        return ParameterValue.constant(0);
    }

    private static ParameterValue convertToString(Parameter value) {
        if (value == null) {
            return ParameterValue.constant("");
        }
        if (!value.isSet()) {
            return ParameterValue.constant("");
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return value.getParameterValue();
            case PAR_INTEGER:
                return ParameterValue.constant(Integer.toString((Integer) v));
            case PAR_FLOAT:
                return ParameterValue.constant(Float.toString((Float) v));
            case PAR_BOOLEAN:
                return ParameterValue.constant(((Boolean) v) ? "true" : "false");
            case PAR_ITEM:
                return ParameterValue.constant(((ItemStack) v).getItem().getRegistryName().toString());
            case PAR_INVENTORY:
                return ParameterValue.constant(InventoryTools.inventoryToString((Inventory) v));
            case PAR_SIDE:
                return ParameterValue.constant(InventoryTools.blockSideToString((BlockSide) v));
            case PAR_EXCEPTION:
                return ParameterValue.constant(((ExceptionType) v).getCode());
            case PAR_TUPLE:
                return ParameterValue.constant(v.toString());
        }
        return ParameterValue.constant("");
    }

    private static ParameterValue convertToInventory(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(null);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(null);
        }
        switch (value.getParameterType()) {
            case PAR_INVENTORY:
                return value.getParameterValue();
            case PAR_SIDE: {
                BlockSide v = (BlockSide) value.getParameterValue().getValue();
                if (v.getSide() == null) {
                    throw new ProgException(ExceptionType.EXCEPT_BADPARAMETERS);
                }
                return ParameterValue.constant(new Inventory(v.getNodeName(), v.getSide(), null));
            }
            case PAR_STRING: {
                Object v = value.getParameterValue().getValue();
                return ParameterValue.constant(InventoryTools.inventoryFromString(v.toString()));
            }
        }
        return ParameterValue.constant(null);
    }

    private static ParameterValue convertToSide(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(null);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(null);
        }
        switch (value.getParameterType()) {
            case PAR_SIDE:
                return value.getParameterValue();
            case PAR_INVENTORY: {
                Inventory v = (Inventory) value.getParameterValue().getValue();
                return ParameterValue.constant(v);
            }
            case PAR_STRING: {
                Object v = value.getParameterValue().getValue();
                return ParameterValue.constant(InventoryTools.blockSideFromString(v.toString()));
            }
        }
        return ParameterValue.constant(null);
    }

    private static ParameterValue convertToItem(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(null);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(null);
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return ParameterValue.constant(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) v)), 1, 0));
            case PAR_ITEM:
                return value.getParameterValue();
        }
        return ParameterValue.constant(null);
    }

    public static final Map<String, Function> FUNCTIONS = new HashMap<>();
    private static final Map<ParameterType,List<Function>> FUNCTIONS_BY_TYPE = new HashMap<>();

    public static void init() {
        register(LASTBOOL);
        register(LASTINT);
        register(LASTSTRING);
        register(LASTITEM);
        register(LASTINV);
        register(LASTSIDE);
        register(TICKET);
        register(CRAFTRESULT);
        register(RANDOMINT);
        register(RANDOMFLOAT);
    }

    public static void register(Function function) {
        FUNCTIONS.put(function.getId(), function);
        ParameterType type = function.getReturnType();
        if (!FUNCTIONS_BY_TYPE.containsKey(type)) {
            FUNCTIONS_BY_TYPE.put(type, new ArrayList<>());
        }
        FUNCTIONS_BY_TYPE.get(type).add(function);
    }

    @Nonnull
    public static List<Function> getFunctionsByType(ParameterType type) {
        if (FUNCTIONS_BY_TYPE.containsKey(type)) {
            return FUNCTIONS_BY_TYPE.get(type);
        } else {
            return Collections.emptyList();
        }
    }
}
