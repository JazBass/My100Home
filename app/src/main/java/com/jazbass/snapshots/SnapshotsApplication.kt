package com.jazbass.snapshots

import android.app.Application
import com.google.firebase.auth.FirebaseUser

class SnapshotsApplication : Application() {
    companion object {
        const val PATH_SNAPSHOTS = "snapshots"
        const val PROPERTY_LIKE_LIST = "likeList"
        const val EU_WEST_INSTANCE = "https://snapshots-24bdd-default-rtdb.europe-west1.firebasedatabase.app"

        lateinit var currentUser: FirebaseUser
    }
}