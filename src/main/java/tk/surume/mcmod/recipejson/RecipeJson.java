package tk.surume.mcmod.recipejson;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


@Mod(modid = RecipeJson.MODID, version = RecipeJson.VERSION)
public class RecipeJson
{
    public static final String MODID = "recipejson";
    public static final String VERSION = "1.0";
    
    static String convertInputStreamToString(InputStream is) throws IOException {
        InputStreamReader reader = new InputStreamReader(is);
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[512];
        int read;
        while (0 <= (read = reader.read(buffer))) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) throws IOException
    {
        // コンフィグロード
        File configFile = new File("./config/recipe.json");
        if (configFile.exists() == false) { // デフォルトコンフィグのコピー
            configFile.createNewFile();
            FileWriter writer = new FileWriter(configFile);
            InputStream defaultConfigStream = this.getClass().getClassLoader().getResourceAsStream("recipe.default.json");
            writer.write(convertInputStreamToString(defaultConfigStream));
            writer.close();
        }
        FileReader reader = new FileReader(configFile);
        JsonArray recipes = (JsonArray) new Gson().fromJson(reader, JsonArray.class);
        for (int i = 0; i < recipes.size(); i++) {
            JsonObject recipe = recipes.get(i).getAsJsonObject();
            ItemStack outItem;
            // 出力アイテムのチェック
            JsonElement outItemElement = recipe.get("out");
            if(outItemElement == null) {
                throw new RuntimeException(String.valueOf(i+1)+"番目のレシピの”out”が指定されていません");
            }
            String outItemName = outItemElement.getAsString();
            if(Item.itemRegistry.containsKey(outItemName) == false) {
                throw new RuntimeException(String.valueOf(i+1)+"番目のレシピの”out”に"+
                    "指定されているアイテム”"+outItemName+"”が見つかりませんでした");
            }
            outItem = new ItemStack(Item.class.cast(Item.itemRegistry.getObject(outItemName)));
            JsonElement recipeElement = recipe.get("recipe");
            if(recipeElement == null) {
                throw new RuntimeException(String.valueOf(i+1)+"番目のレシピの”recipe”が指定されていません");
            }
            // 定形レシピかどうかで別れる
            if (recipe.get("bind") != null) { // 定形レシピ
                List<Object> params = new ArrayList<Object>();
                JsonArray recipeItems = recipeElement.getAsJsonArray();
                // レシピ文字列を追加
                for (int j = 0; j < recipeItems.size(); j++) {
                    params.add((Object) recipeItems.get(j).getAsString());
                }
                // bindを追加
                JsonObject bindPatternsObject = recipe.get("bind").getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> bindPatterns = bindPatternsObject.entrySet();
                for (Map.Entry<String, JsonElement> pattern: bindPatterns) {
                    params.add((Object) pattern.getKey().charAt(0));
                    String itemName = pattern.getValue().getAsString();
                    if(Item.itemRegistry.containsKey(itemName) == false) {
                        throw new RuntimeException(String.valueOf(i+1)+"番目のレシピのbindの”"+pattern.getKey().charAt(0)+"”に"+
                            "指定されているアイテム”"+itemName+"”が見つかりませんでした");
                    }
                    ItemStack item = new ItemStack(Item.class.cast(Item.itemRegistry.getObject(itemName)));
                    params.add((Object) item);
                }
                GameRegistry.addRecipe(outItem, params.toArray());
            } else { // 不定形レシピ
                List<Object> params = new ArrayList<Object>();
                JsonArray recipeItems = recipeElement.getAsJsonArray();
                for (int j = 0; j < recipeItems.size(); j++) {
                    ItemStack item;
                    String itemName = recipeItems.get(j).getAsString();
                    if(Item.itemRegistry.containsKey(itemName) == false) {
                        throw new RuntimeException(String.valueOf(i+1)+"番目のレシピの"+String.valueOf(j+1)+"番目に"+
                            "指定されているアイテム”"+itemName+"”が見つかりませんでした");
                    }
                    item = new ItemStack(Item.class.cast(Item.itemRegistry.getObject(itemName)));
                    params.add((Object) item);
                }
                GameRegistry.addShapelessRecipe(outItem, params.toArray());
            }
        }
        reader.close();

        System.out.println(Item.itemRegistry.containsKey("minecraft:dirrt"));
        
                System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
                GameRegistry.addRecipe(new ItemStack(Items.diamond, 1),
                    " x ",
                    "xxx",
                    " x ",
                    'x', new ItemStack(Item.class.cast(Item.itemRegistry.getObject("minecraft:dirrrt")))
                );
    }
}
