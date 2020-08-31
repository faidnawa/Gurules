package com.skfaid.gurules.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UsersProfile(
    var uid: String? = "",
    var name: String? = "",
    var address: String? = "",
    var image_profile: String? = "",
    var phone : String?=""
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "nama" to name,
        "alamat" to address,
        "imageuser" to image_profile,
        "phone" to phone

    )
}