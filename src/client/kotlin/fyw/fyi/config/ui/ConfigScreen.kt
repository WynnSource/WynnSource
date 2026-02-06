package fyw.fyi.config.ui

import fyw.fyi.config.constraint.RangeConstraint
import fyw.fyi.config.core.ConfigEntry
import fyw.fyi.config.core.ConfigGroup
import fyw.fyi.config.core.ConfigPage
import fyw.fyi.config.core.ConfigRegistry
import fyw.fyi.data.lang.LangRegistry
import fyw.fyi.data.lang.Translatable
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.component.UIComponents
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.UIContainers
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.UIComponent
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.util.*
import java.util.function.Consumer

/**
 * The main configuration screen for WynnSource.
 * This screen dynamically builds its UI based on the registered ConfigPages and their entries.
 *
 * Features:
 * - Left-side tab panel for switching between config pages
 * - Right-side content area that updates based on the selected page
 * - Support for grouped and ungrouped config entries
 * - Validation error display for each entry
 * - Reset, Apply, and Cancel buttons with appropriate actions
 *
 * This class uses the Owo UI library to construct the interface.
 */
@Suppress("unused")
class ConfigScreen(private val parent: Screen? = null) : BaseOwoScreen<FlowLayout>() {
    private var currentPage: ConfigPage? = null
    private var contentPanel: FlowLayout? = null
    private val tabButtons = mutableMapOf<String, ButtonComponent>()

    /**
     * A vanilla-styled button for the config screen.
     */
    private fun vanillaButton(text: Text, onPress: Consumer<ButtonComponent>): ButtonComponent {
        return UIComponents.button(text, onPress).renderer(VanillaButtonRenderer.FIXED_VANILLA)
    }

    companion object {
        val TITLE: Translatable = LangRegistry.translatable(
            "config.wynnsource.title",
            "WynnSource 配置",
            "WynnSource Configuration"
        )
        val RESET: Translatable = LangRegistry.translatable(
            "config.button.reset",
            "重置默认值",
            "Reset to Default"
        )
        val CANCEL: Translatable = LangRegistry.translatable(
            "config.button.cancel",
            "取消",
            "Cancel"
        )
        val APPLY: Translatable = LangRegistry.translatable(
            "config.button.apply",
            "应用",
            "Apply"
        )
        val SAVE_EXIT: Translatable = LangRegistry.translatable(
            "config.button.save_exit",
            "保存并退出",
            "Save & Exit"
        )
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)

        // Main panel
        val mainPanel = UIContainers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
        mainPanel
            .padding(Insets.of(8))

        // Title
        mainPanel.child(
            UIComponents.label(TITLE.toComponent())
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .sizing(Sizing.fill(100), Sizing.content())
                .margins(Insets.bottom(8))
        )

