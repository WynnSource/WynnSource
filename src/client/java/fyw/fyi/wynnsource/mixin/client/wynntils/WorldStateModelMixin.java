package fyw.fyi.wynnsource.mixin.client.wynntils;

import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.type.WorldState;
import fyw.fyi.wynnsource.event.EventBus;
import fyw.fyi.wynnsource.event.WorldStateChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldStateModel.class)
public class WorldStateModelMixin {
    @Inject(method = "setState(Lcom/wynntils/models/worlds/type/WorldState;Ljava/lang/String;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/wynntils/core/WynntilsMod;postEvent(Lnet/neoforged/bus/api/Event;)Z"))
    public void onSetState(WorldState newState,
                           String newWorldName,
                           boolean isFirstJoinWorld,
                           CallbackInfo ci,
                           @Local(name = "oldState") WorldState oldState) {
        EventBus.emitSync(new WorldStateChangeEvent(newState, oldState, newWorldName, isFirstJoinWorld));
    }
}
