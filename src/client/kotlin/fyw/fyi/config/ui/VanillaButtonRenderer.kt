package fyw.fyi.config.ui

import io.wispforest.owo.ui.component.ButtonComponent
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper

object VanillaButtonRenderer {
    private val TEXTURES: ButtonTextures = ButtonTextures(
        Identifier.ofVanilla("widget/button"),
        Identifier.ofVanilla("widget/button_disabled"),
        Identifier.ofVanilla("widget/button_highlighted")
    )

    val FIXED_VANILLA: ButtonComponent.Renderer = ButtonComponent.Renderer { context, button, delta ->
        val texture = when {
            button.active -> if (button.isHovered) TEXTURES.enabledFocused else TEXTURES.enabled
            else -> TEXTURES.disabled
        }
        context.drawGuiTexture(
            RenderPipelines.GUI_TEXTURED,
            texture,
            button.x,
            button.y,
            button.width,
            button.height,
            ColorHelper.getWhite(button.alpha)
        )
    }
}
