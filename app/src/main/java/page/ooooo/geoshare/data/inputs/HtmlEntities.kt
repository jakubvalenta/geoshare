package page.ooooo.geoshare.data.inputs

fun String.decodeBasicHtmlEntities(): String =
    this.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