        // Content area (left tabs + right content)
        val contentArea = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.expand(100))
        contentArea.gap(8)

        // Left tab panel
        val tabPanel = buildTabPanel()
        contentArea.child(tabPanel)

        // Right content panel with scroll
        val scrollContent = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
        scrollContent.gap(4)
        contentPanel = scrollContent

        val scrollContainer = UIContainers.verticalScroll(
            Sizing.expand(100),
            Sizing.fill(100),
            scrollContent
        )

        contentArea.child(scrollContainer)
        mainPanel.child(contentArea)

        // Bottom button bar
        val buttonBar = buildButtonBar()
        mainPanel.child(buttonBar)

        rootComponent.child(mainPanel)

        // Show global page or first page
        ConfigRegistry.getGlobalPage()?.let { switchToPage(it) }
            ?: ConfigRegistry.getPages().firstOrNull()?.let { switchToPage(it) }
    }

    private fun buildTabPanel(): FlowLayout {
        val panel = UIContainers.verticalFlow(Sizing.fixed(100), Sizing.content())
        panel.gap(4)
        panel.padding(Insets.right(4))

        ConfigRegistry.getPages().forEach { page ->
            val tabButton = vanillaButton(page.name.toComponent()) {
                switchToPage(page)
            }
            tabButton.sizing(Sizing.fill(100), Sizing.fixed(20))
            tabButtons[page.id] = tabButton
            panel.child(tabButton)
        }

        return panel
    }

    private fun buildButtonBar(): FlowLayout {
        val bar = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content())
        bar.gap(6)
        bar.margins(Insets.top(8))
        bar.horizontalAlignment(HorizontalAlignment.RIGHT)

        // Reset button (left side)
        bar.child(
            vanillaButton(RESET.toComponent()) { resetToDefaults() }
                .sizing(Sizing.content(), Sizing.fixed(20))
        )

        // Spacer to push other buttons to the right
        bar.child(
            UIComponents.box(Sizing.expand(100), Sizing.fixed(1))
                .color(Color.ofArgb(0))
        )

        // Cancel button
        bar.child(
            vanillaButton(CANCEL.toComponent()) { cancel() }
                .sizing(Sizing.content(), Sizing.fixed(20))
        )

        // Apply button
        bar.child(
            vanillaButton(APPLY.toComponent()) { applyChanges() }
                .sizing(Sizing.content(), Sizing.fixed(20))
        )

        // Save & Exit button
        bar.child(
            vanillaButton(SAVE_EXIT.toComponent()) { saveAndExit() }
                .sizing(Sizing.content(), Sizing.fixed(20))
        )

        return bar
    }

    private fun switchToPage(page: ConfigPage) {
        currentPage = page

        tabButtons.forEach { (id, btn) ->
            btn.active(id != page.id)
        }

        refreshContent()
    }

    private fun refreshContent() {
        val panel = contentPanel ?: return
        panel.clearChildren()

        currentPage?.let { page ->
            // Render ungrouped entries first
            page.getUngroupedEntries().forEach { entry ->
                panel.child(createEntryWidget(entry))
            }

            // Render groups
            page.getAllGroups().forEach { group ->
                panel.child(createGroupComponent(group))
            }
        }
    }

    private fun createGroupComponent(group: ConfigGroup): UIComponent {
        val collapsible = UIContainers.collapsible(
            Sizing.fill(100),
            Sizing.content(),
            group.name.toComponent(),
            group.expanded
        )

        group.getEntries().forEach { entry ->
            collapsible.child(createEntryWidget(entry))
        }

        // Update group expanded state when toggled
        collapsible.onToggled().subscribe { expanded ->
            group.expanded = expanded
        }

        return collapsible
    }

    @Suppress("UNCHECKED_CAST")
    private fun createEntryWidget(entry: ConfigEntry<*>): UIComponent {
        return when (entry.type) {
            Boolean::class -> createBooleanWidget(entry as ConfigEntry<Boolean>)
            Int::class -> createIntWidget(entry as ConfigEntry<Int>)
            Long::class -> createLongWidget(entry as ConfigEntry<Long>)
            Float::class -> createFloatWidget(entry as ConfigEntry<Float>)
            Double::class -> createDoubleWidget(entry as ConfigEntry<Double>)
            String::class -> createStringWidget(entry as ConfigEntry<String>)
            else -> {
                if (entry.type.java.isEnum) {
                    createEnumWidget(entry)
                } else {
                    UIComponents.label(Text.literal("Unsupported: ${entry.key}"))
                }
            }
        }
    }

    // ==================== Widget Creation ====================

    private fun createBooleanWidget(entry: ConfigEntry<Boolean>): UIComponent {
        val row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22))
        row.verticalAlignment(VerticalAlignment.CENTER)
        row.margins(Insets.vertical(2))

        // Label
        row.child(createLabel(entry))

        // Spacer
        row.child(createSpacer())

        // Toggle button
        val buttonTextSupplier = {
            if (entry.getPending()) {
                Text.translatable("options.on")
            } else {
                Text.translatable("options.off")
            }
        }

        val toggleBtn = vanillaButton(buttonTextSupplier()) { btn ->
            entry.setPending(!entry.getPending())
            btn.setMessage(buttonTextSupplier())
        }
        toggleBtn.sizing(Sizing.fixed(50), Sizing.fixed(20))
        row.child(toggleBtn)

        return row
    }

    private fun createIntWidget(entry: ConfigEntry<Int>): UIComponent {
        return createNumberWidget(
            entry,
            getValue = { it.toDouble() },
            setValue = { entry.setPending(it.toInt()) },
            formatValue = { it.toInt().toString() },
            getRange = { constraints ->
                constraints.filterIsInstance<RangeConstraint<Int>>().firstOrNull()?.let {
                    it.getMin().toDouble() to it.getMax().toDouble()
                }
            }
        )
    }

    private fun createLongWidget(entry: ConfigEntry<Long>): UIComponent {
        return createNumberWidget(
            entry,
            getValue = { it.toDouble() },
            setValue = { entry.setPending(it.toLong()) },
            formatValue = { it.toLong().toString() },
            getRange = { constraints ->
                constraints.filterIsInstance<RangeConstraint<Long>>().firstOrNull()?.let {
                    it.getMin().toDouble() to it.getMax().toDouble()
                }
            }
        )
    }

    private fun createFloatWidget(entry: ConfigEntry<Float>): UIComponent {
        return createNumberWidget(
            entry,
            getValue = { it.toDouble() },
            setValue = { entry.setPending(it.toFloat()) },
            formatValue = { String.format(Locale.ENGLISH, "%.2f", it) },
            getRange = { constraints ->
                constraints.filterIsInstance<RangeConstraint<Float>>().firstOrNull()?.let {
                    it.getMin().toDouble() to it.getMax().toDouble()
                }
            }
        )
    }

    private fun createDoubleWidget(entry: ConfigEntry<Double>): UIComponent {
        return createNumberWidget(
            entry,
            getValue = { it },
            setValue = { entry.setPending(it) },
            formatValue = { String.format(Locale.ENGLISH, "%.2f", it) },
            getRange = { constraints ->
                constraints.filterIsInstance<RangeConstraint<Double>>().firstOrNull()?.let {
                    it.getMin() to it.getMax()
                }
            }
        )
    }

    private fun <T : Number> createNumberWidget(
        entry: ConfigEntry<T>,
        getValue: (T) -> Double,
        setValue: (Double) -> Unit,
        formatValue: (Double) -> String,
        getRange: (List<*>) -> Pair<Double, Double>?
    ): UIComponent {
        val range = getRange(entry.getConstraints())

        val container = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
        container.margins(Insets.vertical(2))

        // Label row with current value
        val valueLabel = UIComponents.label(Text.literal(formatValue(getValue(entry.getPending()))))

        val labelRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content())
        val nameLabel = UIComponents.label(entry.name?.toComponent() ?: Text.literal(entry.key))
        entry.description?.let { nameLabel.tooltip(it.toComponent()) }
        labelRow.child(nameLabel)
        labelRow.child(
            UIComponents.box(Sizing.expand(100), Sizing.fixed(1))
                .color(Color.ofArgb(0))
        )
        labelRow.child(valueLabel)
        container.child(labelRow)

        // Slider if we have a range constraint
        if (range != null) {
            val (minVal, maxVal) = range
            val sliderValue = (getValue(entry.getPending()) - minVal) / (maxVal - minVal)

            val slider = UIComponents.slider(Sizing.fill(95))
            slider.value(sliderValue)
            slider.onChanged().subscribe { newSliderValue ->
                val actualValue = minVal + (newSliderValue * (maxVal - minVal))
                setValue(actualValue)
                valueLabel.text(Text.literal(formatValue(actualValue)))
            }
            container.child(slider)
        } else {
            // Text input for numbers without range
            val textBox = UIComponents.textBox(Sizing.fill(100))
            textBox.text(formatValue(getValue(entry.getPending())))
            textBox.onChanged().subscribe { text ->
                text.toDoubleOrNull()?.let { newValue ->
                    setValue(newValue)
                }
            }
            container.child(textBox)
        }

        // Error label (hidden by default)
        container.child(createErrorLabel(entry))

        return container
    }

    private fun createStringWidget(entry: ConfigEntry<String>): UIComponent {
        val container = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
        container.margins(Insets.vertical(2))

        // Label
        container.child(createLabel(entry))

        // Text input
        val textBox = UIComponents.textBox(Sizing.fill(100))
        textBox.text(entry.getPending())
        textBox.onChanged().subscribe { text ->
            entry.setPending(text)
        }
        container.child(textBox)

        // Error label
        container.child(createErrorLabel(entry))

        return container
    }

    @Suppress("UNCHECKED_CAST")
    private fun createEnumWidget(entry: ConfigEntry<*>): UIComponent {
        val enumConstants = entry.type.java.enumConstants as Array<Enum<*>>
        var currentIndex = enumConstants.indexOfFirst {
            it.name == (entry.getPending() as Enum<*>).name
        }.coerceAtLeast(0)

        val row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22))
        row.verticalAlignment(VerticalAlignment.CENTER)
        row.margins(Insets.vertical(2))

        // Label
        row.child(createLabel(entry))

        // Spacer
        row.child(createSpacer())

        // Cycle button
        val cycleBtn = vanillaButton(Text.literal(enumConstants[currentIndex].name)) { btn ->
            currentIndex = (currentIndex + 1) % enumConstants.size
            val newValue = enumConstants[currentIndex]
            (entry as ConfigEntry<Any>).setPending(newValue)
            btn.setMessage(Text.literal(newValue.name))
        }
        cycleBtn.sizing(Sizing.fixed(80), Sizing.fixed(20))
        row.child(cycleBtn)

        return row
    }

    private fun createLabel(entry: ConfigEntry<*>): LabelComponent {
        val label = UIComponents.label(entry.name?.toComponent() ?: Text.literal(entry.key))
        entry.description?.let { label.tooltip(it.toComponent()) }
        return label
    }

    private fun createSpacer(): UIComponent {
        return UIComponents.box(Sizing.expand(100), Sizing.fixed(1)).color(Color.ofArgb(0))
    }

    private fun createErrorLabel(entry: ConfigEntry<*>): LabelComponent {
        val result = entry.validate()
        val errorText = if (!result.valid && result.errorMessage != null) {
            result.errorMessage.toComponent()
        } else {
            Text.empty()
        }

        val label = UIComponents.label(errorText)
        label.color(Color.ofRgb(0xFF5555))
        label.margins(Insets.top(2))
        return label
    }

    // ==================== Button Actions ====================

    private fun applyChanges() {
        // Apply and save ALL pages
        var hasErrors = false
        ConfigRegistry.getPages().forEach { page ->
            val errors = page.validate()
            if (errors.isNotEmpty()) {
                hasErrors = true
            } else {
                page.apply()
                page.save()
            }
        }

        if (hasErrors) {
            // Refresh to show errors on the current page
            refreshContent()
        }
    }

    private fun saveAndExit() {
        // Apply and save all pages
        var hasErrors = false
        ConfigRegistry.getPages().forEach { page ->
            val errors = page.validate()
            if (errors.isNotEmpty()) {
                hasErrors = true
            } else {
                page.apply()
                page.save()
            }
        }

        if (hasErrors) {
            // Don't close if there are validation errors
            refreshContent()
            return
        }

        close()
    }

    private fun cancel() {
        // Discard all unsaved changes
        ConfigRegistry.discardAllChanges()
        close()
    }

    private fun resetToDefaults() {
        currentPage?.resetToDefaults()
        refreshContent()
    }

    override fun close() {
        // Discard any remaining pending changes when closing
        ConfigRegistry.discardAllChanges()
        this.client?.setScreen(parent)
    }
}
