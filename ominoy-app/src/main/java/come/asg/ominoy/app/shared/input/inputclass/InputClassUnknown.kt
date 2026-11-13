package come.asg.ominoy.app.shared.input.inputclass

import come.asg.ominoy.app.shared.input.InputKey

object InputClassUnknown : InputClass {
    override fun getInputKeys(): Set<InputKey> = emptySet()

    override fun getAxesMap(): Map<Int, Int> = emptyMap()
}
