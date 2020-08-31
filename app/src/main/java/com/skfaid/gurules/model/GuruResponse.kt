package com.skfaid.gurules.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GuruResponse(
    var uid: String? = "",
    var nama: String? = "",
    var alamat: String? = "",
    var mata_pelajaran: String? = "",
    var foto: String? = "",
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    var nomertp: String? = "",
    var biaya: Int? = 0
)
 {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to uid,
        "nama" to nama,
        "alamat" to alamat,
        "mata_pelajaran" to mata_pelajaran,
        "foto" to foto,
        "latitude" to latitude,
        "longitude" to longitude,
        "nomertp" to nomertp,
        "biaya" to biaya

    )
}