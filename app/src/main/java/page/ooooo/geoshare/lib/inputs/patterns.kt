package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern

const val MAX_COORD_PRECISION = 17
const val LAT_NUM = """-?\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?"""
const val LON_NUM = """-?\d{1,3}(\.\d{1,$MAX_COORD_PRECISION})?"""
const val LAT = """[\+ ]?(?P<lat>$LAT_NUM)"""
const val LON = """[\+ ]?(?P<lon>$LON_NUM)"""
const val Z = """(?P<z>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
const val Q_PARAM = """(?P<q>.+)"""
const val Q_PATH = """(?P<q>[^/]+)"""

val LAT_PATTERN: Pattern = Pattern.compile(LAT)
val LON_PATTERN: Pattern = Pattern.compile(LON)
val LAT_LON_PATTERN: Pattern = Pattern.compile("$LAT,$LON")
val LON_LAT_PATTERN: Pattern = Pattern.compile("$LON,$LAT")
val Z_PATTERN: Pattern = Pattern.compile(Z)
val Q_PATH_PATTERN: Pattern = Pattern.compile(Q_PATH)
val Q_PARAM_PATTERN: Pattern = Pattern.compile(Q_PARAM)
