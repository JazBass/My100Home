package com.jazbass.snapshots.entities

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Snapshot(@get:Exclude var id: String = "", //Take off the id in database
                    var title: String = "",
                    var photoUrl: String = "",
                    var likeList: Map<String, Boolean> = mutableMapOf())
