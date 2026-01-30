package page.ooooo.geoshare.lib.inputs

import com.google.re2j.Pattern

const val MAX_PRECISION = 17
const val LAT_NUM = """-?\d{1,2}(\.\d{1,$MAX_PRECISION})?"""
const val LON_NUM = """-?\d{1,3}(\.\d{1,$MAX_PRECISION})?"""
const val LAT = """[\+ ]?(?P<lat>$LAT_NUM)"""
const val LON = """[\+ ]?(?P<lon>$LON_NUM)"""
const val Z = """(?P<z>\d{1,2}(\.\d{1,$MAX_PRECISION})?)"""

/**
 * Name when it appears in URI query param
 *
 * Notice that if it contains LAT,LON, then the group doesn't match.
 */
const val NAME_PARAM = """$LAT,$LON|(?P<name>.+)"""

/**
 * Name when it appears in URI path
 *
 * Notice that if it contains LAT,LON, then the group doesn't match.
 */
const val NAME_PATH = """$LAT,$LON|(?P<name>[^/]+)"""

val LAT_PATTERN: Pattern = Pattern.compile(LAT)
val LON_PATTERN: Pattern = Pattern.compile(LON)
val LAT_LON_PATTERN: Pattern = Pattern.compile("$LAT,$LON")
val LON_LAT_PATTERN: Pattern = Pattern.compile("$LON,$LAT")
val Z_PATTERN: Pattern = Pattern.compile(Z)
val Q_PATH_PATTERN: Pattern = Pattern.compile(NAME_PATH)
val Q_PARAM_PATTERN: Pattern = Pattern.compile(NAME_PARAM)
