package com.rippleeffect.fleettracking.mvvm.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rippleeffect.fleettracking.R
import com.rippleeffect.fleettracking.databinding.ActivityMainBinding
import com.rippleeffect.fleettracking.mvvm.base.ParentInteractor
import com.rippleeffect.fleettracking.mvvm.control.ControlFragment
import com.rippleeffect.fleettracking.mvvm.history.HistoryFragment
import com.rippleeffect.fleettracking.mvvm.map.MapFragment
import com.rippleeffect.fleettracking.util.ServicesUtils
import com.rippleeffect.fleettracking.util.ServicesUtils.PLAY_SERVICES_RESOLUTION_REQUEST
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ParentInteractor {


    companion object {
        private const val SELECTED_ITEM_BOTTOM_NAV = "selected_item_bottom_nav"
    }


    private lateinit var binding: ActivityMainBinding
    private var bottomNavigationInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //This app requires play services

        ServicesUtils.checkPlayServices(this)
        initializeBottomNavigation(
            savedInstanceState?.getInt(SELECTED_ITEM_BOTTOM_NAV) ?: R.id.action_control
        )

    }


    private fun initializeBottomNavigation(@IdRes defaultItemId: Int = R.id.action_control) {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_control -> {
                    startFragment(ControlFragment.newInstance())
                }
                R.id.action_map -> {
                    startFragment(MapFragment.newInstance())
                }
                R.id.action_history -> {
                    startFragment(HistoryFragment.newInstance())
                }
            }
            true
        }

        binding.bottomNavigation.selectedItemId = defaultItemId
        bottomNavigationInitialized = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_ITEM_BOTTOM_NAV, binding.bottomNavigation.selectedItemId)
    }


    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragmentContainer, fragment).commit()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            recreate()
        }
    }

    override fun showLoading() {
        binding.loading.clLoading.visibility = View.VISIBLE
        binding.loading.clLoading.alpha = 1f
    }

    override fun hideLoading() {
        binding.loading.clLoading.animate().alpha(0f).withEndAction {
            binding.loading.clLoading.visibility = View.GONE
        }
    }


}