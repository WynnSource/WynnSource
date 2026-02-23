package fyw.fyi.wynnsource.module

object ModuleRegistry {
    private val modules = mutableListOf<BaseModule>()

    fun register(module: BaseModule) {
        modules.add(module)
    }

    fun getModules(): List<BaseModule> {
        return modules
    }
}
