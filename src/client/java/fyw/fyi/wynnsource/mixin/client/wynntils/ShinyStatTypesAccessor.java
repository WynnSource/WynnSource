package fyw.fyi.wynnsource.mixin.client.wynntils;

import com.wynntils.models.stats.ShinyModel;
import com.wynntils.models.stats.type.ShinyStatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = ShinyModel.class, remap = false)
public interface ShinyStatTypesAccessor {
    @Accessor(value = "shinyStatTypes", remap = false)
    Map<String, ShinyStatType> getShinyStatTypes();
}
