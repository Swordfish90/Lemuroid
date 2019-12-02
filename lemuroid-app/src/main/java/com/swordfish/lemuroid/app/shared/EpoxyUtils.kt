package com.swordfish.lemuroid.app.shared

import com.airbnb.epoxy.CarouselModelBuilder
import com.airbnb.epoxy.EpoxyModel

/** Add models to a CarouselModel_ by transforming a list of items into EpoxyModels.
 *
 * @param items The items to transform to models
 * @param modelBuilder A function that take an item and returns a new EpoxyModel for that item. */
inline fun <T> CarouselModelBuilder.withModelsFrom(items: List<T>, modelBuilder: (T) -> EpoxyModel<*>) {
    models(items.map { modelBuilder(it) })
}
