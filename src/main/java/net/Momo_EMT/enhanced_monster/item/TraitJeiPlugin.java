package net.Momo_EMT.enhanced_monster.item;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JeiPlugin
public class TraitJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "main");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        
        List<IJeiAnvilRecipe> anvilRecipes = new ArrayList<>();

        for (String traitId : TraitConfig.getValidTraits()) {
            int maxLvl = TraitConfig.getMaxLevel(traitId);

            for (int lvl = 0; lvl < maxLvl; lvl++) {
                ItemStack left = ModItems.createTraitStack(traitId, lvl);
                ItemStack right = ModItems.createTraitStack(traitId, lvl);
                ItemStack result = ModItems.createTraitStack(traitId, lvl + 1);

                if (!left.isEmpty() && !result.isEmpty()) {
                    anvilRecipes.add(factory.createAnvilRecipe(
                            left, 
                            Collections.singletonList(right), 
                            Collections.singletonList(result)
                    ));
                }
            }
        }

        if (!anvilRecipes.isEmpty()) {
            registration.addRecipes(RecipeTypes.ANVIL, anvilRecipes);
        }
    }
}