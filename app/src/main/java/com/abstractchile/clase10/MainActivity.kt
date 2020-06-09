package com.abstractchile.clase10

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.abstractchile.clase10.configuration.API_KEY
import com.abstractchile.clase10.networking.CatApi
import com.abstractchile.clase10.networking.CatService
import com.google.android.gms.location.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(),
    CategoryFragment.OnListFragmentInteractionListener,
    SubcategoryFragment.OnListFragmentInteractionListener2,
    GifFragment.OnListFragmentInteractionListener3{
    var selectedCategory:String = ""
    var selectedSubCategory:String = ""
    var listOfGif:MutableList<String> = ArrayList()
    var listOfCategories:MutableList<String> = ArrayList()
    var listOfSubcategories:MutableList<MutableList<String>> = ArrayList()
    var directSearch = 0
    var listOfFavorites:MutableList<String> = ArrayList()
    var listOfDirections:MutableList<String> = ArrayList()
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val name = intent.getStringExtra("NAME")
        val email = intent.getStringExtra("EMAIL")
        nameTextView.text = name
        emailTextView.text = email
        val request = CatService.buildService(CatApi::class.java)
        val call = request.getCategories()
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val d = response.body() as JsonObject
                    val e = d.getAsJsonArray("data")
                    for (index in 0 until e.size()){
                        val f = e[index] as JsonObject
                        val subcategories = f.get("subcategories") as JsonArray
                        var subcategoriesList:MutableList<String> = ArrayList()
                        for (index2 in 0 until subcategories.size()){
                            val subCat = subcategories[index2] as JsonObject
                            subcategoriesList.add(subCat.get("name_encoded").asString)
                        }
                        listOfSubcategories.add(subcategoriesList)
                        println(subcategoriesList)
                        listOfCategories.add(f.get("name_encoded").asString)
                    }
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.mainRecycler,CategoryFragment.newInstance(listOfCategories),"categoriesList")
                        .commit()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
        searchButtonImage.setOnClickListener{
            searchActionClick()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        requestNewLocationData()
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        if (location != null) {
                            findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
                            findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
            findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    fun searchActionClick(){
        var searchWord=inputText.text.toString()
        if (searchWord==""){
            listOfGif.clear()
            val request = CatService.buildService(CatApi::class.java)
            val call = request.getRandom(API_KEY)
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful)
                    {
                        val d = response.body() as JsonObject
                        val e = d.get("data") as JsonObject
                        val g = e.get("images") as JsonObject
                        val h = g.get("fixed_width_small") as JsonObject
                        listOfGif.add( h.get("url").asString)
                        val openFragment = supportFragmentManager.findFragmentByTag("categoriesList")
                        if (openFragment != null) {
                            supportFragmentManager.beginTransaction().remove(openFragment).commit()
                            supportFragmentManager
                                .beginTransaction()
                                .add(R.id.mainRecycler,GifFragment.newInstance(listOfGif),"GifsSearch")
                                .commit()
                            searchContainer.visibility = View.GONE
                            titleTextView.text = "Random Gif"
                        }

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        }
        else{
            listOfGif.clear()
            val request = CatService.buildService(CatApi::class.java)
            val limit = 10
            val call = request.getSearch(API_KEY,limit.toString(),searchWord)
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful)
                    {
                        val d = response.body() as JsonObject
                        val e = d.getAsJsonArray("data")
                        val f = e.size()
                        for (index in 0 until f){
                            val g = e[index] as JsonObject
                            val h = g.get("images") as JsonObject
                            val i = h.get("fixed_width_small") as JsonObject
                            listOfGif.add( i.get("url").asString)

                        }
                        val openFragment = supportFragmentManager.findFragmentByTag("categoriesList")
                        if (openFragment != null) {
                            supportFragmentManager.beginTransaction().remove(openFragment).commit()

                            supportFragmentManager
                                .beginTransaction()
                                .add(R.id.mainRecycler,GifFragment.newInstance(listOfGif),"GifsSearch")
                                .commit()
                            searchContainer.visibility = View.GONE
                            if (listOfGif.size > 0){
                                titleTextView.text = searchWord
                            }
                            else{
                                titleTextView.text="Nothing found!"
                            }
                            inputText.text.clear()

                        }

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    override fun onBackPressed() {

        var opened = supportFragmentManager.findFragmentByTag("categoriesList")
        if (opened != null){
            super.onBackPressed()
        }
        opened = supportFragmentManager.findFragmentByTag("Subcategories")
        if (opened != null) {
            supportFragmentManager.beginTransaction().remove(opened).commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.mainRecycler,CategoryFragment.newInstance(listOfCategories),"categoriesList")
                .commit()
            titleTextView.text="Categories"
            searchContainer.visibility = View.VISIBLE
        }
        opened = supportFragmentManager.findFragmentByTag("Gifs")
        if (opened != null) {
            var itemNumber = listOfCategories.indexOf(selectedCategory)
            supportFragmentManager.beginTransaction().remove(opened).commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.mainRecycler,SubcategoryFragment.newInstance(listOfSubcategories[itemNumber]),"Subcategories")
                .commit()
        }
        opened  = supportFragmentManager.findFragmentByTag("GifsSearch")
        if (opened != null){
            supportFragmentManager.beginTransaction().remove(opened).commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.mainRecycler,CategoryFragment.newInstance(listOfCategories),"categoriesList")
                .commit()
            titleTextView.text="Categories"

            searchContainer.visibility = View.VISIBLE

        }
    }
    override fun onListFragmentInteraction(item: String) {
        selectedCategory=item
        var itemNumber = listOfCategories.indexOf(selectedCategory)
        var opened = supportFragmentManager.findFragmentByTag("categoriesList")
        if (opened != null) {
            supportFragmentManager.beginTransaction().remove(opened).commit()
            supportFragmentManager.beginTransaction()
                .add(R.id.mainRecycler,SubcategoryFragment.newInstance(listOfSubcategories[itemNumber]),"Subcategories")
                .commit()
            titleTextView.text=item
            searchContainer.visibility=View.GONE
        }

    }

    override fun onListFragmentInteraction2(item: String) {
        var categoryNumber = listOfCategories.indexOf(selectedCategory)
        var subCategoryNumber=listOfSubcategories[categoryNumber].indexOf(item)
        selectedSubCategory = listOfSubcategories[categoryNumber][subCategoryNumber]
        titleTextView.text=selectedSubCategory
        listOfGif.clear()
        val request = CatService.buildService(CatApi::class.java)
        val limit = 10
        val call = request.getSearch(API_KEY,limit.toString(),selectedSubCategory)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful)
                {
                    val d = response.body() as JsonObject
                    val e = d.getAsJsonArray("data")
                    val f = e.size()
                    for (index in 0 until f){
                        val g = e[index] as JsonObject
                        val h = g.get("images") as JsonObject
                        val i = h.get("fixed_width_small") as JsonObject
                        listOfGif.add( i.get("url").asString)

                    }
                    val openFragment = supportFragmentManager.findFragmentByTag("Subcategories")
                    if (openFragment != null) {
                        supportFragmentManager.beginTransaction().remove(openFragment).commit()

                        supportFragmentManager
                            .beginTransaction()
                            .add(R.id.mainRecycler,GifFragment.newInstance(listOfGif),"Gifs")
                            .commit()
                    }

                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onListFragmentInteraction3(item: String) {
        println(item)
        getLastLocation()
    }
}


// read info
/**
val cartAsText = File(context?.filesDir, "cart.txt").bufferedReader().readLines()
cartAsText.forEach{
itemsOnCart.add(it)
}

 write
val file = File(this.filesDir, "cart.txt")
file.createNewFile()
file.appendText(item+"\n")
 */


