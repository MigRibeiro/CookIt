package com.example.cookit.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.cookit.RecipeUserFragment
import com.example.cookit.databinding.ActivityDashboardUserBinding
import com.example.cookit.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    lateinit var binding: ActivityDashboardUserBinding

    lateinit var firebaseAuth: FirebaseAuth

    lateinit var categoryArrayList: ArrayList<ModelCategory>
    lateinit var viewPagerAdapter: ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.addRecipeBtn.setOnClickListener{
            startActivity(Intent(this, RecipeAddActivity::class.java))
        }

        binding.showRestaurantsButton.setOnClickListener {
            redirectToRestaurantsMap()
        }



    }

    fun redirectToRestaurantsMap() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://www.google.com/maps/search/restaurants+in+Portugal")
        startActivity(intent)
    }

    fun setupWithViewPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this

        )

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory("01", "Todos", 1, "")

                categoryArrayList.add(modelAll)

                viewPagerAdapter.addFragment(

                    RecipeUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )

                viewPagerAdapter.notifyDataSetChanged()

                for(ds in snapshot.children){

                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(

                        RecipeUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )

                    viewPagerAdapter.notifyDataSetChanged()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm, behavior) {

        private val fragmentslist: ArrayList<RecipeUserFragment> = ArrayList()

        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentslist.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentslist[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: RecipeUserFragment, title: String){

            fragmentslist.add(fragment)
            fragmentTitleList.add(title)

        }


    }

    fun checkUser(){

        val firebaseUser = firebaseAuth.currentUser
        if ( firebaseUser == null){


            binding.subTitleTv.text = "Não efetuou login"

        }
        else{
            val email = firebaseUser.email

            binding.subTitleTv.text = email
        }
    }

}