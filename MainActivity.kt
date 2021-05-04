package com.test.alejandragiltest.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.test.alejandragiltest.R
import com.test.alejandragiltest.databinding.MainActivityBinding
import com.test.alejandragiltest.viewmodel.RepoViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class MainActivity : AppCompatActivity()
{
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewDataBinding: MainActivityBinding
    private lateinit var viewModel: RepoViewModel
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val PERMISSION_ID = 1

    // Override methods*
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewDataBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        // Handle item selection
        return when (item.itemId)
        {
            R.id.search ->
            {
                openAutoComplete()
                true
            }
            R.id.current ->
            {
                requestPermission()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                getPlaceData(data)
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR)
            { // TODO: Handle the error.
                val status: Status = Autocomplete.getStatusFromIntent(data!!)
                Log.i("test", status.getStatusMessage()!!)
            }
            else if (resultCode == Activity.RESULT_CANCELED)
            { // The user canceled the operation.
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        if (requestCode == PERMISSION_ID)
        {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                lastLocation()
            }
        }
    }

    /**
     * Return --> Void
     * Params: null
     *  Init methods
     */
    fun init()
    {
        viewModel = ViewModelProviders.of(this).get(RepoViewModel::class.java)
        viewDataBinding.viewmodel = viewModel
        viewDataBinding.setLifecycleOwner(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermission()
    }
    /**
     * Return --> Void
     * Params: data --> Intent
     *  Get values of Place data and call viewmodel getWheatherData function
     */
    fun getPlaceData(data: Intent?)
    {
        val place: Place = Autocomplete.getPlaceFromIntent(data!!)
        var latLng:LatLng = place.latLng!!
        var lat:String = latLng.latitude.toString()
        var lng:String = latLng.longitude.toString()

        viewDataBinding.viewmodel!!.getWeaherData(lat,lng)
    }

    /**
     * Return --> Void
     * Params: null
     *  Call autocomplete intent
     */
    fun openAutoComplete()
    {
        val fields = listOf(Place.Field.ID, Place.Field.NAME)
        val intent: Intent =
            Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.ADDRESS).build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    /**
     * Return --> Void
     * Params: null
     *  Call request permission dialog
     */
    private fun requestPermission()
    {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            Log.i("Permission", "Permission to record denied")
            makeRequest()
        }
        else{
            lastLocation()
        }
    }

    /**
     * Return --> Void
     * Params: null
     *  Call request permission and show user
     */
    private fun makeRequest()
    {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    /**
     * Return --> Void
     * Params: null
     *  Get current location data and set new data into getWheatherData
     */
    fun lastLocation()
    {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location !=null)
                {
                    var lat:String = location.latitude.toString()
                    var lon:String = location.longitude.toString()
                    viewDataBinding.viewmodel!!.getWeaherData(lat,lon)
                }

            }
    }

}
