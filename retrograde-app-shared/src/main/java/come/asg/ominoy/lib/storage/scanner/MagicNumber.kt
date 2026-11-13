package come.asg.ominoy.lib.storage.scanner

import come.asg.ominoy.lib.library.SystemID

class MagicNumber(
    val offset: Int,
    val numbers: ByteArray,
    val systemID: SystemID,
)
