package page.ooooo.geoshare.lib.inputs

const val MAX_PRECISION = 17
const val LAT_NUM = """-?\d{1,2}(?:\.\d{1,$MAX_PRECISION})?"""
const val LON_NUM = """-?\d{1,3}(?:\.\d{1,$MAX_PRECISION})?"""
const val LAT = """[+ ]?($LAT_NUM)"""
const val LON = """[+ ]?($LON_NUM)"""
const val Z = """(\d{1,2}(?:\.\d{1,$MAX_PRECISION})?)"""

/**
 * Matches all characters up until two spaces, new line, or end of string.
 *
 * This allows us to support URIs that contain spaces (but not two or more consecutive spaces), which are not valid
 * URIs but users might expect the app to support such URIs nevertheless. Example:
 *
 * "https://maps.google.com/maps?daddr=2088 Albion Rd"
 */
const val URI_REST = """.+?(?= {2}|\n|$)"""

/**
 * Name when it appears in URI query param
 *
 * Notice that if it contains LAT,LON, then the group doesn't match.
 */
const val NAME_PARAM = """[+ ]?$LAT_NUM,[+ ]?$LON_NUM|(.+)"""

/**
 * Name when it appears in URI path
 *
 * Notice that if it contains LAT,LON, then the group doesn't match.
 */
const val NAME_PATH = """[+ ]?$LAT_NUM,[+ ]?$LON_NUM|([^/]+)"""

val LAT_PATTERN = LAT.toRegex()
val LON_PATTERN = LON.toRegex()
val LAT_LON_PATTERN = "$LAT,$LON".toRegex()
val LON_LAT_PATTERN = "$LON,$LAT".toRegex()
val Z_PATTERN = Z.toRegex()
val Q_PATH_PATTERN = NAME_PATH.toRegex()
val Q_PARAM_PATTERN = NAME_PARAM.toRegex()
