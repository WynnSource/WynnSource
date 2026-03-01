package fyw.fyi.wynnsource.config.ui

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.UIComponents
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper
import java.util.function.Consumer

object VanillaButtonRenderer {
    private val TEXTURES: ButtonTextures = ButtonTextures(
        Identifier.ofVanilla("widget/button"),
        Identifier.ofVanilla("widget/button_disabled"),
        Identifier.ofVanilla("widget/button_highlighted")
    )

    val FIXED_VANILLA: ButtonComponent.Renderer = ButtonComponent.Renderer { context, button, _ ->
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

    fun vanillaButton(message: Text, onPress: Consumer<ButtonComponent>): ButtonComponent {
        return UIComponents.button(message, onPress).renderer(FIXED_VANILLA)
    }
}
