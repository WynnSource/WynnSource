package fyw.fyi.wynnsource.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Style
import net.minecraft.text.StyleSpriteSource
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Colors
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

object ChatLogger {
    private val FONT_BANNER_PILL = StyleSpriteSource.Font(Identifier.of("minecraft:banner/pill"))
    private val BACKGROUND_STYLE: Style? =
        Style.EMPTY.withFont(FONT_BANNER_PILL).withColor(TextColor.fromRgb(0xFF66CCFF.toInt()))
    private val FOREGROUND_STYLE: Style? =
        Style.EMPTY.withFont(FONT_BANNER_PILL).withColor(Colors.BLACK).withoutShadow()

    private val BackgroundTag =
        Text.literal(
            FontUtils.toBanner(
                "\uE060" +
                        "\uDAFF\uDFFFW" +
                        "\uDAFF\uDFFFY" +
                        "\uDAFF\uDFFFN" +
                        "\uDAFF\uDFFFN" +
                        "\uDAFF\uDFFFS" +
                        "\uDAFF\uDFFFO" +
                        "\uDAFF\uDFFFU" +
                        "\uDAFF\uDFFFR" +
                        "\uDAFF\uDFFFC" +
                        "\uDAFF\uDFFFE" +
                        "\uDAFF\uDFFF\uE062" +
                        "\uDAFF\uDFD0",
                true
            )
        )
            .setStyle(BACKGROUND_STYLE)

    // -14 WYNNSOURCE 2
    private val ForegroundTag =
        Text.literal(FontUtils.toBanner("\uDAFF\uDFF2WYNNSOURCE\uDB00\uDC02 "))
            .setStyle(FOREGROUND_STYLE)

    private val ModTag = Text.empty()
        .append(BackgroundTag)
        .append(ForegroundTag)

    private val reset = Text.empty().formatted(Formatting.RESET)

    fun log(message: Text) {
        MinecraftClient.getInstance().execute {
            MinecraftClient.getInstance().player?.sendMessage(
                Text.empty()
                    .append(ModTag.copy())
                    .append(reset)
                    .append(message), false
            )
        }
    }
}
