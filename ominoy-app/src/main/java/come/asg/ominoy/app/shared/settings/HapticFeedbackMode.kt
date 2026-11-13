package come.asg.ominoy.app.shared.settings

enum class HapticFeedbackMode(
    private val key: String,
) {
    NONE("none"),
    PRESS("press"),
    PRESS_RELEASE("press_release"),
    ;

    companion object {
        fun parse(key: String): HapticFeedbackMode =
            HapticFeedbackMode.entries
                .firstOrNull { it.key == key }
                ?: PRESS
    }
}
