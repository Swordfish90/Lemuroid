package come.asg.ominoy.lib.bios

import come.asg.ominoy.lib.library.SystemID

data class Bios(
    val libretroFileName: String,
    val md5: String,
    val description: String,
    val systemID: SystemID,
    val possibleFileSizesBytes: Set<Long>,
    val externalCRC32: String? = null,
    val externalName: String? = null,
) {
    fun displayName() = externalName ?: libretroFileName

    fun matchesChecksum(candidateMd5: String): Boolean = md5.equals(candidateMd5, ignoreCase = true)
}
