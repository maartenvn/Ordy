package com.ordy.app.api.wrappers

import com.google.gson.annotations.SerializedName
import com.ordy.app.api.models.Location

data class LocationWrapper(

    @SerializedName("location")
    val location: Location,

    @SerializedName("favorite")
    var favorite: Boolean
)