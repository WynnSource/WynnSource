package fyw.fyi.wynnsource.mixin.client.mc;

import com.llamalad7.mixinextras.sugar.Local;
import fyw.fyi.wynnsource.event.EventBus;
import fyw.fyi.wynnsource.event.ItemTooltipDrawEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Unique
    private final Set<Integer> triggeredItems = new HashSet<>();

    @Shadow
    abstract List<Text> getTooltipFromItem(ItemStack stack);

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"))
    private void onDrawItemTooltipPre(DrawContext context, int x, int y, CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        if (triggeredItems.contains(itemStack.hashCode())) return;

        EventBus.emitSync(new ItemTooltipDrawEvent(this.getTooltipFromItem(itemStack), itemStack, x, y));
        triggeredItems.add(itemStack.hashCode());
    }
}
