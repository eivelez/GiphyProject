package com.abstractchile.clase10

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abstractchile.clase10.configuration.API_KEY
import com.abstractchile.clase10.networking.CatApi
import com.abstractchile.clase10.networking.CatService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_gif_list.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body


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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchButtonImage.setOnClickListener{
            searchActionClick()
        }
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
    }
}


