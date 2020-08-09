package com.skfaid.gurules.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.skfaid.gurules.R
import com.skfaid.gurules.model.GuruResponse
import com.skfaid.gurules.model.Sortdata
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var mainAdapter: MainAdapter
    private var contentGuru: MutableList<GuruResponse> = mutableListOf()
    private lateinit var locationManager: LocationManager
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private var shopLocation: Location? = null
    private var deviceLocation: Location? = null
    private var deviceLatitude: Double? = 0.0
    private var deviceLongitude: Double? = 0.0
    private var hasGps = false
    private var hasNetwork = false
    private var distance: Double? = 0.0
    private var gurulist: ArrayList<Sortdata> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getGPSCoordinate()
        layout_shop.setOnRefreshListener {
            layout_shop.isRefreshing = false
//            Log.d("debug", layout_shop.toString())

        }
        mainAdapter = MainAdapter(gurulist) {}

        databaseReference = FirebaseDatabase.getInstance().reference
        showLoading()
        databaseReference.child("guru")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Error", "LoadDatabase: onCancelled", databaseError.toException())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val content = dataSnapshot.children.flatMap {
                        mutableListOf(it.getValue(GuruResponse::class.java))
                    }

//                    Log.d("debug", content.toString())

                    contentGuru.addAll(content as List<GuruResponse>)
                    contentGuru.forEach { response ->
                        shopLocation = Location(LocationManager.GPS_PROVIDER).apply {
                            latitude = response.latitude as Double
                            longitude = response.longitude as Double
                        }
                        val valueLatitude =
                            shopLocation?.latitude?.let { deviceLocation?.latitude?.minus(it) }
                        val valuelongitude =
                            shopLocation?.longitude?.let { deviceLocation?.longitude?.minus(it) }
                        val xSquare = Math.pow(valueLatitude as Double, 2.0)
                        val ySquare = Math.pow(valuelongitude as Double, 2.0)
                        distance = Math.sqrt(xSquare + ySquare) * 111.319

                        val sortdata = Sortdata(
                            response.nama.toString(),
                            response.alamat.toString(),
                            response.mata_pelajaran.toString(),
                            response.foto.toString(),
                            response.latitude as Double,
                            response.longitude as Double,
                            response.nomertp.toString(),
                            response.biaya,
                            distance as Double
                        )
                        gurulist.add(sortdata)
                        gurulist.sortBy { it.distance }
                        hideLoading()
                        mainAdapter.notifyDataSetChanged()
                    }
                }
            })
        deviceLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = deviceLatitude as Double
            longitude = deviceLongitude as Double
        }

        rv_shop.setHasFixedSize(true)
        rv_shop.adapter = mainAdapter

    }


    private fun getGPSCoordinate() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationManager =
                activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (hasGps || hasNetwork) {
                if (hasGps) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0, 0f, object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null) {
                                    locationGps = location
                                    deviceLatitude = locationGps?.latitude
                                    deviceLongitude = locationGps?.longitude
                                }
                            }

                            override fun onStatusChanged(
                                provider: String?,
                                status: Int,
                                extras: Bundle?
                            ) {
                            }

                            override fun onProviderEnabled(provider: String?) {}

                            override fun onProviderDisabled(provider: String?) {}
                        })

                    val localGpsLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (localGpsLocation != null) locationGps = localGpsLocation
                } else {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                if (hasNetwork) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 0f, object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null) {
                                    locationNetwork = location
                                    deviceLatitude = locationNetwork?.latitude
                                    deviceLongitude = locationNetwork?.longitude
                                }
                            }

                            override fun onStatusChanged(
                                provider: String?,
                                status: Int,
                                extras: Bundle?
                            ) {
                            }

                            override fun onProviderEnabled(provider: String?) {}

                            override fun onProviderDisabled(provider: String?) {}
                        })

                    val localNetworkLocation =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (localNetworkLocation != null) locationNetwork = localNetworkLocation
                } else {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

                if (locationGps != null && locationNetwork != null) {
                    if (locationGps?.accuracy as Float > locationNetwork?.accuracy as Float) {
                        deviceLatitude = locationNetwork?.latitude
                        deviceLongitude = locationNetwork?.longitude
                    } else {
                        deviceLatitude = locationNetwork?.latitude
                        deviceLongitude = locationNetwork?.longitude
                    }
                }
            } else {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, 101)
        }
    }
    fun showLoading() {
        shimmerStart()
        rv_shop.isGone
    }
     fun hideLoading() {
        shimmerStop()
//        rv_shop.visibility
    }
//
//    fun showNoDataResult() {
//        shimmerStop()
////        tv_no_data_main.visible()
//        rv_shop.gone()
//    }

    @Suppress("SameParameterValue")
    private fun requestPermission(permissionType: String, requestCode: Int) {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permissionType), requestCode)
    }

    // shimmer loading animation start
    private fun shimmerStart() {
        shimmer_frame_main.visibility
        shimmer_frame_main.startShimmer()
    }

    // shimmer loading animation stop
    private fun shimmerStop() {
        shimmer_frame_main.isGone
        shimmer_frame_main.stopShimmer()
    }


}
