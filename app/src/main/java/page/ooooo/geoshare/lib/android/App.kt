package page.ooooo.geoshare.lib.android

data class App(val packageName: String, val dataTypes: Set<DataType>)

typealias Apps = Map<String, App>
