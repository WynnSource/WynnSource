package fyw.fyi.wynnsource.data.transformer

import fyw.fyi.wynnsource.schema.WynnSourceItemOuterClass

abstract class ItemTransformer<T> {
    abstract fun serialize(item: T): WynnSourceItemOuterClass.WynnSourceItem

    abstract fun deserialize(item: WynnSourceItemOuterClass.WynnSourceItem): T
}
