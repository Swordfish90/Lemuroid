package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.annotation.StringRes
import com.swordfish.lemuroid.R

enum class GameSortOrder(@StringRes val labelResId: Int) {
    NAME_ASC(R.string.sort_order_name_asc),
    NAME_DESC(R.string.sort_order_name_desc),
    RATING_DESC(R.string.sort_order_rating_desc),
    RATING_ASC(R.string.sort_order_rating_asc),
    MOST_RATED(R.string.sort_order_most_rated),
    RECENTLY_PLAYED(R.string.sort_order_recently_played),
    MOST_PLAYED(R.string.sort_order_most_played),
    FAVORITES_FIRST(R.string.sort_order_favorites_first),
    RELEASE_DATE_DESC(R.string.sort_order_release_date_desc),
    RELEASE_DATE_ASC(R.string.sort_order_release_date_asc),
    RECENTLY_ADDED(R.string.sort_order_recently_added),
    DEVELOPER_ASC(R.string.sort_order_developer_asc),
    ;

    companion object {
        fun fromKey(key: String?): GameSortOrder {
            return values().firstOrNull { it.name == key } ?: NAME_ASC
        }
    }
}
