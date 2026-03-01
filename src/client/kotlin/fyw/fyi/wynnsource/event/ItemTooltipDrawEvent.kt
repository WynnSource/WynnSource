package fyw.fyi.wynnsource.event

import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class ItemTooltipDrawEvent(
    val text: List<Text>,
    val itemStack: ItemStack?,
    val x: Int,
    val y: Int,
) : BaseEvent()
