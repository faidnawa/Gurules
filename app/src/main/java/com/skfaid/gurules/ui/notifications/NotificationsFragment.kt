package com.skfaid.gurules.ui.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import coil.api.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.skfaid.gurules.R
import com.skfaid.gurules.ui.completeuserprofile.CompleteUserProfileActivity
import com.skfaid.gurules.ui.login.LoginActivity
import com.skfaid.gurules.model.UsersProfile
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.jetbrains.anko.support.v4.toast
import java.util.*


class NotificationsFragment : Fragment() {

    lateinit var mAuth: FirebaseAuth
    lateinit var databaseReference : DatabaseReference
    private var contentusercekdata: MutableList<UsersProfile> = mutableListOf()
    private var deviceLocation: Location? = null
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private var hasGps = false
    private var hasNetwork = false
    private lateinit var locationManager: LocationManager
    private var deviceLatitude: Double? = 0.0
    private var deviceLongitude: Double? = 0.0
    private var mIsRefresh = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getGPSCoordinate()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
         deviceLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = deviceLatitude as Double
            longitude = deviceLongitude as Double
        }

        layout_userprofile.setOnRefreshListener {
            layout_userprofile.isRefreshing = false
            fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
        }
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        if (mAuth.currentUser !=null){
            databaseReference.child("users").orderByChild("uid").equalTo(mAuth.currentUser?.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Error", "LoadDatabase: onCancelled", databaseError.toException())
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val content = dataSnapshot.children.flatMap {
                            mutableListOf(it.getValue(UsersProfile::class.java))
                        }
                        contentusercekdata.addAll(content as List<UsersProfile>)
                        if (contentusercekdata?.isNotEmpty() == false)
                            returnToCompleteUserProfile()
                        contentusercekdata.forEach { userDataRespon->
                            fotouser.load(userDataRespon.image_profile){
//                                crossfade(true)
                                placeholder(R.drawable.loading_animation)
//                                error(R.drawable.ic_broken_image)
                            }
                            namauserprofile.text=userDataRespon.name
                            nomeruserprofile.text=userDataRespon.phone
                            showname(deviceLocation!!.latitude, deviceLocation!!.longitude)


                        }
                    }
                })
        }else{
            returnToSignInActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if (id == R.id.about){
            toast("abaut")
        }
        if (id == R.id.logout) {
            toast("logout")
        }
        return super.onOptionsItemSelected(item)
    }
    fun returnToCompleteUserProfile() {
        getActivity()?.finish()
        val intent = Intent (activity, CompleteUserProfileActivity::class.java)
        activity?.startActivity(intent)
    }
    fun returnToSignInActivity() {
        getActivity()?.finish()
        val intent = Intent (activity, LoginActivity::class.java)
        activity?.startActivity(intent)
    }

    fun showname(lat : Double, long: Double):String {
        var namalokasi=""
        val geocoder= Geocoder(context, Locale.getDefault())
        try {
            val anddresses = geocoder.getFromLocation(lat,long,1)
            if (anddresses.size > 0){
                val fetchedAddress = anddresses.get(0)
                val strAddress= StringBuilder()
                for (i in 0..fetchedAddress.maxAddressLineIndex){
                    namalokasi=strAddress.append(fetchedAddress.getAddressLine(i)).append("").toString()
                    Log.d("lokadiuser", namalokasi)
                    alamatuserprofile.text=namalokasi

                }

            }
        } catch (e:Exception){

        }
        return namalokasi
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
    @Suppress("SameParameterValue")
    private fun requestPermission(permissionType: String, requestCode: Int) {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permissionType), requestCode)
    }

    override fun onStart() {
        super.onStart()
        layout_userprofile.isRefreshing = false
    }
}
